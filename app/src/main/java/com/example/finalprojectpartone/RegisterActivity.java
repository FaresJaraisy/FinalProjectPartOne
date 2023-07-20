package com.example.finalprojectpartone;

import static android.app.ProgressDialog.show;
import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import database.DBManager;
import database.FirebaseManager;

public class RegisterActivity extends AppCompatActivity {

    EditText editTextEmail, editTextPassword;
    Button buttonReg;
    FirebaseAuth mAuth;
    TextView textView;

    DBManager dbManager;
   // FirebaseManager firebaseManager;
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            dbManager = new DBManager(this, findViewById(android.R.id.content));
            dbManager.open();
            dbManager.emptyCommentTable();
            dbManager.emptyUsersTableSQLite();
            dbManager.emptyEventToUserConfirmationTableSQLite();
            dbManager.emptyEventsTableSQLite();

            try {
                // Sleep for 1 seconds (2000 milliseconds)
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // Handle the InterruptedException if needed
                e.printStackTrace();
            }
            FirebaseManager firebaseManager = new FirebaseManager(findViewById(android.R.id.content), dbManager);

            firebaseManager.fetchUsersDataFromFirebase();
            firebaseManager.fetchCommentsFromFirebaseAndInsertToSQL();
            firebaseManager.fillEventToUserConfirmationTableSQLite();
            firebaseManager.fillEventsTableSQLite();

            firebaseManager.startSyncWithSQLite();

            Intent intent = new Intent (getApplicationContext(), MainActivity.class);
            startActivity (intent);
            finish();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        dbManager = new DBManager(this, findViewById(android.R.id.content));
        dbManager.open();

        //firebaseManager = new FirebaseManager(findViewById(android.R.id.content), dbManager);

        //dbManager.emptyUsersTableSQLite();
        //firebaseManager.fetchUsersDataFromFirebase();

        mAuth= FirebaseAuth.getInstance();
        editTextEmail = findViewById(R.id.email);
        editTextPassword = findViewById(R.id.password);
        buttonReg = findViewById(R.id.btn_register);
        textView = findViewById(R.id.loginNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);
                finish ();
            }
        });

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email =String.valueOf(editTextEmail.getText());
                password =String.valueOf(editTextPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(RegisterActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(RegisterActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }
                //firebaseManager = new FirebaseManager(findViewById(android.R.id.content), dbManager);

                //dbManager.emptyUsersTableSQLite();
                //firebaseManager.fetchUsersDataFromFirebase();
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(RegisterActivity.this, "Account created.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "createUserWithEmail:success");
                                    dbManager.insertUser(email , password);
                                    Intent intent = new Intent (getApplicationContext (), LoginActivity.class);
                                    startActivity (intent);
                                    finish();
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "createUserWithEmail:failure" + "mAuth: " + mAuth.toString(), task.getException());
                                }
                            }
                        });


            }
        });

    }
}