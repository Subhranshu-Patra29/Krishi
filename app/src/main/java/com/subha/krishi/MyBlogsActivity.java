package com.subha.krishi;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.subha.krishi.adapters.NewsAdapter;
import com.subha.krishi.model.NewsItem;

import java.util.ArrayList;
import java.util.List;


public class MyBlogsActivity extends AppCompatActivity {
    RecyclerView rvMyBlogs;
    String currentUserName;
    private FirebaseFirestore db;
    List<NewsItem> myBlogsList;
    NewsAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myblogs);

        rvMyBlogs = findViewById(R.id.rvMyBlogs);
        rvMyBlogs.setLayoutManager(new LinearLayoutManager(this));
        myBlogsList = new ArrayList<>();
        adapter = new NewsAdapter(this, myBlogsList);
        rvMyBlogs.setLayoutManager(new LinearLayoutManager(this));
        rvMyBlogs.setAdapter(adapter);

        // 🔸 Get current user's name passed via Intent
        currentUserName = getIntent().getStringExtra("author_name");

        if (currentUserName == null || currentUserName.isEmpty()) {
            Toast.makeText(this, "No user info found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 🔹 Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // 🔸 Fetch blogs by author name
        fetchUserBlogs();
    }

    private void fetchUserBlogs() {
        CollectionReference blogsRef = db.collection("blogs");

        blogsRef
                .whereEqualTo("author", currentUserName)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error != null) {
                            Toast.makeText(MyBlogsActivity.this, "Error loading blogs: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        myBlogsList.clear();

                        if (value != null && !value.isEmpty()) {
                            for (DocumentSnapshot doc : value.getDocuments()) {
                                NewsItem blog = doc.toObject(NewsItem.class);
                                if (blog != null) {
                                    myBlogsList.add(blog);
                                }
                            }
                        }

                        if (myBlogsList.isEmpty()) {
                            Toast.makeText(MyBlogsActivity.this, "No blogs added yet!", Toast.LENGTH_SHORT).show();
                        }

                        adapter.notifyDataSetChanged();
                    }
                });
    }
}
