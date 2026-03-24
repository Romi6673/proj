package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class custom_lv_request_adapter extends BaseAdapter {
    Context context;
    ArrayList<chatRequest> requests;
    private LayoutInflater inflater;

    public custom_lv_request_adapter(Context context, ArrayList<chatRequest> requests) {
        this.context = context;
        this.requests = requests;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() { return requests.size(); }

    @Override
    public Object getItem(int i) { return requests.get(i); }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.custom_lv_requests, viewGroup, false);

        chatRequest req = requests.get(i);

        TextView tvUserName = view.findViewById(R.id.tvUserName);
        TextView tvSubject = view.findViewById(R.id.tvSubject);
        Button btnAccept = view.findViewById(R.id.btnAccept);
        Button btnDecline = view.findViewById(R.id.btnDecline);

        tvUserName.setText(req.fromUserName);
        tvSubject.setText("Wants help with: " + req.subject);

        // כפתור אישור
        btnAccept.setOnClickListener(v -> updateStatus(req.requestId, 2));

        // כפתור דחייה
        btnDecline.setOnClickListener(v -> updateStatus(req.requestId, 1));

        return view;
    }

    private void updateStatus(String requestId, int newStatus) {
        FirebaseDatabase.getInstance().getReference("ChatRequests")
                .child(requestId).child("status").setValue(newStatus);
    }
}