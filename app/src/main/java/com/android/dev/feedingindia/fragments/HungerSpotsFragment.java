package com.android.dev.feedingindia.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.dev.feedingindia.R;
import com.android.dev.feedingindia.pojos.HungerSpot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HungerSpotsFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private ProgressBar progressBar;
    private DatabaseReference mDatabaseReference,mHungerSpotDataBaseReference;
    private String userName;
    private ArrayList<Location> mHungerSpots,mExistingHungerSpots;
    private LinearLayout mLinearLayout;
    private GoogleMap mGoogleMap;
    private boolean mMarkerAdded = false;
    private LatLng mChoosenLatLng;
    private Button mSubmitButton;
    private SharedPreferences mSharedPreferences;



    public HungerSpotsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);
        userName = mSharedPreferences.getString("name","");
        mHungerSpots = new ArrayList<>();
        mExistingHungerSpots = new ArrayList<>();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("HungerSpots");
        mHungerSpotDataBaseReference = FirebaseDatabase.getInstance().getReference().child("HungerSpots");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hunger_spots, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.hunger_spot_container);

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.INVISIBLE);
        mHungerSpotDataBaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){
                    enableUserInteraction();
                }else{
                for(DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    HungerSpot hungerSpot = snapshot.getValue(HungerSpot.class);
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLatitude(hungerSpot.getLatitude());
                    location.setLongitude(hungerSpot.getLongitude());
                    if (hungerSpot.getStatus().equals("validated")){
                        if ((hungerSpot.getAddedBy().equals(userName))) {
                            mHungerSpots.add(location);
                        } else {
                            mExistingHungerSpots.add(location);
                        }
                    }
                }
                Log.i("marked by user",mHungerSpots.toString());
                Log.i("already present",mExistingHungerSpots.toString());
                enableUserInteraction();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        mHungerSpots.clear();
        mExistingHungerSpots.clear();

    }

    private void enableUserInteraction()
    {
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        addMarker();

    }


    private void addHungerSpot(double latitude, double longitude)
    {
        String role = mSharedPreferences.getString("userType","");
        HungerSpot hungerSpot;
        if(role.equals("admin") || role.equals("superadmin")){
            hungerSpot = new HungerSpot(userName,"validated",latitude,longitude);
        }else {
            hungerSpot = new HungerSpot(userName, "pending", latitude, longitude);
            hungerSpot.setUserRole(mSharedPreferences.getString("userType",""));
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Date date = new Date();
            String addedOn = formatter.format(date);
            hungerSpot.setAddedOn(addedOn);
        }
        mDatabaseReference.push().setValue(hungerSpot);
        makeToast("Success! HungerSpot added");
    }

    private void makeToast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.hungerSpotMark);
        mapFragment.getMapAsync(this);
        mSubmitButton = view.findViewById(R.id.hungerSpotSubmitButton);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMarkerAdded && mChoosenLatLng != null){
                    addHungerSpot(mChoosenLatLng.latitude,mChoosenLatLng.longitude);
                }
            }
        });


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        mMarkerAdded = true;
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f));
        mGoogleMap.addMarker(markerOptions).setTitle("Hunger Spot Just Marked");
        mChoosenLatLng = latLng;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        LatLng location = new LatLng(12.971758,77.593712);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,10f));
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }else {
            mGoogleMap.setMyLocationEnabled(true);
        }
        mGoogleMap.setOnMapLongClickListener(this);
    }

    public void addMarker() {
        if (mHungerSpots != null && mHungerSpots.size() > 0) {
            for (int i = 0; i < mHungerSpots.size(); i++) {
                if(mHungerSpots.get(i) != null) {
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mHungerSpots.get(i).getLatitude(),
                                    mHungerSpots.get(i).getLongitude()))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    marker.setTitle("Hunger Spot I Spotted");
                }
            }
        }
        if(mExistingHungerSpots!= null && mExistingHungerSpots.size() > 0){
            for(int i = 0 ; i< mExistingHungerSpots.size();i++){
                if(mExistingHungerSpots.get(i) != null){
                    Marker marker = mGoogleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mExistingHungerSpots.get(i).getLatitude(),
                                    mExistingHungerSpots.get(i).getLongitude())).icon(BitmapDescriptorFactory
                                    .defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    marker.setTitle("Existing Hunger Spots");
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }


}