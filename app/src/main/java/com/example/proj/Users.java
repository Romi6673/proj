package com.example.proj;

import java.util.HashMap;
import java.util.Map;/**
 * Represents a user profile within the application.
 * <p>
 * This class serves as a data model for the Firebase Realtime Database,
 * storing authentication details, profile information, and the user's
 * academic strengths and weaknesses.
 */
public class Users {

    /** The user's registered email address. */
    public String email;

    /** Unique identifier provided by Firebase Authentication. */
    public String userId;

    /** The display name chosen by the user. */
    public String userName;

    /** A short personal description or biography. */
    public String bio;

    /** The user's account password. */
    public String password;

    /** URL pointing to the user's profile image in Firebase Storage. */
    public String profilePicUrl;

    /** Total points accumulated by the user (e.g., for helping others). */
    public int score;

    /**
     * A map representing subjects the user finds difficult.
     * Key: Subject name (e.g., "Math").
     * Value: Boolean (true if the user needs help in this subject).
     */
    public Map<String, Boolean> weakSubjects;

    /**
     * A map representing subjects the user is proficient in.
     * Key: Subject name.
     * Value: Boolean (true if the user can help others in this subject).
     */
    public Map<String, Boolean> strongSubjects;

    /**
     * Default constructor required for calls to DataSnapshot.getValue(Users.class).
     * Necessary for Firebase Realtime Database serialization.
     */
    public Users() {
    }

    /**
     * Constructs a new User profile with default values.
     * <p>
     * Initializes the bio, profile picture, and score to default starting values.
     * Also initializes the subject maps with a standard set of subjects set to false.
     *
     * @param email    The user's email address.
     * @param password The user's chosen password.
     * @param userId   The unique Firebase UID.
     */
    public Users(String email, String password, String userId) {
        this.email = email;
        this.userId = userId;
        this.userName = "";
        this.password = password;
        this.bio = "no bio yet";
        this.profilePicUrl = "";
        this.score = 10;

        // Use HashMap specifically to ensure the map is mutable after retrieval
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
}