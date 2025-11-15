package com.subha.krishi;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.subha.krishi.fragments.CropFragment;
import com.subha.krishi.fragments.NewsFragment;
import com.subha.krishi.fragments.TipsFragment;
import com.subha.krishi.fragments.WeatherFragment;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import android.widget.TextView;
import android.util.Log;


public class MainActivity extends AppCompatActivity {

    private LinearLayout profileSidebar;
    private View dimOverlay;
    private boolean isSidebarVisible = false;
    private TextView username, userphone;
    private String usernameStr;
    FirebaseFirestore db;
    FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        findViewById(R.id.fragment_container).setOnTouchListener(new View.OnTouchListener() {
            private float startX;
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = event.getX();
                        break;
                    case MotionEvent.ACTION_UP:
                        float endX = event.getX();
                        if (startX - endX < -200 && !isSidebarVisible) { // swipe right
                            toggleSidebar();
                            return true;
                        }
                }
                return false;
            }
        });

        username = findViewById(R.id.tvUserName);
        userphone = findViewById(R.id.tvUserPhone);
        db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            fetchUserData(currentUser.getUid());
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        profileSidebar = findViewById(R.id.profileSidebar);
        dimOverlay = findViewById(R.id.dimOverlay);

        // Load default fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new CropFragment())
                .addToBackStack(null)
                .commit();

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemID = item.getItemId();

                    if(itemID == R.id.nav_weather) {
                        selectedFragment = new WeatherFragment();
                    }
                    else if(itemID == R.id.nav_news) {
                        selectedFragment = new NewsFragment();
                    }
                    else if(itemID == R.id.nav_tips) {
                        selectedFragment = new TipsFragment();
                    }
                    else if(itemID == R.id.nav_crop) {
                        selectedFragment = new CropFragment();
                    }
                    else if(itemID == R.id.profileBtn) {
                        toggleSidebar();
                    }
                    if(selectedFragment != null) {
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, selectedFragment)
                                .addToBackStack(null)
                                .commit();
                    }

                return true;
            }
        });

        // Close sidebar when tapping outside (on overlay)
        dimOverlay.setOnClickListener(v -> toggleSidebar());

        Button logout = findViewById(R.id.btnLogout);
        Button showBlogs = findViewById(R.id.btnShowBlogs);

        logout.setOnClickListener(v -> {
            if (currentUser != null)
                FirebaseAuth.getInstance().signOut();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        showBlogs.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MyBlogsActivity.class);
            intent.putExtra("author_name", usernameStr);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
//            finish();
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSidebarVisible) {
                    toggleSidebar();
                    return;
                }

                FragmentManager fm = getSupportFragmentManager();
                if (fm.getBackStackEntryCount() > 1) {
                    fm.popBackStack();
                } else {
                    finish(); // exit app when only first fragment left
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

    }
    private void toggleSidebar() {
        float translation = isSidebarVisible ? profileSidebar.getWidth() : 0;
        float start = isSidebarVisible ? 0 : profileSidebar.getWidth();

        ObjectAnimator animator = ObjectAnimator.ofFloat(profileSidebar, "translationX", start, translation);
        animator.setDuration(300);
        animator.start();

        if (isSidebarVisible) {
            dimOverlay.setVisibility(View.GONE);
        } else {
            dimOverlay.setVisibility(View.VISIBLE);
        }

        isSidebarVisible = !isSidebarVisible;
    }

    private void fetchUserData(String uid) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        usernameStr = name;
                        String phone = documentSnapshot.getString("phone");
                        username.setText(name);
                        userphone.setText(phone);
                    } else {
                        Log.d("Firestore", "No such user document!");
                    }
                })
                .addOnFailureListener(e -> Log.e("Firestore", "Error fetching user", e));
    }

}