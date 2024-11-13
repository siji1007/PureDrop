package com.example.puredrop;


import static android.graphics.Paint.STRIKE_THRU_TEXT_FLAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;

import android.content.Intent;

import android.content.Context;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.Button;
import android.widget.FrameLayout;

import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.ActivityCompat;
;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.etebarian.meowbottomnavigation.MeowBottomNavigation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.Manifest;

public class MainActivity extends AppCompatActivity {

    private static final int DIALOG_DURATION = 7000;
    private DatabaseReference rootDatabaseRef;
    MeowBottomNavigation bottomNavigation;
    private BluetoothAdapter bluetoothAdapter;
    ViewPager2 viewPager;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 0; // Or any other integer value


    public boolean isSwitchChecked() {
        Switch aSwitch = findViewById(R.id.SwitchBtn);
        TextView switchText = findViewById(R.id.Bluetooth);
        boolean isChecked = aSwitch.isChecked();

        if (!isChecked) {
            aSwitch.setChecked(true); // Set the switch to be checked (ON)
            switchText.setText("ON");
            switchText.setTextColor(Color.GREEN);

            // Check if the BLUETOOTH_ADMIN permission is granted
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN)
                    == PackageManager.PERMISSION_GRANTED) {
                // Check if the device supports Bluetooth
                BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter != null) {
                    // Check if Bluetooth is enabled
                    if (!bluetoothAdapter.isEnabled()) {
                        // Request to enable Bluetooth
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivity(enableBtIntent);
                    }

                } else {
                    // Device does not support Bluetooth
                    Toast.makeText(this, "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Request the BLUETOOTH_ADMIN permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
            }
        }

        return isChecked;
    }


    private final Fragment[] fragments = new Fragment[]{
            new home_fragment(),
            new controll_fragment(),
            new key_fragment()
    };

    private boolean isSwipe = false; // Flag to indicate if the navigation was triggered by a swipe


    private FrameLayout overlayLayout;

    private DatabaseReference notificationRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        rootDatabaseRef = FirebaseDatabase.getInstance().getReference().child("your_data_node");
        notificationRef = FirebaseDatabase.getInstance().getReference().child("notification");

        // Listen for changes to the notification flag
        listenForNotificationChanges();

        overlayLayout = findViewById(R.id.overlayLayout);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();


        bottomNavigation = findViewById(R.id.bottomNavigation);
        viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tableLayout);


        showLoginDialog();
        showInternetConnectionDialog();
        setupBottomNavigation();
        setupViewPager();
        setupTabLayout(tabLayout);










//        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");
//
//        // Get the user ID and password from the registration form
//        String userId = "22-1882";  // This should come from user input
//        String password = "1337";  // This should come from user input
//        String datePurchased = "July 4, 2020";  // This is the date you want to store
//
//        // Store the user ID, password, and Date_Purchased in Firebase
//        usersRef.child(userId).child("password").setValue(password)
//                .addOnCompleteListener(task -> {
//                    if (task.isSuccessful()) {
//                        // Data successfully stored in Firebase
//                        // Now store the Date_Purchased
//                        usersRef.child(userId).child("Date_Purchased").setValue(datePurchased)
//                                .addOnCompleteListener(task1 -> {
//                                    if (task1.isSuccessful()) {
//                                        Toast.makeText(MainActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
//                                    } else {
//                                        // Handle the failure if the Date_Purchased was not stored
//                                        Toast.makeText(MainActivity.this, "Error saving Date Purchased: " + task1.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                                    }
//                                });
//                    } else {
//                        // Handle the failure if the data was not stored
//                        Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
//                    }
//                });

    }

    private void listenForNotificationChanges() {
        // Add a listener to the "notification" flag in Firebase
        notificationRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Get the value of the "notification" flag (true or false)
                Boolean notifyFlag = dataSnapshot.getValue(Boolean.class);

                // If the flag is true, create a notification
                if (notifyFlag != null && notifyFlag) {
                    // Trigger the notification when the value is true
                    createNotification("Device Connection", "The hardware device is connected");

                    // Optionally, reset the notification flag to false after notifying
                    // This step is optional, depending on how you want to handle the flag
                    notificationRef.setValue(false);  // Reset the flag to prevent constant notifications
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle possible errors (e.g., Firebase read failure)
                Log.e("MainActivity", "Error reading notification flag: " + databaseError.getMessage());
            }
        });
    }


    private void openFacebookPage() {
        // Replace with your developer's Facebook page URL
        String facebookUrl = "https://www.facebook.com/CJayzzz.com.ph"; // Change this to the actual URL
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(facebookUrl));
        startActivity(intent);
    }

    private void showLoginDialog() {

        // Get the SharedPreferences instance
        SharedPreferences sharedPreferences = getSharedPreferences("LoginPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);

        // If the user is already logged in, do not show the login form
        if (isLoggedIn) {
            // User is already logged in, no need to show the dialog
            return;
        }

        // Inflate the login form layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View loginView = inflater.inflate(R.layout.loginform, null);

        // Create an AlertDialog for the login form
        AlertDialog loginDialog = new AlertDialog.Builder(this)
                .setView(loginView)
                .setCancelable(false)
                .create();

        loginDialog.show();

        // Handle the login button click
        Button buttonLogin = loginView.findViewById(R.id.buttonLogin);
        TextView userIdInput = loginView.findViewById(R.id.editTextUserId);
        TextView passwordInput = loginView.findViewById(R.id.editTextPassword);

        // Find the Developer button inside the loginView
        Button developerButton = loginView.findViewById(R.id.Developer); // Changed to loginView


        if (developerButton != null) {
            developerButton.setOnClickListener(v -> openFacebookPage());
        } else {
            Toast.makeText(this, "Developer button not found", Toast.LENGTH_SHORT).show();
        }




        buttonLogin.setOnClickListener(v -> {
            // Get user input
            String userId = userIdInput.getText().toString();
            String password = passwordInput.getText().toString();

            // Check if inputs are empty
            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter both User ID and Password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Query Firebase to check for userID and password
            DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference().child("users").child(userId);
            usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String storedPassword = dataSnapshot.child("password").getValue(String.class);
                        if (storedPassword != null && storedPassword.equals(password)) {
                            // Successful login, store login status
                            sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();
                            sharedPreferences.edit().putString("userId", userId).apply();
                            Toast.makeText(MainActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();

                            // Dismiss the login dialog
                            loginDialog.dismiss();
                            createNotification("Login Successful", "Welcome back, " + userId + "!");
                        } else {
                            // Password mismatch
                            Toast.makeText(MainActivity.this, "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // User ID does not exist
                        Toast.makeText(MainActivity.this, "User ID does not exist", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle possible errors
                    Toast.makeText(MainActivity.this, "Error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void createNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // For API level 26 and above, create a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("login_channel", "Login Notifications",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        // Create the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "login_channel")
                .setSmallIcon(R.drawable.logo) // Replace with your notification icon
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // Show the notification
        notificationManager.notify(1, builder.build());
    }




    private void showInternetConnectionDialog() {

        if (overlayLayout != null) {
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
        int animationResId; // No need to initialize here
        TextView textCheck = alertDialog.findViewById(R.id.DescNet);
        TextView Title = alertDialog.findViewById(R.id.CheckingTitle);

        if (isConnected) {
            Title.setText("CONNECTED");
            message = "Internet connection is OK!";
            animationResId = R.raw.check;
            Title.setTextColor(Color.parseColor("#228B22"));

            DatabaseReference switchRef = FirebaseDatabase.getInstance().getReference().child("switch_button");
            switchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String switchState = dataSnapshot.getValue(String.class);
                        // Handle switch state, e.g., update UI based on the switch state
                        Log.d("MainActivity", "Switch state from Firebase: " + switchState);

                        // Check if the switch state is "on"
                        if ("on".equals(switchState)) {


                        } else {
                            // Optional: Handle the case when the switch is off
                            Log.d("MainActivity", "Switch is off");
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    // Handle onCancelled event
                }
            });
        } else {
            message = "No internet connection available!";
            animationResId = R.raw.error;
            // Update message for connection lost

            if (textCheck != null) {
                textCheck.setText("We'll activate your Bluetooth to maintain a continuous connection.");
                Title.setText("OPPPS CONNECTION LOST!");
                Title.setTextColor(Color.parseColor("#800020"));



            }
            // Delay before calling the method to switch on Bluetooth
            new Handler().postDelayed(() -> {
                isSwitchChecked();
            }, 10200); // 5 seconds delay

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
            int delay = isConnected ? 1000 : 10200; // Adjust the delay time based on the connection status
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
