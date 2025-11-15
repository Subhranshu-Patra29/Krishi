package com.subha.krishi.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.subha.krishi.R;
import com.subha.krishi.helpers.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {

    private static final String TAG = "WeatherFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final String API_KEY = Config.OPEN_WEATHER_API_KEY;
    private TextView tvLocation, tvTemp, tvDescription;
    private ImageView weatherIcon;
    private LinearLayout hourlyForecastContainer, dailyForecastContainer;
    private FusedLocationProviderClient fusedLocationClient;
    private Button loadMoreBtn;
    private double latitude, longitude;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        tvLocation = view.findViewById(R.id.tvLocation);
        tvTemp = view.findViewById(R.id.tvTemp);
        tvDescription = view.findViewById(R.id.tvDescription);
        weatherIcon = view.findViewById(R.id.weatherIcon);
        hourlyForecastContainer = view.findViewById(R.id.hourlyForecastContainer);
        dailyForecastContainer = view.findViewById(R.id.dailyForecastContainer);
        loadMoreBtn = view.findViewById(R.id.loadMoreBtn);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        getCurrentLocation();

        VideoView weatherVideo = view.findViewById(R.id.weatherVideo);

        // Set the video URI
        Uri uri = Uri.parse("android.resource://" + requireContext().getPackageName() + "/" + R.raw.baro);
        weatherVideo.setVideoURI(uri);

        weatherVideo.setAlpha(0f);
        weatherVideo.animate().alpha(1f).setDuration(800).start();

        // Start playing automatically
        weatherVideo.start();

        // Remove default controls (optional)
        weatherVideo.setMediaController(null);

        // Loop the video
        weatherVideo.setOnCompletionListener(mp -> weatherVideo.start());

        Bundle args = new Bundle();
        args.putDouble("latitude", latitude);
        args.putDouble("longitude", longitude);

        loadMoreBtn.setOnClickListener(v -> {
            Fragment advancedForecast = new AdvancedForecast();
            advancedForecast.setArguments(args);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, advancedForecast)
                    .addToBackStack(null)
                    .commit();
        });
        return view;
    }

    private void getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                fetchWeather(location);
                fetchHourlyWeather(location);
            } else {
                Toast.makeText(getContext(), "Unable to get location", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /** ------------------ DAILY FORECAST ------------------ **/
    @SuppressLint("StaticFieldLeak")
    private void fetchWeather(Location location) {
        new AsyncTask<Location, Void, String>() {
            @Override
            protected String doInBackground(Location... locations) {
                String response = "";
                try {
                    double lat = locations[0].getLatitude();
                    double lon = locations[0].getLongitude();

                    latitude = lat;
                    longitude = lon;

                    Log.d(TAG, "doInBackground: working4"+ "  lat: " + lat + " long: " + lon);
                    int cnt = 15;
                    String urlStr = "https://api.openweathermap.org/data/2.5/forecast/daily?" +
                            "lat=" + lat +
                            "&lon=" + lon +
                            "&cnt=" + cnt +
                            "&units=metric" +
                            "&appid=" + API_KEY;

                    URL url = new URL(urlStr);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("GET");
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        builder.append(line);
                    }
                    reader.close();
                    response = builder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                if (s != null && !s.isEmpty()) {
                    parseWeatherResponse(s);
                }
            }
        }.execute(location);
    }

    private void parseWeatherResponse(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONObject city = json.getJSONObject("city");
            JSONArray list = json.getJSONArray("list");

            tvLocation.setText(city.getString("name"));

            JSONObject today = list.getJSONObject(0);
            JSONObject temp = today.getJSONObject("temp");
            double dayTemp = temp.getDouble("day");
            tvTemp.setText(String.format(Locale.getDefault(), "%.0f°C", dayTemp));

            JSONArray weatherArr = today.getJSONArray("weather");
            JSONObject weatherObj = weatherArr.getJSONObject(0);
            tvDescription.setText(weatherObj.getString("description"));
            String iconCode = weatherObj.getString("icon");
            Glide.with(requireContext())
                    .load("https://openweathermap.org/img/wn/" + iconCode + "@2x.png")
                    .into(weatherIcon);

            // Clear & populate daily forecast
            dailyForecastContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d", Locale.getDefault());

            for (int i = 1; i < list.length(); i++) {
                JSONObject day = list.getJSONObject(i);
                JSONObject tempObj = day.getJSONObject("temp");
                JSONArray dayWeatherArr = day.getJSONArray("weather");
                JSONObject dayWeather = dayWeatherArr.getJSONObject(0);

                View itemView = inflater.inflate(R.layout.item_daily_forecast, dailyForecastContainer, false);

                TextView tvDay = itemView.findViewById(R.id.tvDay);
                ImageView img = itemView.findViewById(R.id.imgWeatherDaily);
                TextView tvTempRange = itemView.findViewById(R.id.tvTempRange);

                long dt = day.getLong("dt") * 1000L;
                tvDay.setText(sdf.format(new Date(dt)));
                tvTempRange.setText(String.format(Locale.getDefault(), "%.0f° / %.0f°",
                        tempObj.getDouble("min"), tempObj.getDouble("max")));

                String dayIcon = dayWeather.getString("icon");
                Glide.with(requireContext())
                        .load("https://openweathermap.org/img/wn/" + dayIcon + "@2x.png")
                        .into(img);

                dailyForecastContainer.addView(itemView);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Failed to parse weather data", Toast.LENGTH_SHORT).show();
        }
    }

    /** ------------------ HOURLY FORECAST ------------------ **/
    @SuppressLint("StaticFieldLeak")
    private void fetchHourlyWeather(Location location) {
        new AsyncTask<Location, Void, String>() {
            @Override
            protected String doInBackground(Location... locations) {
                try {
                    double lat = locations[0].getLatitude();
                    double lon = locations[0].getLongitude();
                    String urlStr = "https://pro.openweathermap.org/data/2.5/forecast/hourly?" +
                            "lat=" + lat + "&lon=" + lon + "&units=metric&appid=" + API_KEY;

                    HttpURLConnection connection = (HttpURLConnection) new URL(urlStr).openConnection();
                    connection.setRequestMethod("GET");

                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder builder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) builder.append(line);
                    reader.close();
                    return builder.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                if (s != null && !s.isEmpty()) parseHourlyWeather(s);
            }
        }.execute(location);
    }

    private void parseHourlyWeather(String response) {
        try {
            JSONObject json = new JSONObject(response);
            JSONArray list = json.getJSONArray("list");

            hourlyForecastContainer.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(requireContext());
            SimpleDateFormat sdf = new SimpleDateFormat("EEE ha", Locale.getDefault());

            // Limit to 96 hours (4 days)
            for (int i = 0; i < Math.min(list.length(), 96); i++) {
                JSONObject hourObj = list.getJSONObject(i);
                JSONObject main = hourObj.getJSONObject("main");
                JSONArray weatherArr = hourObj.getJSONArray("weather");
                JSONObject weather = weatherArr.getJSONObject(0);

                View itemView = inflater.inflate(R.layout.item_hourly_forecast, hourlyForecastContainer, false);

                TextView tvTime = itemView.findViewById(R.id.tvTime);
                ImageView imgWeather = itemView.findViewById(R.id.imgWeather);
                TextView tvTempHourly = itemView.findViewById(R.id.tvTempHourly);

                long dt = hourObj.getLong("dt") * 1000L;
                tvTime.setText(sdf.format(new Date(dt)));
                tvTempHourly.setText(String.format(Locale.getDefault(), "%.0f°C", main.getDouble("temp")));

                String icon = weather.getString("icon");
                Glide.with(requireContext())
                        .load("https://openweathermap.org/img/wn/" + icon + "@2x.png")
                        .into(imgWeather);

                hourlyForecastContainer.addView(itemView);
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error parsing hourly forecast", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
                grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getCurrentLocation();
        }
    }
}
