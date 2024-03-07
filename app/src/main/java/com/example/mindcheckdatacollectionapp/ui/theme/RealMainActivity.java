package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import com.example.mindcheckdatacollectionapp.R;
import com.google.firebase.auth.FirebaseAuth;

public class RealMainActivity extends AppCompatActivity {

    private EditText testingTextField;
    private Button testingButton;
    private Button logout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_main);

        testingTextField = findViewById(R.id.testingTextField);
        logout = findViewById(R.id.logout);

        logout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(RealMainActivity.this, LoginActivity.class));
            finish();
        });
    }
}