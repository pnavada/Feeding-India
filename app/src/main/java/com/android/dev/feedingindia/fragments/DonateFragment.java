package com.android.dev.feedingindia.fragments;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.Toast;

import com.android.dev.feedingindia.activities.FoodLocation;
import com.android.dev.feedingindia.activities.HungerSpotsMapActivity;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.android.dev.feedingindia.R;
import com.android.dev.feedingindia.pojos.DonationDetails;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;

import static android.app.Activity.RESULT_OK;

public class DonateFragment extends Fragment  {

    private EditText foodDescriptionEditText,foodPreparedOnEditText,additionalContactNumberEditText,
            shelfLifeEditText,noOfPeopleCanBeServedEditText;
    private boolean hasContainer;
    public static double latitude,longitude;
    public static String state,city,donorAddress,pinCode,foodDescription,foodPreparedOn,
            additionalContactNumber,shelfLife,noOfPeopleCanBeServed;
    private SharedPreferences mSharedPreferences;
    private HashMap<String,Object> address;
    private RadioButton hasContainerYesRadioButton,hasContainerNoRadioButton,isVegFood,isNonVegFood;
    private Button submitButton;
    private ImageButton locationButton;
    private DatabaseReference mDatabaseReference;
    private ImageButton mFoodImageButton;
    private FirebaseStorage mFireBaseStorage;
    private StorageReference mPhotoStorageReference;
    private static final int PICK_IMAGE = 100;
    private Uri imageUri;
    private String imageUrl;
    private StorageReference photoRef;
    private String foodType;
    private Handler mHandler;
    private boolean mDoanting;
    public static boolean donorIsDelivering;
    private ProgressDialog mProgressDialog;
    private boolean pendingDelivery;


    public DonateFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations").child(FirebaseAuth.getInstance().getUid());

        mSharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia",Context.MODE_PRIVATE);
        mFireBaseStorage = FirebaseStorage.getInstance();
        mPhotoStorageReference = mFireBaseStorage.getReference().child("Food_photos");
        mHandler = new Handler();


        state = donorAddress = pinCode = city = "";
        hasContainer = true;
        foodType = "veg";
        address = new HashMap<>();
        mDoanting = false;
        donorIsDelivering = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_donate, container, false);
        foodDescriptionEditText = view.findViewById(R.id.foodDescriptionEditText);
        foodPreparedOnEditText = view.findViewById(R.id.foodPreparedOnEditText);
        locationButton = view.findViewById(R.id.locationButton);
        additionalContactNumberEditText = view.findViewById(R.id.additionalContactNumberEditText);
        hasContainerYesRadioButton = view.findViewById(R.id.hasContainerYesRadioButton);
        hasContainerNoRadioButton = view.findViewById(R.id.hasContainerNoRadioButton);
        submitButton = view.findViewById(R.id.submitButton);
        isVegFood = view.findViewById(R.id.vegFood);
        isNonVegFood = view.findViewById(R.id.nonVegFood);
        shelfLifeEditText = view.findViewById(R.id.shelfLifeEditText);
        noOfPeopleCanBeServedEditText = view.findViewById(R.id.noOfPeopleCanFeed);
        mFoodImageButton = view.findViewById(R.id.food_image_button);

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);


        return view;

    }

    private void openGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        gallery.setType("image/*");
        startActivityForResult(gallery,PICK_IMAGE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE &&resultCode == RESULT_OK){
            imageUri = data.getData();
            mFoodImageButton.setImageURI(imageUri);
            photoRef = mPhotoStorageReference.child(imageUri.getLastPathSegment());
        }
        else if(requestCode == 2){
            if(resultCode == 2) {
                if (HomeFragment.loadCollectAndDeliverFragment) {
                    final CollectAndDeliverFragment fragment = new CollectAndDeliverFragment();

                    final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    if (fragmentManager.getBackStackEntryCount() > 0)
                        fragmentManager.popBackStack();

                    Runnable mPendingRunnable = new Runnable() {

                        public void run() {
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frame_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commitAllowingStateLoss();
                        }
                    };

                    mHandler.post(mPendingRunnable);
                }
            }
        }
    }





    @Override
    public void onResume() {

        super.onResume();
        hasContainerYesRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    hasContainer = true;
            }
        });


        hasContainerNoRadioButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked)
                    hasContainer = false;
            }
        });

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubmitButton();
            }
        });

        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickLocationButton();
            }
        });
        mFoodImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        isVegFood.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    foodType = "Veg";
            }
        });
        isNonVegFood.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    foodType = "Non-Veg";
            }
        });

    }

    private void onClickSubmitButton() {

        if (!mDoanting) {

            foodDescription = foodDescriptionEditText.getText().toString().trim();
            foodPreparedOn = foodPreparedOnEditText.getText().toString().trim();
            additionalContactNumber = additionalContactNumberEditText.getText().toString().trim();
            shelfLife = shelfLifeEditText.getText().toString().trim();
            noOfPeopleCanBeServed = noOfPeopleCanBeServedEditText.getText().toString().trim();

            if (foodDescription.isEmpty() || foodPreparedOn.isEmpty() || additionalContactNumber.isEmpty()
                    || shelfLife.isEmpty() || noOfPeopleCanBeServed.isEmpty()) {
                makeToast("Fields cannot be empty!");

            }

            else if (pinCode.isEmpty() || city.isEmpty() || donorAddress.isEmpty() || state.isEmpty())
                makeToast("Please choose your location on the map");
            else {
                mProgressDialog.setMessage("Uploading Image...");
                mProgressDialog.show();

                address.put("city", city);
                address.put("state", state);
                address.put("address", donorAddress);
                address.put("pinCode", pinCode);
                address.put("latitude", Double.toString(latitude));
                address.put("longitude", Double.toString(longitude));

                if (imageUri != null) {
                    mDoanting = true;
                    uploadImage();
                } else {
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    makeToast("Please select the Image of the Food");
                }
            }
        }
    }

    private void uploadImage(){

        UploadTask uploadTask = photoRef.putFile(imageUri);
        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    makeToast("unable to upload image");
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
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
                    makeToast("Image Uploaded");
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    askIfUserCanDeliver();
                } else {
                    if(mProgressDialog.isShowing())
                        mProgressDialog.cancel();
                    makeToast("Image not uploaded");
                }
            }
        });

    }

    private void askIfUserCanDeliver(){

        AlertDialog.Builder mBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity(), android.R.style.Theme_Material_Dialog_Alert);
        } else {
            mBuilder = new android.support.v7.app.AlertDialog.Builder(getActivity());
        }

        mBuilder.setMessage("Can you deliver it yourself?")
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if(!hasContainer){
                            makeToast("You Don't have Container To deliver");
                            mDoanting = false;
                        }else {
                            donorIsDelivering = true;
                            push(true, mSharedPreferences.getString("name", ""));
                            reset();


                        }
                    }
                })
                .setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        push(false,"none");
                        reset();

                    }
                }).setCancelable(false).show();

    }

    private void push(boolean canDonate, String deliverer ){
        DonationDetails donationDetails;
        Long timeStamp = System.currentTimeMillis()/1000;
        if(canDonate){
            donationDetails =
                    new DonationDetails(foodDescription,foodPreparedOn,additionalContactNumber, "pending",
                            mSharedPreferences.getString("mobileNumber",""),deliverer,
                            mSharedPreferences.getString("name",""),
                            mSharedPreferences.getString("mobileNumber",""), hasContainer,
                            canDonate,address,imageUrl,null,foodType,"");
            donationDetails.setDonationUserId(FirebaseAuth.getInstance().getUid());
            donationDetails.setNoPeopleCanBeServed(noOfPeopleCanBeServed);
            donationDetails.setShelfLife(shelfLife);
            String key = mDatabaseReference.push().getKey();
            mDatabaseReference.child(key).setValue(donationDetails);

            Fragment fragment = new FeedFragment();
            FeedFragment.chosenFoodLatLng = new LatLng(latitude,longitude);
            FeedFragment.chosenDonationPushId = key;
            FeedFragment.donorUid = FirebaseAuth.getInstance().getUid();
            FeedFragment.nameOfDonor = mSharedPreferences.getString("name","");
            FeedFragment.phoneNumberOfDonor = mSharedPreferences.getString("mobileNumber","");
            FeedFragment.donationImgUrl = imageUrl;

            Map<String,Object> map = address;
            HashMap<String,String> newMap = new HashMap<>();
            for(Map.Entry<String,Object> entry : map.entrySet()){
                newMap.put(entry.getKey(),entry.getValue().toString());
            }

            FeedFragment.chosenDonationAddress = newMap;

            Intent intent = new Intent(getContext(),HungerSpotsMapActivity.class);
            intent.putExtra("DonationId",key);
            startActivityForResult(intent,2);

        }else{
            donationDetails =
                    new DonationDetails(foodDescription,foodPreparedOn, additionalContactNumber,
                            "pending",mSharedPreferences.getString("mobileNumber",""),
                            deliverer,mSharedPreferences.getString("name",""),
                            "", hasContainer,canDonate,address,imageUrl,
                            null,foodType,"");
            donationDetails.setDonationUserId(FirebaseAuth.getInstance().getUid());
            donationDetails.setTimeStamp(timeStamp);
            donationDetails.setNoPeopleCanBeServed(noOfPeopleCanBeServed);
            donationDetails.setShelfLife(shelfLife);
            mDatabaseReference.push().setValue(donationDetails);
            makeToast("Donation success");
        }
    }

    private void reset(){
        donorAddress = pinCode = city = state = "";
        address.clear();
        foodDescriptionEditText.setText("");
        foodPreparedOnEditText.setText("");
        additionalContactNumberEditText.setText("");
        shelfLifeEditText.setText("");
        noOfPeopleCanBeServedEditText.setText("");
        mFoodImageButton.setImageResource(R.drawable.add_image_click);
        hasContainerNoRadioButton.setChecked(true);
        isVegFood.setChecked(true);
        mDoanting = false;
        if(mProgressDialog.isShowing())
            mProgressDialog.cancel();
    }

    private void onClickLocationButton(){
        Intent intent = new Intent(getContext(),FoodLocation.class);
        startActivity(intent);
    }

    private void makeToast(String message){
        Toast.makeText(getContext(),""+message,Toast.LENGTH_SHORT).show();
    }
    private boolean checkForPendingDelivery(){

        Query mQuery = FirebaseDatabase.getInstance().getReference().
                child("Deliveries").child(FirebaseAuth.getInstance().getUid()).
                orderByChild("status").equalTo("pending");
        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount()==0) {
                    pendingDelivery = true;
                    }else{
                    pendingDelivery = false;
                }
                }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "dataBaseError", Toast.LENGTH_SHORT).show();
            }
        });

        return pendingDelivery;
    }

}