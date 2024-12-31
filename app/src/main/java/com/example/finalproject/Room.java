package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

import java.util.StringTokenizer;
import java.util.function.Consumer;

public class Room extends AppCompatActivity {

    ImageView imageView;
    TextView textView;
    ImageView menu;
    EditText editText;
    ImageButton imageButton;

    FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    String sender_id;
    DBHelper db;

    boolean flag;


    @SuppressLint("Range")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        imageView = findViewById(R.id.imageViewInsideToolbar);
        textView = findViewById(R.id.textViewInsideToolbar);
        imageButton = findViewById(R.id.imageButton);
        editText = findViewById(R.id.editTextInsideRoom);
        menu = findViewById(R.id.menuInsideToolbar);

        flag = false;

        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("called...");
                PopupMenu popupMenu = new PopupMenu(Room.this, menu);
                popupMenu.getMenuInflater().inflate(R.menu.popupmenu, popupMenu.getMenu());
                popupMenu.show();

                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.R)
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if (menuItem.getItemId() == R.id.sendLocation) {
                            flag = true;

                            LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                            if (ActivityCompat.checkSelfPermission(Room.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(Room.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(Room.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},1);
                            }

                            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, new LocationListener() {
                                @Override
                                public void onLocationChanged(@NonNull Location location) {
                                    double lon = location.getLongitude();
                                    double lat = location.getLatitude();

                                    if(flag){
                                        Chat c = new Chat();
                                        c.setFrom(firebaseAuth.getUid());
                                        c.setTxt("#1818468489656853546846321654851684#: "+ lon +' '+ lat);
                                        c.setTimestamp(System.currentTimeMillis());

                                        firebaseFirestore.collection("users").document(sender_id).update("chats", FieldValue.arrayUnion(c));
                                        flag = false;
                                    }
                                }
                            });
                        }else if(menuItem.getItemId() == R.id.aboutUs){
                            Toast.makeText(Room.this, "created by Destroyer...", Toast.LENGTH_SHORT).show();
                        }else{
                            Intent intent = new Intent(Room.this,MapsActivity.class);
                            intent.putExtra("lat",23.03d);
                            intent.putExtra("lon",-21d);
                            startActivity(intent);
                        }
                        return true;
                    }
                });
            }
        });

        Intent intent = getIntent();
        sender_id = getIntent().getExtras().getString("_id_of_sender");
        db = new DBHelper(getApplicationContext());

        textView.setText(intent.getExtras().getString("username"));
        Picasso.get().load(intent.getExtras().getString("image_path")).placeholder(R.drawable.ic_baseline_face_24).into(imageView);


        Cursor cursor = db.get(new String[]{"_from","_to","_msg"},"(_from = ? AND _to = ?) OR (_from = ? AND _to = ?)",new String[]{sender_id,firebaseAuth.getUid(),firebaseAuth.getUid(),sender_id});
        ViewGroup vg = findViewById(R.id.chatArea);

        while(cursor.moveToNext()){
            String msg = cursor.getString(cursor.getColumnIndex("_msg"));
            View v;

            if(cursor.getString(cursor.getColumnIndex("_from")).equals(firebaseAuth.getUid())){
                v = getLayoutInflater().inflate(R.layout.message_layout,null);
                TextView t = v.findViewById(R.id.textViewInsideMessage);
                t.setText(msg);

            }else{
                v = getLayoutInflater().inflate(R.layout.recevice_message,null);
                TextView t = v.findViewById(R.id.textViewInsideReceiveMessage);
                t.setText(msg);
            }

            vg.addView(v);

            ((ScrollView)findViewById(R.id.scrollView)).post(new Runnable() {
                @Override
                public void run() {
                    ((ScrollView)findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                }
            });
        }


        db.registerInsertListener(new OnInsertListener() {
            @Override
            public void insertListener(Chat c) {
                if(c.getFrom().equals(sender_id)){
                    System.out.println("-------------------------------> called...");

                    String msg = c.getTxt();
                    StringTokenizer stringTokenizer = new StringTokenizer(msg," ",false);
                    if(stringTokenizer.countTokens() == 3){
                        if(stringTokenizer.nextToken().equals("#1818468489656853546846321654851684#:")){
                            String lon = stringTokenizer.nextToken();
                            String lat = stringTokenizer.nextToken();

                            View v = getLayoutInflater().inflate(R.layout.location,null);
                            v.findViewById(R.id.buttonInsideReceiveMessage).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Room.this,MapsActivity.class);
                                    intent.putExtra("lon",lon);
                                    intent.putExtra("lat",lat);
                                    startActivity(intent);
                                }
                            });
                            vg.addView(v);
                        }else{
                            View v = getLayoutInflater().inflate(R.layout.recevice_message,null);
                            TextView t = v.findViewById(R.id.textViewInsideReceiveMessage);
                            t.setText(msg);
                            vg.addView(v);
                        }
                    }
                    else{
                        View v = getLayoutInflater().inflate(R.layout.recevice_message,null);
                        TextView t = v.findViewById(R.id.textViewInsideReceiveMessage);
                        t.setText(msg);
                        vg.addView(v);
                    }
                }

                ((ScrollView)findViewById(R.id.scrollView)).post(new Runnable() {
                    @Override
                    public void run() {
                        ((ScrollView)findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                    }
                });

            }
        });

        imageButton.setOnClickListener(view->{
            String msg = editText.getText().toString();
            editText.setText("");

            View v = Room.this.getLayoutInflater().inflate(R.layout.message_layout,null);
            TextView textViewInsideMessage = v.findViewById(R.id.textViewInsideMessage);
            textViewInsideMessage.setText(msg);

            vg.addView(v);

            ((ScrollView)findViewById(R.id.scrollView)).post(new Runnable() {
                @Override
                public void run() {
                    ((ScrollView)findViewById(R.id.scrollView)).fullScroll(ScrollView.FOCUS_DOWN);
                }
            });

            Chat c = new Chat();
            c.setFrom(firebaseAuth.getUid());
            c.setTxt(msg);
            c.setTimestamp(System.currentTimeMillis());

            ContentValues values = new ContentValues();

            values.put("_from",firebaseAuth.getUid());
            values.put("_to",sender_id);
            values.put("_msg",msg);
            values.put("_timestamp",System.currentTimeMillis());

            db.insert(values);

            firebaseFirestore.collection("users").document(sender_id).update("chats", FieldValue.arrayUnion(c));
        });
    }
}