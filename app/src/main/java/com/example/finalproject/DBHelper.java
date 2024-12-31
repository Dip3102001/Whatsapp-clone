package com.example.finalproject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DBHelper extends SQLiteOpenHelper
{
    final static String DB_NAME = "chats";
    final static String TABLE_NAME = "chat";
    final static int DB_VERSION = 1;

    public DocumentReference dbRef;
    public FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    public FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    public ArrayList<OnInsertListener> listener = new ArrayList<>();
    public ArrayList<Chat> buffer;

    public DBHelper(Context ct){
        super(ct,DB_NAME,null,DB_VERSION);
        dbRef = firebaseFirestore.collection("users").document(firebaseAuth.getUid());
        buffer = new ArrayList<>();


        dbRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                ArrayList<?> list = (ArrayList<?>)value.get("chats");
                for(int i=0;i<list.size();i++){
                    HashMap<String,Object> h = (HashMap<String,Object>)list.get(i);
                    ContentValues values = new ContentValues();
                    values.put("_from",(String) h.get("from"));
                    values.put("_to",firebaseAuth.getUid());
                    values.put("_msg",(String)h.get("txt"));
                    values.put("_timestamp",System.currentTimeMillis());
                    Chat c = new Chat((String)h.get("from"),(String)h.get("txt"),(Long)h.get("timestamp"));

                    System.out.println((String)h.get("txt")+" <------------------------> "+h.get("timestamp"));

                    System.out.println("-----------------------> "+buffer.contains(c));
                    System.out.println("-----------------------> "+listener.size());
                    if(!buffer.contains(c)){
                        insert(values);
                    }
                    dbRef.update("chats",FieldValue.arrayRemove(list.get(i)));
                    buffer.add(c);
                }
            }
        });
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("create table chat(_id integer primary key autoincrement,_from text,_to text,_msg text,_timestamp integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists chat");
        sqLiteDatabase.execSQL("create table chat(_id integer primary key autoincrement,_from text,_to text,msg text,_timestamp integer)");
    }


    public void insert(ContentValues values){
        Chat c = new Chat();
        c.setTxt((String)values.get("_msg"));
        c.setFrom((String)values.get("_from"));

        for(int i=0;i<listener.size();i++) listener.get(i).insertListener(c);
        this.getWritableDatabase().insert(TABLE_NAME,null,values);
    }

    public Cursor get(String columns[],String whereClause,String whereArgs[]){
        return this.getReadableDatabase().query(TABLE_NAME,columns,whereClause,whereArgs,null,null,"_timestamp");
    }


    public void registerInsertListener(OnInsertListener l)
    {
        listener.add(l);
    }
}
