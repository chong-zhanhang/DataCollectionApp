package com.example.mindcheckdatacollectionapp.ui.theme;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.AppointmentsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.HomeFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.NotificationsFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.ProfileFragment;
import com.example.mindcheckdatacollectionapp.ui.theme.Fragments.VisualizationFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RealMainActivity extends AppCompatActivity {

    private EditText journalTextField;
    private RecyclerView journalRecyclerView;
    private ArrayList<String> journalList = new ArrayList<>();
    private JournalAdapter journalAdapter;
    private Button submit;
    private Button moreInfo;
    private BottomNavigationView bottomNavigationView;
    private Fragment selectorFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_real_main);

        createQuestionnaireNotificationChannel();

        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.nav_home) {
                    selectorFragment = new HomeFragment();
                } else if (itemId == R.id.nav_profile) {
                    //selectorFragment = new ProfileFragment();
                    selectorFragment = null;
                    startActivity(new Intent(RealMainActivity.this, ProfileActivity.class));
                } else if (itemId == R.id.nav_appointment) {
                    selectorFragment = new AppointmentsFragment();
                } else if (itemId == R.id.nav_noti) {
                    selectorFragment = new NotificationsFragment();
                } else if (itemId == R.id.nav_graph) {
                    selectorFragment = new VisualizationFragment();
                }

                if (selectorFragment != null) {
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectorFragment).commit();
                }

                return true;
            }
        });

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.d("DEBUG", "Fetching FCM registration token failed", task.getException());
                            Toast.makeText(RealMainActivity.this, "Fetching FCM registration token failed", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and toast
                        //String msg = getString(R.string.msg_token_fmt, token);
                        Log.d("DEBUGTOKEN", token);
                    }
                });

        journalTextField = findViewById(R.id.journalTextField);
        submit = findViewById(R.id.submitButton);
        moreInfo = findViewById(R.id.moreInfo);
        journalRecyclerView = findViewById(R.id.journalRecyclerView);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();

        journalAdapter = new JournalAdapter(journalList);
        journalRecyclerView.setAdapter(journalAdapter);
        journalRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        submit.setOnClickListener(v -> {
            String journalText = journalTextField.getText().toString();
            int wordCount = countWords(journalText);
            int maxWordLimit = 500;

            if (!journalText.isEmpty() && currentUser != null) {
                if (wordCount > maxWordLimit) {
                    Toast.makeText(RealMainActivity.this, "Word Limit Exceeded: Please write within 500 words", Toast.LENGTH_SHORT).show();
                } else {
                    Map<String, Object> journalEntry = new HashMap<>();
                    journalEntry.put("text", journalText);
                    journalEntry.put("timestamp", System.currentTimeMillis());
                    journalEntry.put("userID", currentUser.getUid());

                    db.collection("journal")
                            .add(journalEntry)
                            .addOnSuccessListener(documentReference -> {
                                Toast.makeText(RealMainActivity.this, "Journal Saved", Toast.LENGTH_SHORT).show();
                                journalTextField.setText("");
                                fetchJournals();
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(RealMainActivity.this, "Error saving journal.", Toast.LENGTH_SHORT).show();
                            });
                }
            } else {
                Toast.makeText(RealMainActivity.this, "Cannot save journal to database", Toast.LENGTH_SHORT).show();
            }
        });

        moreInfo.setOnClickListener(v -> {
            startActivity(new Intent(RealMainActivity.this, MoreInfoActivity.class));
        });

        fetchJournals();
    }

    public void createQuestionnaireNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Questionnaire Time Reminder";
            String description = "Remind user to fill in questionnaire";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel("NQ1", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private void fetchJournals() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            String currentUserID = currentUser.getUid();
            db.collection("journal")
                    .whereEqualTo("userID", currentUserID)
                    .orderBy("timestamp", Query.Direction.DESCENDING)
                    .limit(5)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        journalList.clear();
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String journalText = document.getString("text");
                            journalList.add(journalText);
                        }
                        journalAdapter.notifyDataSetChanged();
                    })
                    .addOnFailureListener(e -> Log.d("DEBUG", "Error fetching journals" + e));
        } else {
            Log.d("DEBUG", "No user is logged in");
            Toast.makeText(RealMainActivity.this, "No user is logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private int countWords(String journalText) {
        String trimmedText = journalText.trim();
        if (trimmedText.isEmpty()) {
            return 0;
        }
        return trimmedText.split("\\s+").length;
    }
}