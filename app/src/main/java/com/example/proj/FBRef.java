package com.example.proj;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

/**
 * A helper class that centralizes static references to Firebase services.
 * <p>
 * This class provides easy access to Firebase Authentication, Realtime Database,
 * and Firebase Storage references used throughout the application.
 */
public class FBRef {

    /**
     * Static reference to the FirebaseAuth instance for managing user login and registration.
     */
    public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

    /**
     * Static reference to the root FirebaseDatabase instance.
     */
    public static FirebaseDatabase proj = FirebaseDatabase.getInstance();

    /**
     * Static reference to the "Users" node in the Firebase Realtime Database.
     * Used for reading/writing user profile information.
     */
    public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");

    /**
     * Static reference to the root Firebase Storage location.
     * Used for managing file uploads, such as profile pictures.
     */
    public static StorageReference refSto = FirebaseStorage.getInstance().getReference();
}
