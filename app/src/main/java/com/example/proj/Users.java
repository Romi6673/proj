package com.example.proj;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Users {

    public String email;
    public String password;
    public String userId;
    public String userName;
    public String bio;
    public String profilePicUrl;
    public int score;

    public Map<String, Boolean> weakSubjects;
    public Map<String, Boolean> strongSubjects;

    public Users() {
    }

    public Users(String email, String password, String userId ) {
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.userName = "";
        this.bio = "no bio yet";
        this.profilePicUrl = "";
        this.score = 10;

        // שימוש ב-HashMap כדי שהמפה תהיה ניתנת לשינוי
        this.weakSubjects = new HashMap<>();
        this.weakSubjects.put("History", false);
        this.weakSubjects.put("Math", false);
        this.weakSubjects.put("English", false);
        this.weakSubjects.put("Science", false);

        this.strongSubjects = new HashMap<>();
        this.strongSubjects.put("History", false);
        this.strongSubjects.put("Math", false);
        this.strongSubjects.put("English", false);
        this.strongSubjects.put("Science", false);
    }

    public void setScore(int score){
        this.score = score;
    }
}
