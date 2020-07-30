package com.example.resourcelocations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MedicalCenterInformationActivity extends AppCompatActivity {

    public static TextView information;
    public static TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medical_center_information);

        information = (TextView) findViewById(R.id.locationInfo);
        title = (TextView) findViewById(R.id.title);

        Intent intent = getIntent();
        MedicalCenter medicalCenter = intent.getParcelableExtra("MedicalCenterObj");

        title.setText(medicalCenter.getName());
        String available = "";
        if (medicalCenter.getAvailable()) {
            available = "YES";
        } else {
            available = "NO";
        }
        information.setText("Description: " + medicalCenter.getDescription() + "\n" + "Is this food shelter available?: " + available);
    }
}
