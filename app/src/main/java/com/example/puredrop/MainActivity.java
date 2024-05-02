package com.example.puredrop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MainActivity extends AppCompatActivity {

    private static final int DIALOG_DURATION = 7000;
    private DatabaseReference rootDatabaseRef;
    MeowBottomNavigation bottomNavigation;
    ViewPager2 viewPager;



    private final Fragment[] fragments = new Fragment[]{
            new home_fragment(),
            new controll_fragment(),
            new key_fragment()
    };

    private boolean isSwipe = false; // Flag to indicate if the navigation was triggered by a swipe


    private FrameLayout overlayLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("your_data_node");

        overlayLayout = findViewById(R.id.overlayLayout);


        bottomNavigation = findViewById(R.id.bottomNavigation);
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tableLayout);

        showInternetConnectionDialog();
        setupBottomNavigation();
        setupViewPager();
        setupTabLayout(tabLayout);

    }


    private void showInternetConnectionDialog() {

        if (overlayLayout != null){
            overlayLayout.setVisibility(View.VISIBLE);

        }


        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.internet_ckeck, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);
        final AlertDialog alertDialog = builder.create();

        if (alertDialog.getWindow() != null) {
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }

        alertDialog.show();

        // Start animation
        LottieAnimationView anim = alertDialog.findViewById(R.id.InternetAnimation);
        if (anim != null) {
            anim.setRepeatCount(LottieDrawable.INFINITE);
            anim.playAnimation();
        }

        // Check internet connection in background after 1 second
        new Handler().postDelayed(() -> {
            boolean isConnected = checkInternetConnection();
            showConnectionResult(isConnected, alertDialog);
        }, DIALOG_DURATION); // Delay for 10 seconds
    }



    private boolean checkInternetConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        }
        return false;
    }

    private void showConnectionResult(boolean isConnected, AlertDialog alertDialog) {
        String message;
        int animationResId = 0; // Initialize to a default value

        if (isConnected) {
            message = "Internet connection is OK!";
            animationResId = R.raw.check; // Replace "check.json" with your animation file for successful connection

        } else {
            message = "No internet connection available!";
            animationResId = R.raw.error; // Replace "error.json" with your animation file for connection error
        }

        // Show toast message
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        // Change the Lottie animation
        LottieAnimationView anim = alertDialog.findViewById(R.id.InternetAnimation);
        if (anim != null) {
            anim.setAnimation(animationResId);
            anim.setRepeatCount(LottieDrawable.INFINITE);
            anim.playAnimation();

            // Delay before dismissing the dialog
            int delay = isConnected ? 1000 : 3200; // Adjust the delay time based on the connection status
            new Handler().postDelayed(() -> {
                alertDialog.dismiss();
                overlayLayout.setVisibility(View.INVISIBLE);
            }, delay); // Change the delay time as needed
        }


}





    private void setupBottomNavigation() {
        bottomNavigation.setCircleColor(Color.parseColor("#ffffff"));
        bottomNavigation.setCountTextColor(Color.WHITE);

        bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.home_icon));
        bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.control_icon));
        bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.key_icon));

        bottomNavigation.setOnClickMenuListener(item -> {
            isSwipe = false;
            viewPager.setCurrentItem(item.getId() - 1);
            return null;
        });

        bottomNavigation.show(2, true);
    }

    private void setupViewPager() {
        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @NonNull
            @Override
            public Fragment createFragment(int position) {
                return fragments[position];
            }

            @Override
            public int getItemCount() {
                return fragments.length;
            }
        });

        viewPager.setCurrentItem(1, false);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (!isSwipe) {
                    bottomNavigation.show(position + 1, true);
                }
                isSwipe = false;
            }
        });
    }

    private void setupTabLayout(TabLayout tabLayout) {
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Customize tab if needed
        }).attach();
    }
}
