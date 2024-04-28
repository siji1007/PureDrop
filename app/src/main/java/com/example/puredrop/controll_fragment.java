package com.example.puredrop;

import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;



public class controll_fragment extends Fragment {

    LottieAnimationView startBtn;
    private boolean startBtnOn = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.controll_fragment, container, false);
        startBtn = rootView.findViewById(R.id.start_btn);

        TableLayout tableLayout = rootView.findViewById(R.id.tableLayout);
        String[] headers = {"Time", "Gallons", "TDS", "EC", "pH Level", "Turbidity", "Temp"};

        // Create a header row
        TableRow headerRow = new TableRow(getContext());
        for (String header : headers) {
            TextView headerText = new TextView(getContext());
            headerText.setText(header);
            headerText.setTextColor(Color.WHITE);
            headerText.setPadding(10, 10, 10, 10); // Adjust padding as neede
            headerRow.addView(headerText);
        }
        tableLayout.addView(headerRow);

        // Example data (replace this with your actual data)
        String[][] data = {
                {"2022-01-01", "100", "50", "30", "7.5", "0.1", "25"},
                {"2022-01-02", "120", "55", "32", "7.2", "0.2", "24"},
                // ADD HERE THE DATA FROM FIREBASE
        };

        // Create data rows
        for (String[] rowData : data) {
            TableRow dataRow = new TableRow(getContext());
            for (String cellData : rowData) {
                TextView cellText = new TextView(getContext());
                cellText.setText(cellData);
                cellText.setTextColor(Color.WHITE);
                cellText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                cellText.setPadding(0, 0, 0, 0); // Adjust padding as needed
                //cellText.setBackgroundResource(R.drawable.table_cell_background); // Optional: set a background drawable for cells
                dataRow.addView(cellText);
            }
            tableLayout.addView(dataRow);

        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (startBtnOn){
                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                    startBtn.playAnimation();
                    startBtnOn = false;
                }else{
                    startBtn.setMinAndMaxProgress(0.0f, 1.0f);
                    startBtn.playAnimation();
                    startBtnOn = true;
                }
            }
        });
        return rootView;






    }
}
