package com.example.proj;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;



public class Custom_Listview_users extends BaseAdapter {
    Context context;
    int [] images;
    private LayoutInflater inflater;

    String [] names;
    String [] subjects;


    public Custom_Listview_users(Context context , int [] images , String [] names , String [] subjects) {
        this.context = context;
        this.images = images;
        this.names = names;
        this.subjects = subjects;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return images.length;
    }

    @Override
    public Object getItem(int i) {
        return names[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        convertView = inflater.inflate(R.layout.listview_users_custom, viewGroup, false);
        ImageView ivUserProfile = convertView.findViewById(R.id.ivUserProfile);
        TextView tvUserNameSearch = convertView.findViewById(R.id.tvUserNameSearch);
        TextView tvUserSubject = convertView.findViewById(R.id.tvUserSubject);
        Button btnSendChatRequestBtn = convertView.findViewById(R.id.btnSendChatRequestBtn);


        return convertView;
    }
}
