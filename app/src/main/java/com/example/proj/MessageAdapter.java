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

/**
 * Custom adapter for a ListView used to display chat messages.
 * <p>
 * This adapter manages the layout of individual chat bubbles, ensuring that
 * messages sent by the current user are aligned to one side (usually right)
 * and received messages are aligned to the other (usually left).
 */
public class MessageAdapter extends BaseAdapter {

    /** Context used for inflating the layout and accessing resources. */
    Context context;

    /** The list of {@link Message} objects to be displayed. */
    ArrayList<Message> messages;

    /** The unique ID of the current authenticated user, used for message alignment logic. */
    String myId;

    /**
     * Constructs a new MessageAdapter.
     *
     * @param context  The current activity or fragment context.
     * @param messages The list of messages in the conversation.
     * @param myId     The ID of the current user to differentiate "sent" vs "received" bubbles.
     */
    public MessageAdapter(Context context, ArrayList<Message> messages, String myId) {
        this.context = context;
        this.messages = messages;
        this.myId = myId;
    }

    /** @return The number of messages in the data set. */
    @Override
    public int getCount() { return messages.size(); }

    /** @return The {@link Message} object at the specified position. */
    @Override
    public Object getItem(int i) { return messages.get(i); }

    /** @return The position of the item as its unique ID. */
    @Override
    public long getItemId(int i) { return i; }

    /**
     * Provides the view for a specific message row.
     * <p>
     * This method handles:
     * 1. Reusing or inflating the message item view.
     * 2. Setting the message content.
     * 3. Dynamically adjusting {@link LinearLayout.LayoutParams} to set gravity
     *    (END for sender, START for recipient) and applying specific background bubbles.
     *
     * @param i           The position of the message.
     * @param view        The recycled view.
     * @param parent      The parent view group.
     * @return A populated View for the message bubble.
     */
    @Override
    public View getView(int i, View view, ViewGroup parent) {
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.message_item, parent, false);
        }

        Message msg = messages.get(i);
        TextView tvContent = view.findViewById(R.id.tvMessageContent);
        LinearLayout container = view.findViewById(R.id.messageContainer);

        tvContent.setText(msg.content);

        // Logic for message alignment (Right/Left)
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tvContent.getLayoutParams();

        if (msg.senderId.equals(myId)) {
            // My message - Right aligned (Gravity.END) with a dark theme/white text
            params.gravity = Gravity.END;
            tvContent.setBackgroundResource(android.R.drawable.editbox_dropdown_dark_frame);
            tvContent.setTextColor(Color.WHITE);
        } else {
            // Received message - Left aligned (Gravity.START) with a light theme/black text
            params.gravity = Gravity.START;
            tvContent.setBackgroundResource(android.R.drawable.editbox_dropdown_light_frame);
            tvContent.setTextColor(Color.BLACK);
        }

        tvContent.setLayoutParams(params);

        return view;
    }
}