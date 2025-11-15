package com.subha.krishi.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.subha.krishi.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.pytorch.torchvision.TensorImageUtils;

public class CropFragment extends Fragment {
    private static final int PICK_IMAGE_REQUEST = 1;

    private ImageView ivLeafImage;
    private Button btnSelectImage, btnPredictDisease;
    private TextView tvPredictionResult;

    private Bitmap bitmap;
    private Module module;
    private List<String> classLabels;

    private EditText etN, etP, etK, etTemperature, etHumidity, etPh, etRainfall;
    private TextView tvCropResult;
    private LinearLayout layoutCrop, layoutDisease;
    private Button btnShowCrop, btnShowDisease;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crop, container, false);

        // Toggle Menu
        btnShowCrop = view.findViewById(R.id.btnShowCrop);
        btnShowDisease = view.findViewById(R.id.btnShowDisease);
        layoutCrop = view.findViewById(R.id.layoutCropPrediction);
        layoutDisease = view.findViewById(R.id.layoutDiseaseDetection);

        // Disease Prediction
        ivLeafImage = view.findViewById(R.id.ivLeafImage);
        btnSelectImage = view.findViewById(R.id.btnSelectImage);
        btnPredictDisease = view.findViewById(R.id.btnPredictDisease);
        tvPredictionResult = view.findViewById(R.id.tvPredictionResult);

        // Crop Prediction UI
        etN = view.findViewById(R.id.etN);
        etP = view.findViewById(R.id.etP);
        etK = view.findViewById(R.id.etK);
        etTemperature = view.findViewById(R.id.etTemperature);
        etHumidity = view.findViewById(R.id.etHumidity);
        etPh = view.findViewById(R.id.etPh);
        etRainfall = view.findViewById(R.id.etRainfall);
        Button btnPredictCrop = view.findViewById(R.id.btnPredictCrop);
        tvCropResult = view.findViewById(R.id.tvCropResult);

        // Toggle between Crop Prediction & Disease Detection
        btnShowCrop.setOnClickListener(v -> {
            layoutCrop.setVisibility(View.VISIBLE);
            layoutDisease.setVisibility(View.GONE);
            btnShowCrop.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
            btnShowDisease.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.grey));
        });

        btnShowDisease.setOnClickListener(v -> {
            layoutCrop.setVisibility(View.GONE);
            layoutDisease.setVisibility(View.VISIBLE);
            btnShowDisease.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.green));
            btnShowCrop.setBackgroundTintList(ContextCompat.getColorStateList(getContext(), R.color.grey));
        });

        // Predict Crop Button Click
        btnPredictCrop.setOnClickListener(v -> {
            if (!validateInputs()) return;
            predictCrop();
        });

        // Load model and labels
        try {
            module = Module.load(assetFilePath("plant_disease_model_mobile.pt"));
            classLabels = loadLabels("classes.txt");
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error loading model or labels", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

        btnSelectImage.setOnClickListener(v -> openGallery());
        btnPredictDisease.setOnClickListener(v -> predictDisease());

        return view;
    }

    // ✅ Validate input fields
    private boolean validateInputs() {
        if (etN.getText().toString().isEmpty() ||
                etP.getText().toString().isEmpty() ||
                etK.getText().toString().isEmpty() ||
                etTemperature.getText().toString().isEmpty() ||
                etHumidity.getText().toString().isEmpty() ||
                etPh.getText().toString().isEmpty() ||
                etRainfall.getText().toString().isEmpty()) {

            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    // 🌾 API Call for Crop Prediction
    private void predictCrop() {
        String url = "https://krishi-crop-prediction-disease-detection.onrender.com/predict_crop";

        try {
            JSONObject jsonRequest = new JSONObject();
            jsonRequest.put("N", Double.parseDouble(etN.getText().toString()));
            jsonRequest.put("P", Double.parseDouble(etP.getText().toString()));
            jsonRequest.put("K", Double.parseDouble(etK.getText().toString()));
            jsonRequest.put("temperature", Double.parseDouble(etTemperature.getText().toString()));
            jsonRequest.put("humidity", Double.parseDouble(etHumidity.getText().toString()));
            jsonRequest.put("ph", Double.parseDouble(etPh.getText().toString()));
            jsonRequest.put("rainfall", Double.parseDouble(etRainfall.getText().toString()));

            tvCropResult.setText("Predicting...");
            tvCropResult.setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));

            RequestQueue queue = Volley.newRequestQueue(requireContext());

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.POST, url, jsonRequest,
                    response -> {
                        try {
                            String crop = response.getString("crop");
                            tvCropResult.setText("🌾 Recommended Crop: " + crop);
                            tvCropResult.setTextColor(ContextCompat.getColor(getContext(), R.color.green));
                        } catch (JSONException e) {
                            e.printStackTrace();
                            tvCropResult.setText("Error parsing response");
                            tvCropResult.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                        }
                    },
                    error -> {
                        error.printStackTrace();
                        tvCropResult.setText("⚠️ API request failed");
                        tvCropResult.setTextColor(ContextCompat.getColor(getContext(), android.R.color.holo_red_dark));
                    }
            );

            queue.add(jsonObjectRequest);

        } catch (JSONException e) {
            e.printStackTrace();
            tvCropResult.setText("Invalid input format");
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Leaf Image"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try (InputStream inputStream = requireContext().getContentResolver().openInputStream(uri)) {
                bitmap = BitmapFactory.decodeStream(inputStream);
                ivLeafImage.setImageBitmap(bitmap);
                btnPredictDisease.setEnabled(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void predictDisease() {
        if (bitmap == null || module == null) {
            Toast.makeText(getContext(), "No image or model not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, true);

        Tensor inputTensor = TensorImageUtils.bitmapToFloat32Tensor(
                resizedBitmap,
                new float[]{0.0f, 0.0f, 0.0f},
                new float[]{1.0f, 1.0f, 1.0f}
//                TensorImageUtils.TORCHVISION_NORM_MEAN_RGB,
//                TensorImageUtils.TORCHVISION_NORM_STD_RGB
        );

        Tensor outputTensor = module.forward(IValue.from(inputTensor)).toTensor();
        float[] scores = outputTensor.getDataAsFloatArray();

        int maxIndex = 0;
        float maxScore = -Float.MAX_VALUE;
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > maxScore) {
                maxScore = scores[i];
                maxIndex = i;
            }
        }

        String prediction = classLabels.get(maxIndex);
        tvPredictionResult.setText("Predicted Disease: " + prediction);
    }

    private String assetFilePath(String assetName) throws IOException {
        File file = new File(requireContext().getFilesDir(), assetName);
        if (file.exists() && file.length() > 0) {
            return file.getAbsolutePath();
        }

        try (InputStream is = requireContext().getAssets().open(assetName);
             FileOutputStream os = new FileOutputStream(file)) {
            byte[] buffer = new byte[4 * 1024];
            int read;
            while ((read = is.read(buffer)) != -1) {
                os.write(buffer, 0, read);
            }
            os.flush();
        }

        return file.getAbsolutePath();
    }

    private List<String> loadLabels(String fileName) throws IOException {
        List<String> labels = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(requireContext().getAssets().open(fileName)))) {
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line.trim());
            }
        }
        return labels;
    }
}
