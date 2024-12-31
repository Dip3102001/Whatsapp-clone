package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

public class GetOtpActivity extends AppCompatActivity {

    private EditText editText;
    private ProgressBar progressBar;
    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_otp);

        button = findViewById(R.id.verifyButton);
        progressBar = findViewById(R.id.progressBar2);
        editText = findViewById(R.id.editTextOtp);



        button.setOnClickListener(view->{
            progressBar.setVisibility(View.VISIBLE);

            String buffer = editText.getText().toString();

            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(getIntent().getExtras().getString("code"), buffer);
            signInWithPhoneAuthCredential(credential);
        });
    }




        public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
            MainActivity.firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener( this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();



                            Intent intent = new Intent(GetOtpActivity.this,SuccessActivity.class);
                            intent.putExtra("country-code",getIntent().getExtras().getString("country-code"));
                            intent.putExtra("phn_number",getIntent().getExtras().getString("phn_number"));
                            startActivity(intent);
                            finish();
                        } else {
                            System.out.println("signInWithCredential:failure");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                System.out.println("Invalid code..");
                            }
                        }
                    }
                });
    }
}