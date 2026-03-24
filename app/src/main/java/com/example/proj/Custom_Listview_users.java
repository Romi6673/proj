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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
import java.util.Map;


public class Custom_Listview_users extends BaseAdapter {
    Context context;
    List<Users> userList; // רשימה של אובייקטי המשתמש שמצאנו
    private LayoutInflater inflater;

    public Custom_Listview_users(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_users_custom, viewGroup, false);
        }

        ImageView ivUserProfile = convertView.findViewById(R.id.ivUserProfile);
        TextView tvUserNameSearch = convertView.findViewById(R.id.tvUserNameSearch);
        TextView tvUserSubject = convertView.findViewById(R.id.tvUserSubject);
        Button btnSendChatRequestBtn = convertView.findViewById(R.id.btnSendChatRequestBtn);

        Users user = userList.get(i);

        tvUserNameSearch.setText(user.userName);

        // נציג למשל את המקצוע הראשון שהוא חזק בו מתוך הרשימה
        String topSubject = "No specific subject";
        if (user.strongSubjects != null) {
            for (Map.Entry<String, Boolean> entry : user.strongSubjects.entrySet()) {
                if (entry.getValue()) {
                    topSubject = entry.getKey();
                    break;
                }
            }
        }
        tvUserSubject.setText("Strong in: " + topSubject);

        // טעינת תמונה עם Glide
        if (user.profilePicUrl != null && !user.profilePicUrl.isEmpty()) {
            Glide.with(context).load(user.profilePicUrl).into(ivUserProfile);
        } else {
            ivUserProfile.setImageResource(R.drawable.outline_account_circle_24);
        }

        // בתוך ה-getView של Custom_Listview_users
        btnSendChatRequestBtn.setOnClickListener(v -> {
            String fromId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            String toId = user.userId; // המשתמש שמופיע בשורה הזו

            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRequests");
            String requestId = ref.push().getKey(); // יצירת מפתח ייחודי

            // יצירת אובייקט הבקשה (כאן נניח שהשם של השולח ידוע או נשלח לאדפטר)
            chatRequest newRequest = new chatRequest(requestId, fromId, "Someone", toId, "Math", 0);

            ref.child(requestId).setValue(newRequest).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(context, "Request Sent!", Toast.LENGTH_SHORT).show();
                    btnSendChatRequestBtn.setEnabled(false); // מניעת שליחה כפולה
                    btnSendChatRequestBtn.setText("Sent");
                }
            });
        });

        return convertView;
    }
}