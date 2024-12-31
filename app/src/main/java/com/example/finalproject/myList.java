package com.example.finalproject;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class myList extends ArrayAdapter<String>
{
    private Activity ct;
    private ArrayList<PersonContactInformation> list;

    public myList(Activity ct, ArrayList<PersonContactInformation> list,ArrayList<String> name){
        super(ct,R.layout.layout_card,name);

        this.ct = ct;
        this.list = list;
    }

    public View getView(int position, View view, ViewGroup parent){
        LayoutInflater inflater = ct.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.layout_card,null,true);

        try {
            ImageView img = rowView.findViewById(R.id.imageViewInsideCardView);
            TextView textView = rowView.findViewById(R.id.textViewInsideCardView);
            textView.setText(list.get(position).getName());

            Picasso.get().load(list.get(position).getImage_path()).placeholder(R.drawable.ic_baseline_face_24).into(img);
        }
        catch(Exception e){
            System.out.println("Error in Picasso.... .... .....");
        }

        return rowView;
    }
}
