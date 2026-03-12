package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;


public class custom_adapter_strong extends BaseAdapter{
    private Context context;
    private String[] subjects;
    private boolean[] isChecked;
    private LayoutInflater inflater;

    public custom_adapter_strong(Context context, String[] subjects, boolean[] isChecked) {
        this.context = context;
        this.subjects = subjects;
        this.isChecked = isChecked;
        this.inflater = LayoutInflater.from(context);

    }

    public int getCount() {
        return subjects.length;
    }

    public Object getItem(int position) {
        return subjects[position];
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.custom_spinner_layout, parent, false);
        TextView textView = convertView.findViewById(R.id.textView);
        textView.setText(subjects[position]);
        Switch switch1 = convertView.findViewById(R.id.switch1);
        switch1.setChecked(isChecked[position]);
        return convertView;



    }
}