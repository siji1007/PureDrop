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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.key_fragment, container, false);

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



        return view;
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
