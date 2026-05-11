package com.example.proj;

import static com.example.proj.FBRef.refUsers;

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
 * A custom adapter for a ListView or Spinner that displays a list of subjects
 * alongside toggle switches.
 * <p>
 * This adapter is specifically used to manage a user's "weak subjects."
 * When a switch is toggled, the adapter automatically updates the state
 * in the Firebase Realtime Database under the "weakSubjects" node for the current user.
 */
public class custom_adapter_weak extends BaseAdapter {

    /** Context used for inflating layouts and accessing resources. */
    private Context context;

    /** The list of subject names to be displayed. */
    private String[] subjects;

    /** A boolean array representing the current checked state of each subject's switch. */
    private boolean[] isCheckedArr;

    /** Used to instantiate the row layout XML into View objects. */
    private LayoutInflater inflater;

    /**
     * Constructs a new custom_adapter_weak.
     *
     * @param context    The current activity or fragment context.
     * @param subjects   An array of strings containing the subject names.
     * @param isChecked  A boolean array indicating which subjects are initially selected.
     */
    public custom_adapter_weak(Context context, String[] subjects, boolean[] isChecked) {
        this.context = context;
        this.subjects = subjects;
        this.isCheckedArr = isChecked;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * @return The total number of items in the subjects array.
     */
    @Override
    public int getCount() {
        return subjects.length;
    }

    /**
     * @param position Position of the item.
     * @return The subject name at the specified position.
     */
    @Override
    public Object getItem(int position) {
        return subjects[position];
    }

    /**
     * @param position Position of the item.
     * @return The position as the row ID.
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Provides a view for each row in the list.
     * <p>
     * This method handles:
     * 1. Inflating the custom layout.
     * 2. Setting the subject text.
     * 3. Managing the Switch state while avoiding recycling bugs.
     * 4. Updating Firebase when the user interacts with a switch.
     *
     * @param position    The position of the item within the adapter's data set.
     * @param convertView The old view to reuse, if possible.
     * @param parent      The parent view that this view will eventually be attached to.
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
         * IMPORTANT: We remove the listener before setting the checked state.
         * This prevents the listener from being triggered by the adapter
         * during the view recycling process (e.g., when scrolling).
         */
        switch1.setOnCheckedChangeListener(null);
        switch1.setChecked(isCheckedArr[position]);

        // Add the listener to handle user-initiated toggles
        switch1.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 1. Update the local data array
            isCheckedArr[position] = isChecked;

            // 2. Sync the change to Firebase Database
            String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("weakSubjects")
                    .child(subjectName)
                    .setValue(isChecked);
        });

        return convertView;
    }
}