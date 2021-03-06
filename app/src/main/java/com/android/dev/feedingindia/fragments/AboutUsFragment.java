package com.android.dev.feedingindia.fragments;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.android.dev.feedingindia.R;

public class AboutUsFragment extends Fragment {


    private ImageButton facebookIcon,twitterIcon, instagramIcon;
    View view;
    public AboutUsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view =  inflater.inflate(R.layout.fragment_about_us, container, false);
        facebookIcon = view.findViewById(R.id.facebook_icon);
        twitterIcon = view.findViewById(R.id.twitter_icon);
        instagramIcon=view.findViewById(R.id.instagram_icon);
        facebookIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/feedingindia"));
                startActivity(intent);
            }
        });

        twitterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/FeedingIndia"));
                startActivity(intent);
            }
        });

        instagramIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.instagram.com/feedingindia/"));
                startActivity(intent);
            }
        });

        return  view;
    }

}
