package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;


public class custom_adapter_strong extends BaseAdapter{
    private Context context;
    private String[] subjects;
    private boolean[] isCheckedArr;
    private LayoutInflater inflater;

    public custom_adapter_strong(Context context, String[] subjects, boolean[] isChecked) {
        this.context = context;
        this.subjects = subjects;
        this.isCheckedArr = isChecked;
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
        switch1.setChecked(isCheckedArr[position]);


        String subjectName = subjects[position];
        textView.setText(subjectName);

        // חשוב: מסירים מאזין קודם כדי למנוע באגים בזמן גלילה
        switch1.setOnCheckedChangeListener(null);
        switch1.setChecked(isCheckedArr[position]);

        // הוספת המאזין החדש
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 1. עדכון המערך המקומי בפרגמנט
            isCheckedArr[position] = isChecked;

            // 2. עדכון ה-Firebase
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // כאן את בוחרת אם זה weakSubjects או strongSubjects לפי שם האדפטר
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("strongSubjects") // או strongSubjects במחלקה השנייה
                    .child(subjectName)
                    .setValue(isChecked);
        });

        return convertView;


    }
}