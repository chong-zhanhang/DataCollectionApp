package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.mindcheckdatacollectionapp.R;

public class RealMainActivity extends AppCompatActivity {

    private EditText testingTextField;
    private Button testingButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_main);

        testingTextField = findViewById(R.id.testingTextField);
    }
}