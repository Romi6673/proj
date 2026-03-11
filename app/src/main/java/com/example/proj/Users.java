package com.example.proj;

import java.util.Hashtable;
import java.util.Map;

public class Users {

    public String email;
    public String password;
    public String userId;
    public String userName;
    public String bio;
    public String profilePicUrl;

    public Map<String, Boolean> weakSubjects;
    public Map<String, Boolean> strongSubjects;

    public Users() {
    }

    public Users(String email, String password, String userId) {
        this.email = email;
        this.password = password;
        this.userId = userId;
        this.userName = "";
        this.bio = "";
        this.profilePicUrl = "";
        this.weakSubjects = Map.of(
                "History", false,
                "Math", false,
                "English", false,
                "Science", false
        );
        this.strongSubjects = Map.of(
                "History", false,
                "Math", false,
                "English", false,
                "Science", false
        );

    }
}
