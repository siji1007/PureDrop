package com.example.puredrop;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class controll_fragment extends Fragment {
    private Runnable dataFetchRunnable;

    private DatabaseReference dataRef;
    private TableLayout tableLayout;
    private Handler handler;
    private final int INTERVAL = 1000; // Refresh interval in milliseconds
    private int rowCount = 0; // Track row count

    private LottieAnimationView startBtn;
    private boolean startBtnOn = false;

    private Switch aSwitch;

    private BluetoothAdapter bluetoothAdapter;

    private static final int REQUEST_BLUETOOTH_PERMISSION = 1001;
    private BluetoothSocket bluetoothSocket;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.controll_fragment, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        handler = new Handler(Looper.getMainLooper());
        startBtn = rootView.findViewById(R.id.start_btn); // Replace R.id.start_btn with your actual LottieAnimationView ID
        aSwitch = rootView.findViewById(R.id.SwitchBtn);

        String[] headers = {"Time", "TDS", "pH Level", "Turbidity"};

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

        TextView SwitchText = rootView.findViewById(R.id.Bluetooth);
        aSwitch.getThumbDrawable().setTint(ContextCompat.getColor(getContext(), R.color.darkBlue));


        DatabaseReference switchRef = FirebaseDatabase.getInstance().getReference().child("switch_button");
        switchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String switchState = dataSnapshot.getValue(String.class);
                    if ("on".equals(switchState)) {
                        startBtnOn = false;
                        startTextView.setText("OFF");
                        fetchDataAndUpdateTable();
                        startDataFetchScheduler();

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle onCancelled event
            }
        });


        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!startBtnOn) { // If the start button is off
                    // Check the current state of the switch
                    DatabaseReference switchRef = FirebaseDatabase.getInstance().getReference().child("switch_button");
                    switchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String switchState = dataSnapshot.getValue(String.class);
                                if ("on".equals(switchState)) {
                                    // If the switch is on, do not allow starting the button
                                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                                    startBtn.playAnimation();
                                    startBtnOn = false;
                                    updateSwitchStateInFirebase("off");
                                    handler.removeCallbacks(dataFetchRunnable); // Stop data retrieval
                                    tableLayout.removeAllViews(); // Clear table layout
                                    startTextView.setText("START");
                                    Toast.makeText(getContext(), "Switch off", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If the switch is off, start the button
                                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                                    startBtn.playAnimation();
                                    startBtnOn = true;
                                    long currentTimeMillis = System.currentTimeMillis();
                                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
                                    String currentDateTime = sdf.format(new Date(currentTimeMillis));
                                    // addDataToFirebase(createRow(currentDateTime, "110", "60", "35", "7.0", "0.15", "25"));
                                    fetchDataAndUpdateTable();
                                    startDataFetchScheduler(); // Start data retrieval
                                    startTextView.setText("OFF");
                                    Toast.makeText(getContext(), "Switch on", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            // Handle onCancelled event
                        }
                    });
                } else {
                    // If the start button is already on, do nothing or show a message
                    // You can add your logic here if needed
                }
            }
        });

        dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node");

        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aSwitch.isChecked()) {
                    SwitchText.setText("ON");
                    SwitchText.setTextColor(Color.GREEN);
                    enableBluetooth();

                } else {
                    SwitchText.setText("OFF");
                    SwitchText.setTextColor(Color.RED);
                    // Disable Bluetooth
                    // disableBluetooth();
                }
            }
        });

        return rootView;
    }

    private void enableBluetooth() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN)
                    != PackageManager.PERMISSION_GRANTED) {
                // Request the BLUETOOTH_ADMIN permission
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.BLUETOOTH_ADMIN}, REQUEST_BLUETOOTH_PERMISSION);
            } else {
                // Permission already granted, start the activity to enable Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(enableBtIntent);
                // Now, you can show the dialog to list paired and available devices
                showBluetoothDeviceDialog();
            }
        } else {
            // Bluetooth is already enabled, show the dialog
            showBluetoothDeviceDialog();
        }
    }

    private void showBluetoothDeviceDialog() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH)
                == PackageManager.PERMISSION_GRANTED) {

            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter == null) {
                return;
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
            List<String> deviceNames = new ArrayList<>();
            final List<BluetoothDevice> devices = new ArrayList<>();
            for (BluetoothDevice device : pairedDevices) {
                deviceNames.add(device.getName() + "\n" + device.getAddress());
                devices.add(device);
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Paired Bluetooth Devices")
                    .setItems(deviceNames.toArray(new String[0]), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Get the selected device info string and pass it to connectToDevice
                            String deviceInfo = deviceNames.get(which);
                            connectToDevice(deviceInfo);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }


    private void connectToDevice(String deviceInfo) {
        // Split the deviceInfo string into name and address
        String[] parts = deviceInfo.split("\n");
        if (parts.length != 2) {
            // Invalid format, handle error (e.g., display a message)
            return;
        }
        String deviceName = parts[0];
        String deviceAddress = parts[1];

        // Get the Bluetooth device using the address
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);

        // Check Bluetooth permissions
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
            // Request permissions from the user
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN},
                    REQUEST_BLUETOOTH_PERMISSION);
        } else {
            // Permissions granted, proceed with connection
            try {
                // Create a socket and connect to the device using the SPP UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
                bluetoothSocket.connect();

                // Connection successful, show a toast message indicating successful connection
                Toast.makeText(getContext(), "Connected to " + deviceName, Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // Error occurred during connection, handle accordingly
                e.printStackTrace();
                // Show a toast message indicating connection failure
                Toast.makeText(getContext(), "Failed to connect to " + deviceName, Toast.LENGTH_SHORT).show();
                try {
                    // Close the socket if it was opened
                    if (bluetoothSocket != null) {
                        bluetoothSocket.close();
                    }
                } catch (IOException closeException) {
                    closeException.printStackTrace();
                }
            }
        }
    }



    public void fetchDataAndUpdateTable() {
        updateSwitchStateInFirebase("on");
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node")
                .child("2024").child("05");
        Log.d("MainActivity", "FETCH DATA TEST" );
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


    public TableRow createHeaderRow() {
        TableRow headerRow = new TableRow(getContext());
        String[] headers = {"Time", "TDS", "pH Level", "Turbidity"};
        //String[] headers = {"Time", "Gallons", "TDS", "EC", "pH Level", "Turbidity", "Temp"};
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(10, 10, 10, 10);
            headerRow.addView(headerText);
        }
        return headerRow;
    }


    public TableRow createDataRow(Map<String, String> rowData) {
        TableRow dataRow = new TableRow(getContext());

        // Ensure all columns are present even if data is missing
        String[] columnNames = {"Time", "TDS", "pH Level", "Turbidity"};

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





    public void startDataFetchScheduler() {
        dataFetchRunnable = new Runnable() {
            @Override
            public void run() {
                fetchDataAndUpdateTable();
                if (startBtnOn) {
                    startDataFetchScheduler();
                }
            }
        };
        handler.postDelayed(dataFetchRunnable, INTERVAL);
    }
}
