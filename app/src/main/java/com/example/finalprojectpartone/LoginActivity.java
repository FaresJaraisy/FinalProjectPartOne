package com.example.finalprojectpartone;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import database.DBManager;

public class LoginActivity extends AppCompatActivity {
    private DBManager dbManager;
    private UserProfile user;

    FirebaseAuth mAuth;
    TextView textView;

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            Intent intent = new Intent (getApplicationContext(), MainActivity.class);
            startActivity (intent);
            finish();
        }
    }

    // get UI elements and set listener to login button
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mAuth= FirebaseAuth.getInstance();

        textView = findViewById(R.id.registerNow);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intent);
                finish ();
            }
        });



        Button Login = findViewById(R.id.Login);
        EditText userName = findViewById(R.id.email);
        EditText userPassword = findViewById(R.id.password);

        dbManager = new DBManager(this);
        dbManager.open();

        // onClock handler to login button
        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email, password;
                email =String.valueOf(userName.getText());
                password =String.valueOf(userPassword.getText());

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(LoginActivity.this, "Enter email", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(LoginActivity.this, "Enter password", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Toast.makeText(LoginActivity.this, "Login successful.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.d(TAG, "signInWithEmail:success");
                                    Intent intent = new Intent (getApplicationContext(), MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());

                                }
                            }
                        });

                //user = dbManager.getUser(userName.getText().toString() , userPassword.getText().toString());
               // if (user != null) {
               //     Toast.makeText(LoginActivity.this, "Login success ",Toast.LENGTH_LONG).show();
               //     Intent main_intent = new Intent(getApplicationContext(), MainActivity.class);
               //     main_intent.putExtra("id", user.getId());
               //     main_intent.putExtra("username", user.getUserName());
//
               //     startActivity(main_intent);
               // } else {
                //    Toast.makeText(LoginActivity.this, "Login failed ",Toast.LENGTH_LONG).show();
               // }
            }
        });
    }
}