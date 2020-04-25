package com.tauk.carsapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    private EditText etEmail;
    private EditText etPassword;
    private TextView tvStatus;
    boolean loginWasSuccessfull;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etEmail = findViewById(R.id.etCarRegNumber);
        etPassword = findViewById(R.id.etModelYear);
        tvStatus = findViewById(R.id.tvStatus);

        FirebaseApp.initializeApp(this); //initialize firebase

        firebaseAuth = FirebaseAuth.getInstance(); //get instance of firebaseAuth
    }

    public void doRegister(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("doRegister() ", "createUserWithEmail:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            //user;
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("doRegister", "createUserWithEmail:failure", task.getException());
                            tvStatus.setText("Register failed!!!" +task.getException());
                        }
                        // ...
                    }
                });
    }

    public void doLogin(View view) {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d("doRegister() ", "createUserWithEmail:success");

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            tvStatus.setText(user.toString());
                            loginWasSuccessfull = true;
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("doRegister", "createUserWithEmail:failure", task.getException());
                            tvStatus.setText("Register failed!!!" +task.getException());
                        }

                        // ...
                    }
                });
            if (loginWasSuccessfull) {
                Intent intent = new Intent(this, Main2Activity.class);
                startActivity(intent);
            }

    }
}
