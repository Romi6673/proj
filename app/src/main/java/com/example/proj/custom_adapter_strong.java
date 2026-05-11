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

/**
 * Custom adapter for a ListView or Spinner that displays a list of subjects
 * with toggle switches. This adapter is specifically designed to manage
 * "strong subjects" for the current user.
 *
 * Each toggle interaction directly updates the state in the Firebase Realtime Database.
 */
public class custom_adapter_strong extends BaseAdapter {

    /** Context for inflating layouts and accessing resources. */
    private Context context;

    /** Array of subject names to be displayed in the list. */
    private String[] subjects;

    /** Boolean array tracking the checked state of each subject. */
    private boolean[] isCheckedArr;

    /** Used to instantiate the layout XML file into its corresponding View objects. */
    private LayoutInflater inflater;

    /**
     * Constructs a new custom_adapter_strong.
     *
     * @param context    The current context.
     * @param subjects   An array of strings representing the subject names.
     * @param isChecked  A boolean array representing the initial state of the switches.
     */
    public custom_adapter_strong(Context context, String[] subjects, boolean[] isChecked) {
        this.context = context;
        this.subjects = subjects;
        this.isCheckedArr = isChecked;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * Returns the number of items in the data set.
     * @return The size of the subjects array.
     */
    @Override
    public int getCount() {
        return subjects.length;
    }

    /**
     * Returns the data item associated with the specified position.
     * @param position Position of the item whose data we want within the adapter's data set.
     * @return The subject name at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return subjects[position];
    }

    /**
     * Returns the row id associated with the specified position in the list.
     * @param position The position of the item within the adapter's data set.
     * @return The same value as the position.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Get a View that displays the data at the specified position in the data set.
     * This method handles the binding of the subject name to the TextView and
     * manages the Firebase update logic via the Switch.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent that this view will eventually be attached to.
     * @return A View corresponding to the data at the specified position.
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Inflate the layout for each row
        convertView = inflater.inflate(R.layout.custom_spinner_layout, parent, false);

        TextView textView = convertView.findViewById(R.id.textView);
        Switch switch1 = convertView.findViewById(R.id.switch1);

        String subjectName = subjects[position];
        textView.setText(subjectName);

        /*
         * IMPORTANT: Remove the previous listener before setting the checked state.
         * This prevents the listener from triggering incorrectly when the ListView
         * recycles views during scrolling.
         */
        switch1.setOnCheckedChangeListener(null);
        switch1.setChecked(isCheckedArr[position]);

        // Add a new listener to handle user interaction and Firebase synchronization
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 1. Update the local array to keep the UI data in sync
            isCheckedArr[position] = isChecked;

            // 2. Update the Firebase Database for the current authenticated user
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Specifically updates the "strongSubjects" node
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("strongSubjects")
                    .child(subjectName)
                    .setValue(isChecked);
        });

        return convertView;
    }
}