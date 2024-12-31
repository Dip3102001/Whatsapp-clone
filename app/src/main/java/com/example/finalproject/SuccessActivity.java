package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SuccessActivity extends AppCompatActivity {

    private static final int SELECT_PICTURE = 200;
    public ImageView imageView;
    public EditText editText;
    public Button button;

    public FirebaseAuth firebaseAuth;
    public FirebaseStorage storage;
    public StorageReference storageReference;
    public DatabaseReference dbReference;
    public FirebaseFirestore firebaseFirestore;
    public String imagePath;
    public ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_success);

        imageView = findViewById(R.id.imageView);
        editText = findViewById(R.id.personName);
        button = findViewById(R.id.button2);

        firebaseAuth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        dbReference = FirebaseDatabase.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressBar = findViewById(R.id.progressBar3);

        progressBar.setVisibility(View.INVISIBLE);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                String name = editText.getText().toString();
                if(name == null){
                    Toast.makeText(SuccessActivity.this, "Enter Your Name", Toast.LENGTH_SHORT).show();
                }
                else{
                    Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
                    ByteArrayOutputStream bout = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG,100,bout);
                    byte[] imageInByte = bout.toByteArray();

                    getStorageRef(imageInByte,name);
                }
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"Select Picture"),SELECT_PICTURE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_PICTURE) {
            if(resultCode == RESULT_OK){
                Uri uri = data.getData();
                imageView.setImageURI(uri);
            }
        }
    }

    public void getStorageRef(byte[] img,String name){
        StorageReference ref = storageReference.child("image").child(firebaseAuth.getUid());

        ref.putBytes(img).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageReference.child("image").child(firebaseAuth.getUid()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        dbReference.child("users").child(firebaseAuth.getUid()).setValue(new User(name,firebaseAuth.getUid()));


                        Map<String,Object> data = new HashMap<>();
                        data.put("username",name);
                        data.put("image_path",uri.toString());
                        data.put("country_code",getIntent().getExtras().getString("country-code"));
                        data.put("phn_number",getIntent().getExtras().getString("phn_number"));

                        firebaseFirestore.collection("users").document(firebaseAuth.getUid())
                                .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Intent intent = new Intent(SuccessActivity.this,ChatActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println(e.toString());
                    }
                });
            }
        });
    }
}