package com.example.puredrop;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.fragment.app.Fragment;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import androidx.cardview.widget.CardView;

public class home_fragment extends Fragment {
    private VideoView videoView;
    private FrameLayout frameLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_fragment, container, false);

        videoView = view.findViewById(R.id.videoView);
        frameLayout = view.findViewById(R.id.frameLayout);



        CardView turbidity = view.findViewById(R.id.TURBIDITY_SENSOR);
        CardView PhSensor = view.findViewById(R.id.PH_SENSOR);
        CardView ECSensor = view.findViewById(R.id.EC_CENSOR);
        CardView TDSSensor = view.findViewById(R.id.TDS_SENSOR);

        // Find the LottieAnimationView by its ID
        LottieAnimationView anim = view.findViewById(R.id.capAnimation);

        // Set loop mode to LOOP
        anim.setRepeatCount(LottieDrawable.INFINITE);
        // Start the animation
        anim.playAnimation();
        // Set an OnClickListener to the CardView
        turbidity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Display a dialog with the message "TURBIDITY SENSOR"
                showTurbiditySensorDialog();
            }
        });

        PhSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPhsensorDialog();
            }
        });

        ECSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showECSensor();
            }
        });
        TDSSensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTdsSensor();
            }
        });



        return view;
    }

    private void showTurbiditySensorDialog() {
        // Ensure the FrameLayout and VideoView are visible
        frameLayout.setVisibility(View.VISIBLE);  // Make sure the FrameLayout is visible
        videoView.setVisibility(View.VISIBLE);    // Ensure the VideoView is visible

        // Set the video path (replace 'turbidity' with your actual video file name)
        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.tubidity);
        videoView.setVideoURI(videoUri);

        // Set up an OnPreparedListener to start the video once it's ready
        videoView.setOnPreparedListener(mp -> videoView.start()); // Start the video once it's prepared

        // Create the AlertDialog for sensor info
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setMessage("The turbidity sensor measures the cloudiness or haziness of a fluid caused by large particles that are generally invisible to the naked eye. It is commonly used in water quality monitoring to assess the clarity of water, which can indicate the presence of suspended solids, sediment, or other contaminants.");

        builder.setPositiveButton("OK", (dialog, id) -> {
            // Dismiss the dialog and stop the video when done
            dialog.dismiss();

          // Hide the video after the dialog is dismissed
        });
        frameLayout.setVisibility(View.VISIBLE);

        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showPhsensorDialog(){
        frameLayout.setVisibility(View.VISIBLE);  // Make sure the FrameLayout is visible
        videoView.setVisibility(View.VISIBLE);    // Ensure the VideoView is visible

        // Set the video path (replace 'turbidity' with your actual video file name)
        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.ph_level);
        videoView.setVideoURI(videoUri);

        // Set up an OnPreparedListener to start the video once it's ready
        videoView.setOnPreparedListener(mp -> videoView.start());

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set the message for the dialog
        builder.setMessage("The purpose of the pH sensor is to measure the acidity or alkalinity of a solution by determining the concentration of hydrogen ions (H+) in the solution. It is commonly used in various industries such as water quality monitoring, chemical processing, aquaculture, and biotechnology.");
        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
        frameLayout.setVisibility(View.VISIBLE);
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void showECSensor(){
        frameLayout.setVisibility(View.VISIBLE);  // Make sure the FrameLayout is visible
        videoView.setVisibility(View.VISIBLE);    // Ensure the VideoView is visible

        // Set the video path (replace 'turbidity' with your actual video file name)
        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.ec_sensor);
        videoView.setVideoURI(videoUri);

        // Set up an OnPreparedListener to start the video once it's ready
        videoView.setOnPreparedListener(mp -> videoView.start());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set the message for the dialog
        builder.setMessage("The purpose of the EC sensor, also known as an Electrical Conductivity sensor, is to measure the ability of a solution to conduct electrical current. This measurement is indicative of the concentration of ions, such as salts or minerals, dissolved in the solution. EC sensors are commonly used in hydroponics, agriculture, water quality monitoring, and environmental research to assess the nutrient levels and salinity of water or nutrient solutions.");
        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
        frameLayout.setVisibility(View.VISIBLE);
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showTdsSensor(){
        frameLayout.setVisibility(View.VISIBLE);  // Make sure the FrameLayout is visible
        videoView.setVisibility(View.VISIBLE);    // Ensure the VideoView is visible

        // Set the video path (replace 'turbidity' with your actual video file name)
        Uri videoUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.tds_sensor);
        videoView.setVideoURI(videoUri);

        // Set up an OnPreparedListener to start the video once it's ready
        videoView.setOnPreparedListener(mp -> videoView.start());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        // Set the message for the dialog
        builder.setMessage("The purpose of the TDS sensor, also known as Total Dissolved Solids sensor, is to measure the total amount of dissolved solids in a liquid. These dissolved solids can include salts, minerals, metals, and other substances. TDS sensors are commonly used in water quality monitoring to assess the overall purity of water, as higher TDS levels may indicate contamination or the presence of unwanted substances.");
        // Add a button to dismiss the dialog
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dismiss the dialog
                dialog.dismiss();
            }
        });
        frameLayout.setVisibility(View.VISIBLE);
        // Create and show the AlertDialog
        AlertDialog dialog = builder.create();
        dialog.show();



    }

}
