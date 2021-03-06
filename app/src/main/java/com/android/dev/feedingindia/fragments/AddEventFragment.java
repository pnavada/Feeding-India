package com.android.dev.feedingindia.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.dev.feedingindia.R;
import com.android.dev.feedingindia.pojos.FeedingIndiaEvent;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static android.app.Activity.RESULT_OK;

public class AddEventFragment extends Fragment {

    private View view;
    private EditText mEventName;
    private EditText mEventDescription;
    private ImageButton mEventImage;
    private Button mAddEventButton;
    private String eventName;
    private String eventDescription;
    private FirebaseDatabase mFirebaseDataBase;
    private DatabaseReference mDatabaseReference;
    private StorageReference mStroageReference;
    private FirebaseStorage mFirebaseStorage;
    private Uri imageUri;
    private String imageUrl;
    private static final int PICK_IMAGE = 100;
    private boolean eventUploading;

    private StorageReference photoRef;
    private ProgressDialog mProgressDialog;


    public AddEventFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDataBase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDataBase.getReference().child("Notifications");
        mFirebaseStorage = FirebaseStorage.getInstance();
        mStroageReference = mFirebaseStorage.getReference().child("notification_photos");
        eventUploading = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_add_event, container, false);
        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEventName = view.findViewById(R.id.event_name);
        mEventDescription = view.findViewById(R.id.event_description);
        mEventImage = view.findViewById(R.id.event_image);
        mAddEventButton = view.findViewById(R.id.add_event_button);
        mAddEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });
        mEventImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

    }

    public void selectImage(){

        Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/jpeg");
        startActivityForResult(gallery,PICK_IMAGE);

    }



    public void addEvent(){



        if (isEmpty()) {
                MakeToast("please enter event name and event description");
                return;
            }
        if(!eventUploading) {
            eventUploading = true;
            if (photoRef != null) {
                mProgressDialog.setMessage("Uploading Image...");
                mProgressDialog.show();
                UploadTask uploadTask = photoRef.putFile(imageUri);
                Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            if(mProgressDialog.isShowing())
                            mProgressDialog.dismiss();
                            Toast.makeText(getContext(), "unable to upload image", Toast.LENGTH_SHORT).show();
                            throw task.getException();
                        }
                        return photoRef.getDownloadUrl();
                    }
                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()) {
                            Uri downloadUri = task.getResult();
                            imageUrl = downloadUri.toString();
                            Toast.makeText(getContext(), "Image Uploaded", Toast.LENGTH_SHORT).show();
                            addToDatabase();
                            if(mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                        } else {
                            if(mProgressDialog.isShowing())
                                mProgressDialog.dismiss();
                            Toast.makeText(getContext(), "Image not uploaded", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                addToDatabase();
                if(mProgressDialog.isShowing())
                    mProgressDialog.dismiss();
            }

        }


    }

    public void addToDatabase(){
        eventName = mEventName.getText().toString();
        eventDescription = mEventDescription.getText().toString();
        FeedingIndiaEvent feedingIndiaEvent = new FeedingIndiaEvent(eventName,eventDescription,imageUrl);
        Long timeStamp = System.currentTimeMillis()/1000;
        feedingIndiaEvent.setTimeStamp(timeStamp);
        mDatabaseReference.push().setValue(feedingIndiaEvent);
        MakeToast("Event added");
        eventUploading = false;
    }
    public boolean isEmpty(){
        if(mEventName.getText().toString().trim().isEmpty() || mEventDescription.getText().toString().trim().isEmpty()){
            return true;
        }else {
            return false;
        }
    }

    public void MakeToast(String infoString){
        Toast.makeText(getContext(), ""+infoString, Toast.LENGTH_SHORT).show();
        mEventName.setText("");
        mEventDescription.setText("");
        mEventImage.setImageResource(R.drawable.add_image_click);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            imageUri = data.getData();
            mEventImage.setImageURI(imageUri);
            photoRef =
                    mStroageReference.child(imageUri.getLastPathSegment());

        }
    }





}