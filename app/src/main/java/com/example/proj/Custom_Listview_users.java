package com.example.proj;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Custom adapter for a ListView that displays a list of {@link Users}.
 * <p>
 * This adapter manages the mapping between a list of user objects and the
 * custom layout 'listview_users_custom'. It handles user names,
 * scores, and profile pictures using the Glide library.
 */
public class Custom_Listview_users extends BaseAdapter {

    /** The context of the calling activity or fragment. */
    private Context context;

    /** The list of user objects to be displayed. */
    private List<Users> userList;

    /** Used to inflate the custom layout XML into View objects. */
    private LayoutInflater inflater;

    /**
     * Constructs a new Custom_Listview_users adapter.
     *
     * @param context  The current context.
     * @param userList The list of Users to populate the ListView.
     */
    public Custom_Listview_users(Context context, List<Users> userList) {
        this.context = context;
        this.userList = userList;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Returns the total number of items in the user list.
     * @return The size of the userList.
     */
    @Override
    public int getCount() {
        return userList.size();
    }

    /**
     * Returns the user object at the specified position.
     * @param i The position of the item in the list.
     * @return The Users object at position i.
     */
    @Override
    public Object getItem(int i) {
        return userList.get(i);
    }

    /**
     * Returns the row ID for the specified position.
     * @param i The position of the item.
     * @return The position itself as the ID.
     */
    @Override
    public long getItemId(int i) {
        return i;
    }

    /**
     * Creates or reuses a view for a specific row in the ListView.
     * <p>
     * This method:
     * 1. Inflates the custom layout if no recycled view is available.
     * 2. Sets the user's name and score.
     * 3. Loads the profile picture from a URL using Glide,
     *    or sets a default icon if no URL is present.
     *
     * @param i           The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param viewGroup   The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.listview_users_custom, viewGroup, false);
        }

        ImageView ivUserProfile = convertView.findViewById(R.id.ivUserProfile);
        TextView tvUserNameSearch = convertView.findViewById(R.id.tvUserNameSearch);
        TextView tvUserSubject = convertView.findViewById(R.id.tvUserSubject);

        // Get the specific user for the current row
        Users user = userList.get(i);

        // Bind the text data
        tvUserNameSearch.setText(user.userName);
        tvUserSubject.setText("Score: " + user.score);

        // Handle profile picture loading
        if (user.profilePicUrl != null && !user.profilePicUrl.isEmpty()) {
            // Load the image from the URL into the ImageView
            Glide.with(context)
                    .load(user.profilePicUrl)
                    .into(ivUserProfile);
        } else {
            // Set a default placeholder if the user has no profile picture
            ivUserProfile.setImageResource(R.drawable.outline_account_circle_24);
        }

        return convertView;
    }
}