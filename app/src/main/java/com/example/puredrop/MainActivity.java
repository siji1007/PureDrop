package com.example.puredrop;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;


public class MainActivity extends AppCompatActivity {


    MeowBottomNavigation bottomNavigation;
    ViewPager2 viewPager;

    private final Fragment[] fragments = new Fragment[]{
            new home_fragment(),
            new controll_fragment(),
            new key_fragment()
    };

    private boolean isSwipe = false; // Flag to indicate if the navigation was triggered by a swipe

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigation = findViewById(R.id.bottomNavigation);

        if (bottomNavigation != null) {
            bottomNavigation.setCircleColor(Color.parseColor("#ffffff"));
            bottomNavigation.setCountTextColor(Color.WHITE);

            bottomNavigation.add(new MeowBottomNavigation.Model(1, R.drawable.home_icon));
            bottomNavigation.add(new MeowBottomNavigation.Model(2, R.drawable.control_icon));
            bottomNavigation.add(new MeowBottomNavigation.Model(3, R.drawable.key_icon));

            bottomNavigation.setOnClickMenuListener(item -> {
                isSwipe = false;
                viewPager.setCurrentItem(item.getId() - 1);
                // No return statement needed, as Unit (void) is returned implicitly in Kotlin
                return null;
            });


            bottomNavigation.show(2, true);
        } else {
            Log.e("MainActivity", "BottomNavigation is Null");
        }

        viewPager = findViewById(R.id.viewPager);
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

        TabLayout tabLayout = findViewById(R.id.tableLayout);
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            // Customize tab if needed
        }).attach();

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (!isSwipe) {
                    bottomNavigation.show(position + 1, true);
                }
                isSwipe = false; // Reset flag after handling navigation
            }
        });
    }
}
