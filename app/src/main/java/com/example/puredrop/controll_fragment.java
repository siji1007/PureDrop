package com.example.puredrop;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class controll_fragment extends Fragment {

    private DatabaseReference dataRef;
    private TableLayout tableLayout;
    private Handler handler;
    private final int INTERVAL = 1000; // Refresh interval in milliseconds
    private int rowCount = 0; // Track row count

    private LottieAnimationView startBtn;
    private boolean startBtnOn = false;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.controll_fragment, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        handler = new Handler(Looper.getMainLooper());
        startBtn = rootView.findViewById(R.id.start_btn); // Replace R.id.start_btn with your actual LottieAnimationView ID

        String[] headers = {"Time", "Gallons", "TDS", "EC", "pH Level", "Turbidity", "Temp"};

        // Create a header row
        TableRow headerRow = new TableRow(getContext());
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(10, 10, 10, 10); // Adjust padding as needed
            headerRow.addView(headerText);
        }
        tableLayout.addView(headerRow);

        // Initialize Firebase Database reference
        TextView startTextView = rootView.findViewById(R.id.start_id);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startBtnOn) {
                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                    startBtn.playAnimation();
                    startBtnOn = false;
                    updateSwitchStateInFirebase("off");
                    handler.removeCallbacksAndMessages(null); // Stop data retrieval
                    tableLayout.removeAllViews(); // Clear table layout

                    startTextView.setText("OFF");
                    Toast.makeText(getContext(), "Switch off", Toast.LENGTH_SHORT).show();




                } else {
                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                    startBtn.playAnimation();
                    startBtnOn = true;
                    long currentTimeMillis = System.currentTimeMillis();
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                    String currentDateTime = sdf.format(new Date(currentTimeMillis));
                    updateSwitchStateInFirebase("on");
                    addDataToFirebase(createRow(currentDateTime, "110", "60", "35", "7.0", "0.15", "25"));
                    fetchDataAndUpdateTable();
                    startDataFetchScheduler(); // Start data retrieval
                    startTextView.setText("START");
                    Toast.makeText(getContext(), "Switch on", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node");


        return rootView;
    }

    public void fetchDataAndUpdateTable() {
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node")
                .child("2024").child("05");

        dataRef.orderByKey().limitToLast(5).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayout.removeAllViews(); // Clear existing data
                tableLayout.addView(createHeaderRow()); // Add header row
                rowCount = 0; // Reset row count

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (rowCount >= 5) break; // Stop adding rows after reaching 5 rows
                    Map<String, String> rowData = (Map<String, String>) snapshot.getValue();
                    if (rowData != null) {
                        tableLayout.addView(createDataRow(rowData)); // Add data row
                        rowCount++;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }



    private Map<String, String> createRow(String time, String gallons, String tds, String ec, String phLevel, String turbidity, String temp) {
        Map<String, String> rowData = new HashMap<>();
        rowData.put("Time", time);
        rowData.put("Gallons", gallons);
        rowData.put("TDS", tds);
        rowData.put("EC", ec);
        rowData.put("pH Level", phLevel);
        rowData.put("Turbidity", turbidity);
        rowData.put("Temp", temp);
        return rowData;
    }

    private void addDataToFirebase(Map<String, String> rowData) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd_HH:mm:ss_SSS", Locale.getDefault());
        String key = sdf.format(new Date()); // Generate a unique key for each row
        if (key != null) {
            // Check if the generated key's date is greater than the latest date in Firebase
            dataRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        String latestKey = dataSnapshot.getChildren().iterator().next().getKey();
                        if (latestKey != null && key.compareTo(latestKey) > 0) {
                            // The generated key's date is greater than the latest date in Firebase
                            dataRef.child(key).setValue(rowData)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("MainActivity", "Data added to Firebase: " + rowData.toString());
                                            // Optional: Update UI or handle success
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("MainActivity", "Failed to add data to Firebase: " + e.getMessage());
                                            // Optional: Handle failure
                                        }
                                    });
                        } else {
                            Log.d("MainActivity", "Generated key's date is not greater than the latest date in Firebase.");
                            // Handle case where generated key's date is not greater than the latest date in Firebase
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Log.e("MainActivity", "Failed to read latest date from Firebase: " + databaseError.getMessage());
                    // Handle onCancelled event
                }
            });
        }
    }



    private void updateSwitchStateInFirebase(String state) {
        DatabaseReference switchRef = FirebaseDatabase.getInstance().getReference().child("switch_button");

        switchRef.setValue(state)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("MainActivity", "Switch state updated in Firebase: " + state);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MainActivity", "Failed to update switch state in Firebase: " + e.getMessage());
                    }
                });
    }

    private void fetchLatestTimeAndUpdateTable() {
        DatabaseReference timeRef = FirebaseDatabase.getInstance().getReference()
                .child("your_data_node").child("2024").child("05").child("latest_time");

        timeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String latestTime = dataSnapshot.getValue(String.class);
                if (latestTime != null) {
                    // Assuming your tableLayout already exists
                    tableLayout.removeAllViews(); // Clear existing data
                    tableLayout.addView(createHeaderRow()); // Add header row

                    Map<String, String> rowData = new HashMap<>();
                    rowData.put("Time", latestTime); // Assuming "Time" is the key for the latest time data
                    tableLayout.addView(createDataRow(rowData)); // Add data row

                    rowCount = 1; // Set row count to 1 since we added a new row
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });
    }



    private TableRow createHeaderRow() {
        TableRow headerRow = new TableRow(getContext());
        String[] headers = {"Time", "Gallons", "TDS", "EC", "pH Level", "Turbidity", "Temp"};
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(10, 10, 10, 10);
            headerRow.addView(headerText);
        }
        return headerRow;
    }

    private TableRow createDataRow(Map<String, String> rowData) {
        TableRow dataRow = new TableRow(getContext());

        // Ensure all columns are present even if data is missing
        String[] columnNames = {"Time", "Gallons", "TDS", "EC", "pH Level", "Turbidity", "Temp"};

        for (String columnName : columnNames) {
            TextView cellText = new TextView(getContext());
            String value = rowData.containsKey(columnName) ? rowData.get(columnName) : "";
            cellText.setText(value);
            cellText.setTextColor(Color.WHITE);
            cellText.setPadding(10, 10, 10, 10);
            dataRow.addView(cellText);
        }

        return dataRow;
    }

    private void startDataFetchScheduler() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                fetchDataAndUpdateTable();
                startDataFetchScheduler(); // Schedule the next fetch
            }
        }, INTERVAL);
    }
}
