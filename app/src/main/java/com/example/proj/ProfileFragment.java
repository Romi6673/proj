package com.example.proj;

import static com.example.proj.FBRef.refSto;
import static com.example.proj.FBRef.refUsers;
import android.widget.ArrayAdapter;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.Manifest;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Map;

public class ProfileFragment extends Fragment {

    Uri selectedImageUri;


    View view;
    Button userNameEditBtn;
    Button saveBioBtn;
    String userId;
    EditText bioEditText;
    ImageButton profilePictureBtn;
    Spinner weakSubSpinner;
    Spinner strongSubSpinner;
    String nameStr;
    String bioStr;
    LinearLayout dialog_username;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_STORAGE = 2000;
    EditText edit_text_dialog; // זה המשתנה שיחזיק את השדה מהדיאלוג
    AlertDialog.Builder adb;

    public String[] subjectsWeakArr = { "History" , "Math" , "English", "Science"};
    public String[] subjectsStrongArr = { "History" , "Math" , "English", "Science"};

    public boolean[] subjectsWeakBool = {false, false, false, false};
    public boolean[] subjectsStrongBool = {false, false, false, false};




    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                Toast.makeText(getContext(), "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            // הצגת התמונה בכפתור מיד (לפני ההעלאה, בשביל חווית משתמש טובה)
            profilePictureBtn.setImageURI(selectedImageUri);

            // עכשיו מעלים
            uploadImageToStorage();
        }
    }

    private void uploadImageToStorage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        // 1. נתיב ייחודי לכל משתמש (שימוש ב-userId)
        StorageReference galleryRef = refSto.child("Pictures/" + userId + ".jpg");

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        // 2. העלאת הקובץ
        galleryRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {

                    // 3. הצלחה! עכשיו מושכים את ה-URL של התמונה שהרגע עלתה
                    galleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // 4. שמירת הלינק ב-Database תחת המשתמש הנוכחי
                        refUsers.child(userId).child("profilePicUrl").setValue(downloadUrl)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Profile picture updated successfully!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(getContext(), "Database update failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    });
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    Toast.makeText(getContext(), "Upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    public void clickedDownload() {
        StorageReference refFile = refSto.child("Pictures/pictures.jpg");

        final long MAX_SIZE = 1024 * 1024; // 1MB

        refFile.getBytes(MAX_SIZE)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        profilePictureBtn.setImageBitmap(bitmap);
                        Toast.makeText(getContext(),
                                "Download successful",
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(),
                                "Download failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST);
    }


    public void checkPermissionAndOpenGallery() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }





    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);


        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            // כאן כדאי להחזיר את המשתמש למסך התחברות אם הוא לא מחובר
            return view;
        }
        userNameEditBtn = view.findViewById(R.id.userNameEditBtn);
        profilePictureBtn = view.findViewById(R.id.profilePictureBtn);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvScore = view.findViewById(R.id.tvScore);



        weakSubSpinner = view.findViewById(R.id.weakSubSpinner);
        strongSubSpinner = view.findViewById(R.id.strongSubSpinner);

        custom_adapter_weak customAdapterWeak = new custom_adapter_weak (getContext(), subjectsWeakArr, subjectsWeakBool);
        weakSubSpinner.setAdapter(customAdapterWeak) ;

        custom_adapter_strong customAdapterStrong = new custom_adapter_strong (getContext(), subjectsStrongArr, subjectsStrongBool);
        strongSubSpinner.setAdapter(customAdapterStrong) ;




        saveBioBtn = view.findViewById(R.id.saveBioBtn);
        bioEditText = view.findViewById(R.id.bioEditText); //כשמתעסקים עם שמירה בפיירבייס ,
        // לאחר לחיצה על כפתור  save יש לשמור את הביו על פרטי המשתמש

        ImageButton profilePictureBtn = view.findViewById(R.id.profilePictureBtn);

        dialog_username = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_username, null);

        edit_text_dialog = dialog_username.findViewById(R.id.edit_text_dialog);


        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();



        refUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users user = snapshot.getValue(Users.class);
                if (user != null) {
                    // מעדכנים את ה-UI רק כאן, כשיש לנו את הנתונים
                    tvUsername.setText(user.userName);
                    bioEditText.setText(user.bio);



                    // בתוך ה-onDataChange:
                    Users u = snapshot.getValue(Users.class);
                    if (u != null) {
                        tvUsername.setText(u.userName);
                        bioEditText.setText(u.bio);

                        // הצגת התמונה בעזרת Glide (אם השדה לא ריק)
                        if (u.profilePicUrl != null && !u.profilePicUrl.isEmpty()) {
                            Glide.with(getContext())
                                    .load(u.profilePicUrl)
                                    .into(profilePictureBtn);
                        }
                    }



                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
           });





        adb = new AlertDialog.Builder(this.getContext());
        adb.setView(dialog_username);
        adb.setTitle("Update Username");


        adb.setPositiveButton("save", (dialog, which) -> {
            String newUserName = edit_text_dialog.getText().toString();
            tvUsername.setText(newUserName);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId)
                    .child("userName")
                    .setValue(newUserName);

            ((ViewGroup) dialog_username.getParent()).removeView(dialog_username);
        });

        userNameEditBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dialog_username.getParent() != null) {
                    ((ViewGroup) dialog_username.getParent()).removeView(dialog_username);
                }
                adb.show();
            }
        });

        profilePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                checkPermissionAndOpenGallery();
            }
        });


        saveBioBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseDatabase.getInstance().getReference("Users")
                        .child(userId)
                        .child("bio")
                        .setValue(bioEditText.getText().toString());

            }
        });


        return view;


    }
}