package com.example.mindcheckdatacollectionapp.ui.theme;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.mindcheckdatacollectionapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioGroup[] questionGroups = new RadioGroup[9];
    private Button submit;
    private static final String COLLECTION_NAME = "mobileUser";
    private static final String DOCUMENT_ID = retrieveDocument();
    private static final int DEPRESSION_THRESHOLD_SCORE = 5;

    private static String retrieveDocument() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        final String[] docId = new String[1];
        if (currentUser != null) {
            String userID = currentUser.getUid();
            db.collection(COLLECTION_NAME)
                    .whereEqualTo("userID", userID)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                for (DocumentSnapshot document : task.getResult()) {
                                    docId[0] = document.getId();
                                }
                            } else {
                                Log.e("DEBUG", "Cannot retrieve user ID");
                            }
                        }
                    });
        }
        return docId[0];
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        questionGroups[0] = findViewById(R.id.radioGroup1);
        questionGroups[1] = findViewById(R.id.radioGroup2);
        questionGroups[2] = findViewById(R.id.radioGroup3);
        questionGroups[3] = findViewById(R.id.radioGroup4);
        questionGroups[4] = findViewById(R.id.radioGroup5);
        questionGroups[5] = findViewById(R.id.radioGroup6);
        questionGroups[6] = findViewById(R.id.radioGroup7);
        questionGroups[7] = findViewById(R.id.radioGroup8);
        questionGroups[8] = findViewById(R.id.radioGroup9);

        submit = findViewById(R.id.submit);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int[] responses = new int[questionGroups.length];
                boolean allAnswered = true;

                for(int i = 0; i < questionGroups.length; i++){
                    int radioButtonID = questionGroups[i].getCheckedRadioButtonId();
                    View radioButton = questionGroups[i].findViewById(radioButtonID);
                    int idx = questionGroups[i].indexOfChild(radioButton);

                    if (idx == -1){
                        allAnswered = false;
                        break;
                    }else {
                        responses[i] = idx;
                    }
                }

                if (allAnswered) {
                    int sumOfScores = 0;
                    Map<String, Object> questionnaireData = new HashMap<>();

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    String userID = currentUser != null ? currentUser.getUid() : "anonymous";

                    for (int i = 0; i < responses.length; i++){
                        sumOfScores += responses[i];
                        questionnaireData.put("Question " + (i + 1), responses[i]);
                    }
                    questionnaireData.put("TotalScore", sumOfScores);
                    questionnaireData.put("UserID", userID);
                    questionnaireData.put("Timestamp", FieldValue.serverTimestamp());

                    updateUserDocument(sumOfScores);

                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    db.collection("questionnaire")
                                    .add(questionnaireData)
                                    .addOnSuccessListener(documentReference -> Toast.makeText(QuestionnaireActivity.this, "Data Stored Successfully with ID: " + documentReference, Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e -> Toast.makeText(QuestionnaireActivity.this, "Error storing data: " + e.getMessage(), Toast.LENGTH_SHORT).show());

                    Toast.makeText(QuestionnaireActivity.this, "Questionnaire Submitted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(QuestionnaireActivity.this, "Please answer all the questions.", Toast.LENGTH_SHORT).show();
                }
                startActivity(new Intent(QuestionnaireActivity.this, RealMainActivity.class));
                finish();
            }
        });
    }

    private void updateUserDocument(int sumOfScores) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userDocument = db.collection(COLLECTION_NAME).document(DOCUMENT_ID);

        boolean isDepressed = false;
        if (sumOfScores >= DEPRESSION_THRESHOLD_SCORE) {
            isDepressed = true;
        }

        Map<String, Object> updateDepressionStatus = new HashMap<>();
        updateDepressionStatus.put("isDepressed", isDepressed);

        //Check if "isDepressed" field exists in the document
        userDocument.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null){
                userDocument.set(updateDepressionStatus, SetOptions.merge());
            }
        });
    }
}