package com.example.resourcelocations;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class FoodShelterInformationActivity extends AppCompatActivity {

    public static TextView information;
    public static TextView title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_shelter_information);

        information = (TextView) findViewById(R.id.locationInfo);
        title = (TextView) findViewById(R.id.title);

        Intent intent = getIntent();
        FoodShelter foodShelter = intent.getParcelableExtra("FoodShelterObj");

        title.setText(foodShelter.getName());
        String available = "";
        if (foodShelter.getAvailable()) {
            available = "YES";
        }
        else {
            available = "NO";
        }
        information.setText("Description: " + foodShelter.getDescription() + "\n" + "Is this food shelter available?: " + available);
    }
}
