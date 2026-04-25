package com.example.proj;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
public class SearchFragment extends Fragment {

    ListView userListView;
    Spinner filterSpinner;
    ArrayList<Users> suggestedUsers = new ArrayList<>();
    ArrayList<String> myWeakSubjectsList = new ArrayList<>();
    Custom_Listview_users adapter;
    String currentUserId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        userListView = view.findViewById(R.id.userListView);
        filterSpinner = view.findViewById(R.id.filterSpinner);
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // שלב 1: טעינת המקצועות של המשתמש המחובר לספינר
        loadMyWeakSubjectsIntoSpinner();

        // שלב 2: האזנה לבחירה בספינר
        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedSubject = myWeakSubjectsList.get(position);
                // שלב 3: חיפוש משתמשים שחזקים במקצוע שנבחר
                searchUsersBySubject(selectedSubject);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private void loadMyWeakSubjectsIntoSpinner() {
        FBRef.refUsers.child(currentUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users currentUser = snapshot.getValue(Users.class);
                myWeakSubjectsList.clear();

                if (currentUser != null && currentUser.weakSubjects != null) {
                    for (Map.Entry<String, Boolean> entry : currentUser.weakSubjects.entrySet()) {
                        // עוברים רק על מפת המקצועות החלשים וentry הוא משתנה שהגדרנו כדי כל פעם
                        // לעבור איתו על מפת המקצועות החלשים
                        if (entry.getValue()) { // רק מקצועות שסומנו כחלשים (true)
                            myWeakSubjectsList.add(entry.getKey());
                        }
                    }
                }

                if (myWeakSubjectsList.isEmpty()) {
                    myWeakSubjectsList.add("No weak subjects defined");
                }

                // יצירת אדפטר פשוט לספינר (בלי סוויצ'ים, רק טקסט)
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(),
                        android.R.layout.simple_spinner_item, myWeakSubjectsList);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                filterSpinner.setAdapter(spinnerAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void searchUsersBySubject(String subject) {
        FBRef.refUsers.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suggestedUsers.clear();
                for (DataSnapshot userSnap : snapshot.getChildren()) {
                    Users otherUser = userSnap.getValue(Users.class); // המשתמש במקום הנוכחי בלולאה

                    if (otherUser != null && !otherUser.userId.equals(currentUserId)) {
                        if (otherUser.strongSubjects != null &&
                                otherUser.strongSubjects.containsKey(subject) &&
                                otherUser.strongSubjects.get(subject)) {
                            //סינון שהמשתמש הוא לא המשתמש המחובר, הוא לא null רשימת
                            // המקצועות החזקים שלו מכילה את המקצוע שמסונן והערך שלו בmap הוא true

                            suggestedUsers.add(otherUser);
                        }
                    }
                }

                // 1. בודקים שהפרגמנט עדיין מחובר (isAdded)
                // 2. בודקים שהקונטקסט לא null
                if (isAdded() && getContext() != null) {
                    adapter = new Custom_Listview_users(getContext(), suggestedUsers);
                    userListView.setAdapter(adapter);
                }
                // --------------------------
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
}