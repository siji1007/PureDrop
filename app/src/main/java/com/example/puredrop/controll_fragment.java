package com.example.puredrop;
import android.Manifest;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.pm.PackageManager;
import android.graphics.Color;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
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
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import android.net.wifi.WifiManager;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class controll_fragment extends Fragment {
    PieChart pieChart;
    List<PieChart> pieEntryList;
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
    //COUNTDOWN
    private static final long COUNTDOWN_INTERVAL = 1000; // Countdown interval in milliseconds
    private static final long START_TIME = 30000; // Start time for the countdown in milliseconds
    private CountDownTimer countDownTimer;
    private TextView countdownTextView;
    private WifiStateReceiver wifiStateReceiver;

    private boolean chartVisible = false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.controll_fragment, container, false);
        tableLayout = rootView.findViewById(R.id.tableLayout);

        pieEntryList=new ArrayList<>();

        pieChart = rootView.findViewById(R.id.ChartShow);


        handler = new Handler(Looper.getMainLooper());
        startBtn = rootView.findViewById(R.id.start_btn); // Replace R.id.start_btn with your actual LottieAnimationView ID
        aSwitch = rootView.findViewById(R.id.SwitchBtn);

        String[] headers = {"Time", "TDS", "EC", "Turbidity", "pH Level", "Temperature"};

        // Create a header row
        TableRow headerRow = new TableRow(getContext());
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(8, 8, 8, 8); // Adjust padding as needed
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
                        startBtnOn = !startBtnOn; // Toggle the state of the start button
                        if (startBtnOn) { // If the start button is turned on
                            fetchDataAndUpdateTable(); // Fetch data and update table
                            startDataFetchScheduler(); // Start data fetch scheduler
                            startTextView.setText("OFF");

                        } else { // If the start button is turned off
                            handler.removeCallbacks(dataFetchRunnable); // Stop data retrieval
                            tableLayout.removeAllViews(); //Clear table layout
                            startTextView.setText("START");

                        }

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
                DatabaseReference switchRef = FirebaseDatabase.getInstance().getReference().child("switch_button");
                switchRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (!isConnectedToInternet()) {
                            // Show a toast indicating no internet connection
                            Toast.makeText(getContext(), "No internet connection", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (dataSnapshot.exists()) {
                            String switchState = dataSnapshot.getValue(String.class);
                            if ("on".equals(switchState)) {
                                // If the switch is on, turn it off
                                startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                                startBtn.playAnimation();
                                startBtnOn = false;
                                updateSwitchStateInFirebase("off");
                                tableLayout.removeAllViews(); // Clear table layout
                                handler.removeCallbacks(dataFetchRunnable); // Stop data retrieval
                                startTextView.setText("START");
                                startBtn.setEnabled(false); // Disable the start button
                                startCountdownTimer(); //
                            } else {
                                // If the switch is off or not set, turn it on
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
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        // Handle onCancelled event
                    }


                    private boolean isConnectedToInternet() {
                        Context context = getContext();
                        if (context != null) {
                            // Get the connectivity manager
                            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
                            if (connectivityManager != null) {
                                // Get the network info
                                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                                // Check if there is a network connection and if the device is connected
                                return networkInfo != null && networkInfo.isConnected();
                            }
                        }
                        // Return false if there is no context or connectivity manager
                        return false;
                    }


                });
            }
        });





        dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node");
        WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        aSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (aSwitch.isChecked()) {
                    SwitchText.setText("ON");
                    SwitchText.setTextColor(Color.GREEN);
                    enableBluetooth();
                    wifiManager.setWifiEnabled(false);

                } else {
                    SwitchText.setText("OFF");
                    SwitchText.setTextColor(Color.RED);
                }
            }
        });


        Button detailsButton = rootView.findViewById(R.id.DetailsShow);
        detailsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                togglePieChartVisibility(); // Toggle visibility when the button is clicked
            }
        });


        return rootView;
    }





    private void togglePieChartVisibility() {
        PieChart pieChart = getView().findViewById(R.id.ChartShow);
        Button chartButton = getView().findViewById(R.id.DetailsShow);

        if (pieChart.getVisibility() == View.VISIBLE) {
            // If visible, hide the PieChart
            chartVisible = false; // Update chart visibility state
            chartButton.setText("CHART"); // Update button text to "SHOW"
            pieChart.animateXY(2000, 2000, Easing.EaseInOutQuad, Easing.EaseInOutQuad);
            pieChart.setVisibility(View.GONE);
        } else {
            pieChart.setVisibility(View.VISIBLE); // If hidden, show the PieChart
            chartVisible = true; // Update chart visibility state
            setupPieChart(); // Set up the PieChart when it becomes visible
            chartButton.setText("HIDE"); // Update button text to "HIDE"
        }
    }

    private void setupPieChart() {
        if (chartVisible) {
            // Set up the PieChart when it becomes visible
            PieChart pieChart = getView().findViewById(R.id.ChartShow);
            List<PieEntry> pieEntryList = new ArrayList<>();

            // Assuming you have the sensor data values available here
            float tdsValue = 300; // Example value
            float phLevel = 7.0f; // Example value
            float ecValue = 1500; // Example value

            // Calculate safe and bad values based on sensor data
            float safeValue = 0;
            float badValue = 0;

            if (isSafeValue(tdsValue, phLevel, ecValue)) {
                safeValue = 100; // 100% Safe
            } else {
                badValue = 100; // 100% Bad
            }

            // Add PieEntry objects with values
            pieEntryList.add(new PieEntry(safeValue, "SAFE"));
            pieEntryList.add(new PieEntry(badValue, "BAD"));

            String pieDataSetLabel;
            if (safeValue > badValue) {
                pieDataSetLabel = "Ready for Drink";
            } else {
                pieDataSetLabel = "Please refilter the water";
            }


            // Create a PieDataSet and set specific colors for "SAFE" and "BAD"
            PieDataSet pieDataSet = new PieDataSet(pieEntryList, pieDataSetLabel);
            pieDataSet.setValueTextColor(Color.BLACK);
            pieDataSet.setValueTextSize(10f);

            // Set colors for "SAFE" and "BAD"
            List<Integer> colors = new ArrayList<>();
            colors.add(Color.parseColor("#0E87A1")); // Color for "SAFE" (using hexadecimal color code)
            colors.add(Color.RED);  // Color for "BAD"
            pieDataSet.setColors(colors);

            PieData pieData = new PieData(pieDataSet);
            pieChart.setData(pieData);

            pieChart.getDescription().setEnabled(true);
            pieChart.getDescription().setText("Drinkability Status"); // Set description label text
            pieChart.getDescription().setTextSize(10f);
            pieChart.getDescription().setTextColor(Color.BLACK);

            pieChart.invalidate(); // Refresh the chart
            // Animation to spin the chart slowly clockwise
            pieChart.animateXY(2000, 2000); // Adjust duration as per your preference
        }
    }

    private boolean isSafeValue(float tds, float ph, float ec) {
        return (tds >= 50 && tds <= 250) &&
                (ph >= 6.5 && ph <= 8.0) &&
                (ec >= 0 && ec <= 2499);
    }

    private void startCountdownTimer() {
        // Create a countdown timer for 3 seconds
        new CountDownTimer(3000, 1000) {
            public void onTick(long millisUntilFinished) {
                // Do nothing on tick
            }

            public void onFinish() {
                // When the countdown finishes, enable the start button
                startBtn.setEnabled(true);
                tableLayout.removeAllViews(); // Clear table layout
                updateSwitchStateInFirebase("off");
                Toast.makeText(getContext(), "Button is disable in 3s", Toast.LENGTH_SHORT).show();

            }
        }.start();
    }


    public void fetchDataAndUpdateTable() {
        updateSwitchStateInFirebase("on");
        DatabaseReference dataRef = FirebaseDatabase.getInstance().getReference().child("your_data_node")
                .child("2024").child("05");
        Log.d("MainActivity", "FETCH DATA TEST" );
        dataRef.orderByKey().limitToLast(6).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                tableLayout.removeAllViews(); // Clear existing data
                tableLayout.addView(createHeaderRow()); // Add header row
                rowCount = 0; // Reset row count

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (rowCount >= 6) break; // Stop adding rows after reaching 5 rows
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
        //String[] headers = {"Time", "TDS", "pH Level", "Turbidity"};
        String[] headers = {"Time", "TDS", "EC", "Turbidity", "pH Level", "Temperature"};
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(8, 8, 8, 8);
            headerRow.addView(headerText);
        }
        return headerRow;
    }




    public TableRow createDataRow(Map<String, String> rowData) {
        TableRow dataRow = new TableRow(getContext());
        // Ensure all columns are present even if data is missing
        //String[] columnNames = {"Time", "TDS", "pH Level", "Turbidity"};
        String[] columnNames = {"Time", "TDS", "EC", "Turbidity", "pH Level", "Temperature"};
        for (String columnName : columnNames) {
            TextView cellText = new TextView(getContext());
            String value = rowData.containsKey(columnName) ? rowData.get(columnName) : "";
            cellText.setText(value);
            cellText.setTextColor(Color.WHITE);
            cellText.setPadding(8, 8, 8, 8);
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

    //BLUETOOTH CODE HERE

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
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED) {
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
                            // Get the selected device info
                            BluetoothDevice selectedDevice = devices.get(which);
                            // Pass the selected device to connectToDevice
                            connectToDevice(selectedDevice);
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }

    private void connectToDevice(BluetoothDevice device) {
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
                // Get the UUID of the selected device
                UUID deviceUUID = device.getUuids()[0].getUuid();
                Log.d("Bluetooth", "Selected Device UUID: " + deviceUUID.toString());

                // Create a socket and connect to the device using its UUID
                bluetoothSocket = device.createRfcommSocketToServiceRecord(UUID.fromString(deviceUUID.toString()));
                bluetoothSocket.connect();

                // Connection successful, show a toast message indicating successful connection
                Toast.makeText(getContext(), "Connected to " + device.getName(), Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                // Error occurred during connection, handle accordingly
                e.printStackTrace();
                // Show a toast message indicating connection failure
                Toast.makeText(getContext(), "Failed to connect to " + device.getName(), Toast.LENGTH_SHORT).show();
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
}


