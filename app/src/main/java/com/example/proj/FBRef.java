package com.example.proj;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage; // ייבוא חדש
import com.google.firebase.storage.StorageReference; // ייבוא חדש
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class FBRef {

        public static FirebaseAuth refAuth = FirebaseAuth.getInstance();

        public static FirebaseDatabase proj = FirebaseDatabase.getInstance();
        public static DatabaseReference refUsers = FirebaseDatabase.getInstance().getReference("Users");
        public static StorageReference refSto = FirebaseStorage.getInstance().getReference();

    }
