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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
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

/**
 * Fragment responsible for displaying and editing the user's profile.
 * <p>
 * Features include:
 * 1. Uploading and displaying a profile picture using Firebase Storage and Glide.
 * 2. Editing username via a custom AlertDialog.
 * 3. Managing "Weak" and "Strong" subjects via custom adapters and Spinners.
 * 4. Persisting a short user biography to Firebase Realtime Database.
 */
public class ProfileFragment extends Fragment {

    /** URI of the image selected from the local gallery. */
    Uri selectedImageUri;

    /** Root view of the fragment. */
    View view;

    /** UI component references. */
    Button userNameEditBtn, saveBioBtn;
    EditText bioEditText, edit_text_dialog;
    ImageButton profilePictureBtn;
    Spinner weakSubSpinner, strongSubSpinner;
    TextView tvScore;
    LinearLayout dialog_username;

    /** The unique ID of the currently authenticated user. */
    String userId;

    /** Request codes for permissions and gallery intent. */
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int REQUEST_READ_STORAGE = 2000;

    /** Builder for the username update dialog. */
    AlertDialog.Builder adb;

    /** Lists of available subjects to be displayed in Spinners. */
    public String[] subjectsWeakArr = { "History" , "Math" , "English", "Science"};
    public String[] subjectsStrongArr = { "History" , "Math" , "English", "Science"};

    /** Boolean arrays tracking which subjects are currently selected by the user. */
    public boolean[] subjectsWeakBool = {false, false, false, false};
    public boolean[] subjectsStrongBool = {false, false, false, false};

    /**
     * Handles the result of the runtime permission request for storage access.
     */
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

    /**
     * Callback received when the user selects an image from the gallery.
     * Updates the UI immediately and initiates the Firebase upload.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST
                && resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {

            selectedImageUri = data.getData();
            profilePictureBtn.setImageURI(selectedImageUri); // Local preview for better UX
            uploadImageToStorage();
        }
    }

    /**
     * Uploads the selected image to Firebase Storage and updates the
     * profilePicUrl in the Realtime Database upon completion.
     */
    private void uploadImageToStorage() {
        if (selectedImageUri == null) {
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
            return;
        }

        StorageReference galleryRef = refSto.child("Pictures/" + userId + ".jpg");

        ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Uploading...");
        progressDialog.show();

        galleryRef.putFile(selectedImageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Get the public download URL after successful upload
                    galleryRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();

                        // Save the URL to the user's database node
                        refUsers.child(userId).child("profilePicUrl").setValue(downloadUrl)
                                .addOnCompleteListener(task -> {
                                    progressDialog.dismiss();
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getContext(), "Profile picture updated!", Toast.LENGTH_SHORT).show();
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

    /**
     * Launches the system intent to pick an image from the device storage.
     */
    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Checks for necessary storage permissions based on Android version.
     * Required for API levels below Tiramisu (Android 13).
     */
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

    /**
     * Initializes the fragment UI, verifies authentication, and loads user data from Firebase.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Security check: Redirect to login if session is invalid
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            startActivity(new Intent(getActivity(), LoginActivity.class));
            getActivity().finish();
            return view;
        }

        // Initialize UI components
        userNameEditBtn = view.findViewById(R.id.userNameEditBtn);
        profilePictureBtn = view.findViewById(R.id.profilePictureBtn);
        TextView tvUsername = view.findViewById(R.id.tvUsername);
        TextView tvScore = view.findViewById(R.id.tvScore);
        bioEditText = view.findViewById(R.id.bioEditText);
        saveBioBtn = view.findViewById(R.id.saveBioBtn);

        // Setup Subject Adapters
        weakSubSpinner = view.findViewById(R.id.weakSubSpinner);
        strongSubSpinner = view.findViewById(R.id.strongSubSpinner);

        custom_adapter_weak customAdapterWeak = new custom_adapter_weak(getContext(), subjectsWeakArr, subjectsWeakBool);
        weakSubSpinner.setAdapter(customAdapterWeak);

        custom_adapter_strong customAdapterStrong = new custom_adapter_strong(getContext(), subjectsStrongArr, subjectsStrongBool);
        strongSubSpinner.setAdapter(customAdapterStrong);

        // Setup Username Dialog View
        dialog_username = (LinearLayout) getLayoutInflater().inflate(R.layout.dialog_username, null);
        edit_text_dialog = dialog_username.findViewById(R.id.edit_text_dialog);

        // Load existing user data from Firebase Realtime Database
        refUsers.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users u = snapshot.getValue(Users.class);
                if (u != null) {
                    tvUsername.setText(u.userName);
                    bioEditText.setText(u.bio);
                    tvScore.setText("Score: " + u.score);

                    // Load profile image using Glide
                    if (isAdded() && getContext() != null && !isDetached()) {
                        if (u.profilePicUrl != null && !u.profilePicUrl.isEmpty()) {
                            Glide.with(getContext()).load(u.profilePicUrl).into(profilePictureBtn);
                        }
                    }

                    // Sync Weak Subjects state
                    if (u.weakSubjects != null) {
                        for (int i = 0; i < subjectsWeakArr.length; i++) {
                            Boolean isSelected = u.weakSubjects.get(subjectsWeakArr[i]);
                            subjectsWeakBool[i] = (isSelected != null && isSelected);
                        }
                        if (weakSubSpinner.getAdapter() != null) {
                            ((custom_adapter_weak) weakSubSpinner.getAdapter()).notifyDataSetChanged();
                        }
                    }

                    // Sync Strong Subjects state
                    if (u.strongSubjects != null) {
                        for (int i = 0; i < subjectsStrongArr.length; i++) {
                            Boolean isSelected = u.strongSubjects.get(subjectsStrongArr[i]);
                            subjectsStrongBool[i] = (isSelected != null && isSelected);
                        }
                        if (strongSubSpinner.getAdapter() != null) {
                            ((custom_adapter_strong) strongSubSpinner.getAdapter()).notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
            }
        });

        // Setup Username Update Dialog
        adb = new AlertDialog.Builder(this.getContext());
        adb.setView(dialog_username);
        adb.setTitle("Update Username");

        adb.setPositiveButton("save", (dialog, which) -> {
            String newUserName = edit_text_dialog.getText().toString();
            tvUsername.setText(newUserName);
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId).child("userName").setValue(newUserName);

            // Workaround: Manually remove the view from parent to allow re-inflating in future calls
            if (dialog_username.getParent() != null) {
                ((ViewGroup) dialog_username.getParent()).removeView(dialog_username);
            }
        });

        userNameEditBtn.setOnClickListener(v -> {
            // Ensure the view is detached from any previous dialog before showing again
            if (dialog_username.getParent() != null) {
                ((ViewGroup) dialog_username.getParent()).removeView(dialog_username);
            }
            adb.show();
        });

        profilePictureBtn.setOnClickListener(v -> checkPermissionAndOpenGallery());

        saveBioBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance().getReference("Users")
                    .child(userId).child("bio").setValue(bioEditText.getText().toString());
            Toast.makeText(getContext(), "Bio saved", Toast.LENGTH_SHORT).show();
        });

        return view;
    }
}