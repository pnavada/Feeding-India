package com.android.dev.feedingindia.fragments;


import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.dev.feedingindia.R;
import com.android.dev.feedingindia.activities.SimpleDIviderItemDecoration;
import com.android.dev.feedingindia.pojos.FeedingIndiaEvent;
import com.android.dev.feedingindia.adapters.NotificationsAdapter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class NotificationsFragment extends Fragment {
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mDatabaseReference;
    private ArrayList<FeedingIndiaEvent> events ;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private View view;
    private ProgressBar progressBar;


    public NotificationsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference().child("Notifications");
        events = new ArrayList<>();

    }

    private void enableUseInteraction() {
        mAdapter = new NotificationsAdapter(events);

        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
        progressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        mRecyclerView.setNestedScrollingEnabled(false);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_notifications, container, false);
        mRecyclerView = view.findViewById(R.id.notification_recyclerView);
        progressBar = view.findViewById(R.id.progressBar);
        mRecyclerView.addItemDecoration(new SimpleDIviderItemDecoration(getContext()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){
                    enableUseInteraction();
                }
                else{
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                        FeedingIndiaEvent fievent = snapshot.getValue(FeedingIndiaEvent.class);
                        events.add(fievent);
                    }
                    sortList();
                    Toast.makeText(getContext(), "data loaded", Toast.LENGTH_SHORT).show();
                    enableUseInteraction();
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
        events.clear();
    }

    public void sortList(){
        if(events != null){
            Collections.sort(events, new Comparator<FeedingIndiaEvent>() {
                @Override
                public int compare(FeedingIndiaEvent o1, FeedingIndiaEvent o2) {
                    return Long.valueOf( o2.getTimeStamp()).compareTo(o1.getTimeStamp());
                }
            });
        }
    }
}