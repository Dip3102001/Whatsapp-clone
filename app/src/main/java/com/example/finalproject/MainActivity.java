package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.hbb20.CountryCodePicker;

import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private Button button;
    private EditText editText;
    protected static FirebaseAuth firebaseAuth;
    private String var_string;
    private String countryCodeString;
    private CountryCodePicker codePicker;
    private ProgressBar progressBar;


    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth = FirebaseAuth.getInstance();
        if(firebaseAuth.getCurrentUser() != null){
            Intent intent = new Intent(MainActivity.this,ChatActivity.class);
            startActivity(intent);
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.button);
        editText = findViewById(R.id.editText);
        codePicker = findViewById(R.id.countryCodePicker);
        countryCodeString = codePicker.getDefaultCountryCodeWithPlus();
        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        codePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
               countryCodeString = codePicker.getSelectedCountryCodeWithPlus();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String number = editText.getText().toString();
                progressBar.setVisibility(View.VISIBLE);
                if(number == null)
                    Toast.makeText(MainActivity.this, "Plz Enter Phone Number..", Toast.LENGTH_SHORT).show();
                else if(number.length() < 10)
                    Toast.makeText(MainActivity.this, "Plz Enter Appropriate PhoneNumber", Toast.LENGTH_SHORT).show();
                else{
                    String final_number_with_country_code = countryCodeString+number;
                    System.out.println("--------------------->"+final_number_with_country_code);
                    PhoneAuthOptions options =
                            PhoneAuthOptions.newBuilder(firebaseAuth)
                                    .setPhoneNumber(final_number_with_country_code)       // Phone number to verify
                                    .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                                    .setActivity(MainActivity.this)                 // Activity (for callback binding)
                                    .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                            signInWithPhoneAuthCredential(phoneAuthCredential);
                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {

                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            super.onCodeSent(s, forceResendingToken);
                                            var_string = s;
                                            Intent intent = new Intent(MainActivity.this,GetOtpActivity.class);
                                            intent.putExtra("code",s);
                                            intent.putExtra("phn_number",final_number_with_country_code);
                                            intent.putExtra("country-code",countryCodeString);
                                            startActivity(intent);
                                        }
                                    })
                                    .build();
                    PhoneAuthProvider.verifyPhoneNumber(options);
                }

            }
        });
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        MainActivity.firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener( MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            System.out.println("signInWithCredential:success");

                            FirebaseUser user = task.getResult().getUser();
                            Intent intent = new Intent(MainActivity.this,SuccessActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Sign in failed, display a message and update the UI
                            System.out.println("signInWithCredential:failure");
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }
}