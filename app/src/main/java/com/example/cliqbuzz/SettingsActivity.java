package com.example.cliqbuzz;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private Button UpdateAccountSettings;
    private EditText userName, userStatus;
    private CircleImageView userProfileImage;
    private String currentUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference RooRef;


    private static final int GalleryPick = 1;

    private StorageReference UserProfileImagesRef;
    private ProgressDialog loadingbar;

    private Toolbar SettingsToolBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        RooRef = FirebaseDatabase.getInstance().getReference();

        UserProfileImagesRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);



        UpdateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
              UpdateSettings();
            }
        });



        RetrieveUserInfo();

        userProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
             Intent galleryIntent = new Intent();
             galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
             galleryIntent.setType("image");
             startActivityForResult(galleryIntent,GalleryPick);

            }
        });
    }



    private void InitializeFields()

    {
        UpdateAccountSettings = (Button) findViewById(R.id.update_settings_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        userStatus = (EditText) findViewById(R.id.set_profile_status);
        userProfileImage = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingbar = new ProgressDialog(this);

        SettingsToolBar = (Toolbar) findViewById(R.id.settings_toolbar);
        setSupportActionBar(SettingsToolBar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("Account Settings");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==GalleryPick && resultCode==RESULT_OK && data!=null)

        {
            Uri ImageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                loadingbar.setTitle("Set Profile Image");
                loadingbar.setMessage("Please wait,your rofile image is updating....");
                loadingbar.setCanceledOnTouchOutside(false);
                loadingbar.show();

                Uri resultUri = result.getUri();

                StorageReference filePath = UserProfileImagesRef.child(currentUserID + ".jpg");

                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                       if (task.isSuccessful())
                       {
                           Toast.makeText(SettingsActivity.this, "Profile Image uploaded Sucessfully....", Toast.LENGTH_SHORT).show();

                           final String downloaedUrl = task.getResult().getStorage().getDownloadUrl().toString();

                           RooRef.child("Users").child(currentUserID).child("image")
                           .setValue(downloaedUrl)
                                   .addOnCompleteListener(new OnCompleteListener<Void>()
                                   {
                                       @Override
                                       public void onComplete(@NonNull Task<Void> task)
                                       {
                                        if (task.isSuccessful())
                                        {
                                            Toast.makeText(SettingsActivity.this, "Image save in Database Successfully...", Toast.LENGTH_SHORT).show();
                                            loadingbar.dismiss();

                                        }

                                        else
                                        {
                                            String message = task.getException().toString();
                                            Toast.makeText(SettingsActivity.this, "Error: " + message, Toast.LENGTH_SHORT).show();
                                            loadingbar.dismiss();
                                        }
                                       }
                                   });

                       }


                       else
                       {
                           String message = task.getException().toString();
                           Toast.makeText(SettingsActivity.this, "Error: " +message, Toast.LENGTH_SHORT).show();
                           loadingbar.dismiss();
                       }
                    }
                });
            }

        }


    }

    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please write your Username .....", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please write your Bio .....", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String,Object> profileMap = new HashMap<>();
            profileMap.put("uid",currentUserID);
            profileMap.put("name",setUserName);
            profileMap.put("status",setStatus);

            RooRef.child("Users").child(currentUserID).updateChildren(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                         if (task.isSuccessful())
                         {
                             SendUserToMainActivity();
                             Toast.makeText(SettingsActivity.this, "Profile Updated Sucessfully....", Toast.LENGTH_SHORT).show();
                         }
                           else
                         {
                             String message = task.getException().toString();
                             Toast.makeText(SettingsActivity.this, "Error:"+message, Toast.LENGTH_SHORT).show();
                         }
                        }
                    });
        }
    }



    private void RetrieveUserInfo()
    {
        RooRef.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot datasnapshot)
                    {


                        if ((datasnapshot.exists()) && (datasnapshot.hasChild("name") && (datasnapshot.hasChild("image"))))
                        {
                            String retrieveUserName = datasnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = datasnapshot.child("status").getValue().toString();
                            String retrieveUserProfileImage = datasnapshot.child("image").getValue().toString();

                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);
                            Picasso.get().load(retrieveUserProfileImage).into(userProfileImage);
                        }
                      else if ((datasnapshot.exists()) && (datasnapshot.hasChild("name")))
                        {


                            String retrieveUserName = datasnapshot.child("name").getValue().toString();
                            String retrieveUserStatus = datasnapshot.child("status").getValue().toString();


                            userName.setText(retrieveUserName);
                            userStatus.setText(retrieveUserStatus);


                        }
                        else
                        {
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this, "Please set & update your Profile information....", Toast.LENGTH_SHORT).show();

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }

    private void SendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this,MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}