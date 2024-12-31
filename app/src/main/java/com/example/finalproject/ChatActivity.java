package com.example.finalproject;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;
    public FirebaseFirestore firebaseFirestore;
    public ListView listView;
    public ProgressBar progressBar;

    final public ArrayList<PersonContactInformation> buffer = new ArrayList<PersonContactInformation>();
    final ArrayList<PersonContactInformation> personInContact = new ArrayList<>();
    String auth = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        listView = findViewById(R.id.listView);
        progressBar = findViewById(R.id.progressBar4);
        progressBar.setVisibility(View.VISIBLE);

        firebaseFirestore.collection("users").document(firebaseAuth.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                auth = documentSnapshot.getString("username");
            }
        });

        getAllContacts();

        myAsyncTask t = new myAsyncTask();
        t.execute();


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                 PersonContactInformation p = personInContact.get(i);
                 Intent intent = new Intent(ChatActivity.this,Room.class);

                 intent.putExtra("username",p.getName());
                 intent.putExtra("image_path",p.getImage_path());
                 intent.putExtra("auth",auth);
                 intent.putExtra("_id_of_sender",personInContact.get(i).get_id());

                 startActivity(intent);
            }
        });

        DocumentReference dbRef = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        dbRef.update("chats", Arrays.asList());
    }

    @SuppressLint("Range")
    private void getAllContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);

        if ((cur != null ? cur.getCount() : 0) > 0) {
            while (cur != null && cur.moveToNext()) {
                @SuppressLint("Range") String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                @SuppressLint("Range") String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                if (cur.getInt(cur.getColumnIndex( ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[]{id}, null);
                    while (pCur.moveToNext()) {
                        PersonContactInformation pr = new PersonContactInformation();
                        pr.setId(id);
                        pr.setName(name);
                        String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        pr.setPhn_number(phoneNo);
                        buffer.add(pr);
                    }
                    pCur.close();
                }
            }
        }
        if (cur != null) {
            cur.close();
        }
    }

    public String getTrimString(String num){
        StringBuilder sb = new StringBuilder();
        for(int i=0;i<num.length();i++){
            if(i==0 && num.charAt(i)=='+') sb.append(num.charAt(i));
            else if ('0'<=num.charAt(i) && num.charAt(i)<='9')sb.append(num.charAt(i));
        }
        return sb.toString();
    }



    public class myAsyncTask extends AsyncTask<Void,Void,Void>{
        String code = "";

        @Override
        protected Void doInBackground(Void... voids) {
            DocumentReference dbRef = firebaseFirestore.collection("users").document(firebaseAuth.getUid());

            dbRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    code = task.getResult().getString("country_code");

                    for(int i=0;i<buffer.size();i++){
                        String number = getTrimString(buffer.get(i).getPhn_number());

                        if(number.charAt(0) != '+' && number.length()>10)
                            number = "+" + number;
                        else if(number.charAt(0) != '+')
                            number = code + number;

                        buffer.get(i).setPhn_number(number);
                    }



                    ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();

                    for(int i=0;i<buffer.size();i++)
                    {
                        if(i%10 == 0) {
                            list.add(new ArrayList<String>());
                        }
                        list.get(i/10).add(buffer.get(i).getPhn_number());
                    }



                    final ArrayList<String> name = new ArrayList<>();

                    for(int i=0;i<list.size();i++){
                        Query q = firebaseFirestore.collection("users").whereIn("phn_number",list.get(i));
                        q.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                List<DocumentSnapshot> buffer = task.getResult().getDocuments();
                                for(int i=0;i<buffer.size();i++){
                                    PersonContactInformation p = new PersonContactInformation();
                                    p.setPhn_number(buffer.get(i).getString("phn_number"));
                                    p.setName(buffer.get(i).getString("username"));
                                    p.setImage_path(buffer.get(i).getString("image_path"));
                                    p.set_id(buffer.get(i).getId());
                                    personInContact.add(p);
                                    name.add(buffer.get(i).getString("username"));
                                }
                                listView.setAdapter(new myList(ChatActivity.this,personInContact,name));
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
                    }



                }
            });
            return null;
        }
    }
}