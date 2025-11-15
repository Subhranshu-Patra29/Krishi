package com.subha.krishi.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.subha.krishi.R;
import com.subha.krishi.helpers.Config;

import org.json.JSONException;
import org.json.JSONObject;

public class AdvancedForecast extends Fragment {
    TextView tvSoilTemp, tvSoilMoisture, tvSoilDepthTemp, tvUVIndex;
    private TextView tvNDVIMean, tvNDVIMedian, tvNDVIMin, tvNDVIMax, tvNDVICloud, tvNDVISource, tvNDVIZoom, tvNDVIArea;
    ImageView imgTrue, imgFalse, imgNDVI, imgEVI, imgEVI2, imgNRI, imgDSWI, imgNDWI;

    String POLY_ID = "691627765a63918ec1b2a8b0";
    private final String APP_ID = Config.AGRO_API;
    private RequestQueue queue;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_adv_forecast, container, false);

        tvSoilTemp = view.findViewById(R.id.tvSoilTemp);
        tvSoilMoisture = view.findViewById(R.id.tvSoilMoisture);
        tvSoilDepthTemp = view.findViewById(R.id.tvSoilDepthTemp);
        tvUVIndex = view.findViewById(R.id.tvUVIndex);

        tvNDVIMean = view.findViewById(R.id.tvNDVIMean);
        tvNDVIMedian = view.findViewById(R.id.tvNDVIMedian);
        tvNDVIMin = view.findViewById(R.id.tvNDVIMin);
        tvNDVIMax = view.findViewById(R.id.tvNDVIMax);
        tvNDVICloud = view.findViewById(R.id.tvNDVICloud);
        tvNDVISource = view.findViewById(R.id.tvNDVISource);
        tvNDVIArea = view.findViewById(R.id.tvNDVIUsefulArea);
        tvNDVIZoom = view.findViewById(R.id.tvNDVIZoom);

        imgTrue = view.findViewById(R.id.imgTrueColor);
        imgFalse = view.findViewById(R.id.imgFalseColor);
        imgNDVI = view.findViewById(R.id.imgNDVI);
        imgEVI = view.findViewById(R.id.imgEVI);
        imgEVI2 = view.findViewById(R.id.imgEVI2);
        imgNRI = view.findViewById(R.id.imgNRI);
        imgDSWI = view.findViewById(R.id.imgDSWI);
        imgNDWI = view.findViewById(R.id.imgNDWI);

        queue = Volley.newRequestQueue(requireContext());

        fetchSoilData();
        fetchUVData();
        fetchNDVIData();
        fetchSatelliteImages();

        return view;
    }

    private void fetchSoilData() {
        String url = "https://api.agromonitoring.com/agro/1.0/soil?polyid=" + POLY_ID + "&appid=" + APP_ID;
//        Log.d("TAG", "fetchSoilData: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        double t0 = response.getDouble("t0") - 273.15;
                        double t10 = response.getDouble("t10") - 273.15;
                        double moisture = response.getDouble("moisture");
                        Log.d("TAG", "fetchSoilData: " + t0 + " " + t10 + " " + moisture);
                        tvSoilTemp.setText(String.format("Surface Temp: %.2f °C", t0));
                        tvSoilDepthTemp.setText(String.format("10cm Temp: %.2f °C", t10));
                        tvSoilMoisture.setText(String.format("Moisture: %.3f m³/m³", moisture));

                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to fetch soil data", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void fetchUVData() {
        String url = "https://api.agromonitoring.com/agro/1.0/uvi?polyid=" + POLY_ID + "&appid=" + APP_ID;
//        Log.d("TAG", "fetchUVData: " + url);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        double uvi = response.getDouble("uvi");
                        Log.d("TAG", "fetchUVData: " + uvi);
                        tvUVIndex.setText(String.format("UV Index: %.2f", uvi));
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Parsing error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to fetch UV data", Toast.LENGTH_SHORT).show()
        );

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(request);
    }

    private void fetchNDVIData() {
        long start = 1736784850L;
        long end = 1763050450L;

        String url = "https://samples.agromonitoring.com/agro/1.0/ndvi/history?polyid=" + POLY_ID +
                "&appid=" + APP_ID + "&start=" + start + "&end=" + end;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {
                            // Take the latest NDVI record (last item)
                            JSONObject latest = response.getJSONObject(response.length() - 1);
                            JSONObject data = latest.getJSONObject("data");

                            double mean = data.getDouble("mean");
                            double median = data.getDouble("median");
                            double min = data.getDouble("min");
                            double max = data.getDouble("max");
                            double cl = latest.getDouble("cl") * 100; // convert fraction to %
                            double dc = latest.getDouble("dc");
                            int zoom = latest.getInt("zoom");
                            String source = latest.getString("source");

                            tvNDVIMean.setText(String.format("Mean NDVI: %.3f", mean));
                            tvNDVIMedian.setText(String.format("Median: %.3f", median));
                            tvNDVIMin.setText(String.format("Min: %.3f", min));
                            tvNDVIMax.setText(String.format("Max: %.3f", max));
                            tvNDVICloud.setText(String.format("Cloud Cover: %.1f%%", cl));
                            tvNDVIArea.setText("Approx. useful area: " + dc + " %");
                            tvNDVIZoom.setText("Zoom Level: " + zoom);
                            tvNDVISource.setText("Source: " + source.toUpperCase());
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "NDVI parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to fetch NDVI data", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void fetchSatelliteImages() {

        long start = 1736784850L;
        long end = 1763050450L;

        String url = "https://api.agromonitoring.com/agro/1.0/image/search?start=" +
                start + "&end=" + end + "&polyid=" + POLY_ID + "&appid=" + APP_ID;
//        Log.d("TAG", "fetchSatelliteImages: " + url);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.length() > 0) {

                            JSONObject obj = response.getJSONObject(response.length() - 1);
                            JSONObject image = obj.getJSONObject("image");

                            loadImage(imgTrue, image.getString("truecolor") + "&bbox=true");
                            loadImage(imgFalse, image.getString("falsecolor") + "&bbox=true");
                            loadImage(imgNDVI, image.getString("ndvi") + "&bbox=true");
                            loadImage(imgEVI, image.getString("evi") + "&bbox=true");
                            loadImage(imgEVI2, image.getString("evi2") + "&bbox=true");
                            loadImage(imgNRI, image.getString("nri") + "&bbox=true");
                            loadImage(imgDSWI, image.getString("dswi") + "&bbox=true");
                            loadImage(imgNDWI, image.getString("ndwi") + "&bbox=true");
                        }
                    } catch (Exception e) {
                        Toast.makeText(getContext(), "Image parse error", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(getContext(), "Failed to load imagery", Toast.LENGTH_SHORT).show()
        );

        queue.add(request);
    }

    private void loadImage(ImageView img, String url) {
//        Log.d("TAG", "loadImage: " + url);
        url = url.replace("http", "https");

        Glide.with(requireContext())
                .load(url)
                .placeholder(R.drawable.image_placeholder_bg)
                .error(R.drawable.error)
                .into(img);
    }
}
