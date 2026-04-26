package com.example.proj;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MessageAdapter extends BaseAdapter {
    Context context;
    ArrayList<Message> messages;
    String myId;

    public MessageAdapter(Context context, ArrayList<Message> messages, String myId) {
        this.context = context;
        this.messages = messages;
        this.myId = myId;
    }

    @Override
    public int getCount() { return messages.size(); }
    @Override
    public Object getItem(int i) { return messages.get(i); }
    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        }

        Message msg = messages.get(i);
        TextView tvContent = view.findViewById(R.id.tvMessageContent);
        LinearLayout container = view.findViewById(R.id.messageContainer);

        tvContent.setText(msg.content);

        // לוגיקה לסידור ההודעה (ימין/שמאל)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvContent.getLayoutParams();
        if (msg.senderId.equals(myId)) {
            // הודעה שלי - צד ימין בלבן
            params.gravity = Gravity.END;
            tvContent.setBackgroundResource(android.R.drawable.editbox_dropdown_dark_frame); // או צבע רקע אחר
            tvContent.setTextColor(Color.WHITE);
        } else {
            // הודעה של הצד השני - צד שמאל באפור
            params.gravity = Gravity.START;
            tvContent.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            tvContent.setTextColor(Color.BLACK);
        }
        tvContent.setLayoutParams(params);

        return view;
    }
}