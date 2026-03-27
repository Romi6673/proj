package com.example.proj;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class chatsRequestActivity extends AppCompatActivity {
    ListView lvFollowRequests;
    ArrayList<chatRequest> requestList;
    custom_lv_request_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_request);
        lvFollowRequests = findViewById(R.id.lvFollowRequests);
        requestList = new ArrayList<>();

        loadRequests();
    }

    private void loadRequests() {
        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("ChatRequests");

        // סינון: הבא לי רק בקשות שנשלחו אליי (toUserId == myId)
        ref.orderByChild("toUserId").equalTo(myId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requestList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    chatRequest req = ds.getValue(chatRequest.class);
                    // נציג רק בקשות שממתינות (status == 0)
                    if (req != null && req.status == 0) {
                        requestList.add(req);
                    }
                }

                // עדכון האדפטר (שיניתי אותו קצת שיקבל רשימת אובייקטים)
                adapter = new custom_lv_request_adapter(chatsRequestActivity.this, requestList);
                lvFollowRequests.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }



    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        String id = item.getTitle().toString();
        if (id.equals("requests")) {
            Intent intent = new Intent(this, chatsRequestActivity.class);
            startActivity(intent);

            return super.onOptionsItemSelected(item);
        }else{
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return super.onOptionsItemSelected(item);
        }

    }

}