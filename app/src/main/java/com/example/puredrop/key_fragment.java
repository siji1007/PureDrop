package com.example.puredrop;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class key_fragment extends Fragment {

    private DatabaseReference databaseReference; // Reference to your Firebase Database
    private Button Button;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.key_fragment, container, false);

        Button btn_logout = view.findViewById(R.id.logout);

        // Fetch the user ID from SharedPreferences
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE);
        String userId = sharedPreferences.getString("userId", null); // Default to null if not found

        // Display user ID in a TextView
        TextView userIdTextView = view.findViewById(R.id.userID); // Replace with your actual TextView ID
        if (userId != null) {
            userIdTextView.setText(userId); // Set the text to show user ID
            fetchDatePurchased(userId); // Fetch the purchase date from Firebase
        } else {
            userIdTextView.setText("No user logged in");
        }

        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_logout_f();
            }
        });


        return view;
    }


    private void btn_logout_f() {
        // Access SharedPreferences to modify the login status
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("LoginPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // Set the 'isLoggedIn' flag to false to indicate the user is logged out
        editor.putBoolean("isLoggedIn", false);

        // Optionally clear the userId or any other saved data
        editor.remove("userId");

        // Commit the changes to SharedPreferences
        editor.apply();

        // Show a Toast message to confirm logout
        Toast.makeText(getContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Reset the UI to reflect the logged-out state
        updateUIForLogout();

        // Restart the application (MainActivity or the starting activity)
        restartApplication();
    }

    private void restartApplication() {
        // Create an intent to launch the MainActivity (or any activity that should be the entry point after logout)
        Intent intent = new Intent(getActivity(), MainActivity.class);
        // Clear the activity stack so that pressing the back button won't return to the previous activity
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        // Optionally, finish the current activity (logout activity)
        getActivity().finish();
    }


    private void updateUIForLogout() {
        // Example: Reset TextViews or any other UI elements to reflect logged-out state
        TextView userIdTextView = getView().findViewById(R.id.userID);
        if (userIdTextView != null) {
            userIdTextView.setText("No user logged in");
        }

        // You can reset or hide any elements that should not be shown when the user is logged out
        Button btn_logout = getView().findViewById(R.id.logout);
        if (btn_logout != null) {
            btn_logout.setVisibility(View.GONE);  // Hide logout button or replace with a login button, for example
        }

        // You can refresh any other fragments or UI elements if necessary (like replacing fragments, etc.)
        // For example, to replace the fragment with the login screen:
        // Fragment loginFragment = new LoginFragment(); // Replace with your actual login fragment
        // FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        // transaction.replace(R.id.fragment_container, loginFragment);
        // transaction.commit();

        // Optionally, you can manually reset the activity UI by clearing user data from the fragment
        // so the activity looks like it's logged out (without reloading the activity or starting a new one).
    }



    private void fetchDatePurchased(String userId) {
        databaseReference = FirebaseDatabase.getInstance().getReference("users").child(userId);

        // Fetch the Date_Purchased value from Firebase
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String datePurchased = dataSnapshot.child("Date_Purchased").getValue(String.class);

                    // Display Date_Purchased in another TextView
                    TextView datePurchasedTextView = getView().findViewById(R.id.Purchased_Date); // Replace with your actual TextView ID
                    if (datePurchased != null) {
                        datePurchasedTextView.setText(datePurchased);
                    } else {
                        datePurchasedTextView.setText("No purchase date found");
                    }
                } else {
                    Toast.makeText(getContext(), "User not found in Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to read data from Firebase: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
