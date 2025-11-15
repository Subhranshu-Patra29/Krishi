package com.subha.krishi.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.subha.krishi.R;

import java.util.HashMap;

public class TipsFragment extends Fragment {
    AutoCompleteTextView cropCategoryDropdown, cropDropdown;
    MaterialButton getTipsBtn;
    TextView tipsResult;
    MaterialCardView tipsCard;

    HashMap<String, String[]> cropMap = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_tips, container, false);

        cropCategoryDropdown = view.findViewById(R.id.cropCategoryDropdown);
        cropDropdown = view.findViewById(R.id.cropDropdown);
        getTipsBtn = view.findViewById(R.id.getTipsBtn);
        tipsResult = view.findViewById(R.id.tipsResult);
        tipsCard = view.findViewById(R.id.tipsCard);

        setupCropData();
        setupCategoryDropdown();
        setupListeners();

        return view;
    }

    private void setupCropData() {
        cropMap.put("Fruits", new String[]{"Mango", "Banana", "Apple", "Grapes", "Guava"});
        cropMap.put("Vegetables", new String[]{"Tomato", "Potato", "Onion", "Carrot", "Cabbage"});
        cropMap.put("Cereals", new String[]{"Wheat", "Rice", "Maize", "Barley"});
        cropMap.put("Pulses", new String[]{"Moong", "Arhar", "Chana", "Masoor"});
        cropMap.put("Oilseeds", new String[]{"Groundnut", "Mustard", "Sesame"});
        cropMap.put("Spices", new String[]{"Turmeric", "Ginger", "Chilli", "Coriander"});
        cropMap.put("Flowers", new String[]{"Rose", "Marigold", "Jasmine"});
    }

    private void setupCategoryDropdown() {
        String[] categories = cropMap.keySet().toArray(new String[0]);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                categories
        );

        cropCategoryDropdown.setAdapter(adapter);
    }

    private void setupListeners() {

        cropCategoryDropdown.setOnItemClickListener((parent, view, position, id) -> {
            String selectedCategory = parent.getItemAtPosition(position).toString();
            loadSpecificCrops(selectedCategory);
        });

        getTipsBtn.setOnClickListener(v -> {
            String category = cropCategoryDropdown.getText().toString();
            String crop = cropDropdown.getText().toString();

            if (category.isEmpty() || crop.isEmpty()) {
                Toast.makeText(getContext(), "Please select both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Placeholder for AI call
            fetchAITips(category, crop);
        });
    }

    private void loadSpecificCrops(String category) {
        String[] crops = cropMap.get(category);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                crops
        );

        cropDropdown.setText(""); // clear old selection
        cropDropdown.setAdapter(adapter);
    }

    // TODO: Replace with your AI API call
    private void fetchAITips(String category, String crop) {

        // Mock result – replace with actual AI response
        String exampleTip = "Here are some tips for growing " + crop + ":\n\n" +
                "• Ensure proper irrigation.\n" +
                "• Maintain ideal soil moisture.\n" +
                "• Use organic fertilizers.\n" +
                "• Protect from pests using natural methods.";

        tipsResult.setText(exampleTip);
        tipsCard.setVisibility(View.VISIBLE);
    }
}
