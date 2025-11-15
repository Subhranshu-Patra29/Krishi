package com.subha.krishi.fragments;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.subha.krishi.R;
import com.subha.krishi.adapters.NewsAdapter;
import com.subha.krishi.helpers.Config;
import com.subha.krishi.helpers.ImgbbApiService;
import com.subha.krishi.model.NewsItem;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NewsFragment extends Fragment {
    RecyclerView rvNews;
    FloatingActionButton fabAdd;
    List<NewsItem> newsList, apiNewsList, blogsList;
    NewsAdapter adapter;
    private static final String API_KEY = Config.NEWS_API;
    private static final String IMGBB_API_KEY = Config.IMGBB_API;
    private static final String NEWS_API_URL = "https://newsapi.org/v2/everything?q=agriculture+farming+agritech&apiKey=" + API_KEY;
    private static final int PICK_IMAGE_REQUEST = 100;
    private Uri selectedImageUri;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        rvNews = view.findViewById(R.id.rvNews);
        fabAdd = view.findViewById(R.id.fabAdd);


        newsList = new ArrayList<>();
        apiNewsList = new ArrayList<>();
        blogsList = new ArrayList<>();
        adapter = new NewsAdapter(getContext(), newsList);
        rvNews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvNews.setAdapter(adapter);

        newsLoader();
        fabAdd.setOnClickListener(v -> showAddBlogDialog());

        return view;
    }

    private void newsLoader()
    {
        loadNews();
        fetchBlogsFromFirebase();
    }

    private void loadNews() {
        Log.d(TAG, "loadNews: URL" + NEWS_API_URL);
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(NEWS_API_URL).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to load news", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    String json = response.body().string();
                    try {
                        JSONObject obj = new JSONObject(json);
                        JSONArray articles = obj.getJSONArray("articles");

                        List<NewsItem> tempList = new ArrayList<>();

                        for (int i = 0; i < articles.length(); i++) {
                            JSONObject article = articles.getJSONObject(i);
                            String author = article.optString("author", "Unknown");
                            String title = article.optString("title", "No Title");
                            String desc = article.optString("description", "No Description");
                            String url = article.optString("url", "");
                            String imageUrl = article.optString("urlToImage", "");
                            String publishedAt = article.optString("publishedAt", "");

                            tempList.add(new NewsItem(author, title, desc, url, imageUrl, publishedAt, false));
                        }

                        getActivity().runOnUiThread(() -> {
                            apiNewsList.clear();
                            apiNewsList.addAll(tempList);
                            adapter.notifyItemRangeInserted(0, apiNewsList.size());
                            mergeNewsAndBlogs();
                            adapter.notifyDataSetChanged();
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void showAddBlogDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("📝 Add New Blog");

        View titleView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_title, null);
        builder.setCustomTitle(titleView);

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_blog, null);
        builder.setView(dialogView);

        EditText etAuthor = dialogView.findViewById(R.id.etAuthor);
        EditText etTitle = dialogView.findViewById(R.id.etTitle);
        EditText etDesc = dialogView.findViewById(R.id.etDesc);
        ImageView ivBlogImage = dialogView.findViewById(R.id.ivBlogImage);

        ivBlogImage.setOnClickListener(v -> openImagePicker());

        builder.setPositiveButton("Add", (dialog, which) -> {
            String author = etAuthor.getText().toString();
            String title = etTitle.getText().toString();
            String desc = etDesc.getText().toString();
            String currDate = getCurrentDate();

            if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(desc)) {
                String imageUriString = selectedImageUri != null ? selectedImageUri.toString() : "";
                if (selectedImageUri != null) {
                    uploadImageToImgbb(selectedImageUri, new OnImageUploadListener() {
                        @Override
                        public void onUploadSuccess(String imageUrl) {
                            // Save blog data to Firestore (with imageUrl)
                            Map<String, Object> blogData = new HashMap<>();
                            blogData.put("author", author);
                            blogData.put("title", title);
                            blogData.put("description", desc);
                            blogData.put("date", currDate);
                            blogData.put("imageUrl", imageUrl);
                            blogData.put("isUserBlog", true);

                            FirebaseFirestore.getInstance()
                                    .collection("blogs")
                                    .add(blogData)
                                    .addOnSuccessListener(doc -> {
                                        Toast.makeText(getContext(), "Blog uploaded successfully!", Toast.LENGTH_SHORT).show();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to save blog: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                            newsLoader();
                        }

                        @Override
                        public void onUploadFailed(String errorMessage) {
                            Toast.makeText(getContext(), "Image upload failed: " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    });
                } else {
                    Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
                }
                blogsList.add(0, new NewsItem(author, title, desc, "", imageUriString, currDate, true));
                adapter.notifyItemInserted(0);
                rvNews.scrollToPosition(0);
            } else {
                Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.dialog_card_bg));
    }

    private String getCurrentDate() {
        SimpleDateFormat ft = new SimpleDateFormat("yyyy-MM-dd");
        String str = ft.format(new Date());

        // Returning the formatted date
        return str;
    }

    // Open gallery to select image
    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    // Handle image result
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();
            // Set preview in dialog
            ImageView ivPreview = getActivity().findViewById(R.id.ivBlogImage);
            if (ivPreview != null) {
                ivPreview.setImageURI(selectedImageUri);
            }
        }
    }

    private void uploadImageToImgbb(Uri imageUri, OnImageUploadListener listener) {
        try {
            File file = new File(getRealPathFromURI(imageUri));
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part body = MultipartBody.Part.createFormData("image", file.getName(), requestFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://api.imgbb.com/")
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            ImgbbApiService apiService = retrofit.create(ImgbbApiService.class);

            Call<ResponseBody> call = apiService.uploadImage(IMGBB_API_KEY, body);
            call.enqueue(new retrofit2.Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String json = response.body().string();
                            JSONObject jsonObj = new JSONObject(json);
                            String imageUrl = jsonObj.getJSONObject("data").getString("url");
                            listener.onUploadSuccess(imageUrl);
                        } catch (Exception e) {
                            listener.onUploadFailed(e.getMessage());
                        }
                    } else {
                        listener.onUploadFailed("Upload failed: " + response.message());
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    listener.onUploadFailed(t.getMessage());
                }
            });
        } catch (Exception e) {
            listener.onUploadFailed(e.getMessage());
        }
    }

    public interface OnImageUploadListener {
        void onUploadSuccess(String imageUrl);
        void onUploadFailed(String errorMessage);
    }

    private String getRealPathFromURI(Uri uri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getActivity().getContentResolver().query(uri, proj, null, null, null);
        if (cursor != null) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            String path = cursor.getString(column_index);
            cursor.close();
            return path;
        }
        return null;
    }

    private void fetchBlogsFromFirebase() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("blogs")
                .orderBy("date", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        String author = doc.getString("author");
                        String title = doc.getString("title");
                        String desc = doc.getString("description");
                        String imageUrl = doc.getString("imageUrl");
                        String date = doc.getString("date");

                        blogsList.add(new NewsItem(author, title, desc, "", imageUrl, date, true));
                    }
                    mergeNewsAndBlogs();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load blogs: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    Log.d(TAG, "fetchBlogsFromFirebase: " + e.getMessage());
                });
    }

    private void mergeNewsAndBlogs() {
        newsList.clear();
        newsList.addAll(blogsList);
        newsList.addAll(apiNewsList);
        adapter.notifyDataSetChanged();
    }

}
