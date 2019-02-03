package com.android.dev.feedingindia.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.android.dev.feedingindia.R;
import com.android.dev.feedingindia.pojos.GlideApp;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.FileOutputStream;


public class EnlargeImageViewActivity extends AppCompatActivity {
    private String mImageUrl;
    private String mType;
    PhotoView photoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarge_image_view);
        Toolbar toolbar = findViewById(R.id.image_toolbar);
        setSupportActionBar(toolbar);
        photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setImageResource(R.drawable.no_image);
        Bundle bundle = getIntent().getExtras();
       mImageUrl = bundle.getString("ImageUrl", "");
       mType = bundle.getString("type","");
       if(mImageUrl != null){
         GlideApp.with(this).load(mImageUrl).fitCenter().override(480,360).into(photoView);
      }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.enlarge_image_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
            if(item.getItemId() == R.id.share_image){
                OnClickShare();
            }
        return super.onOptionsItemSelected(item);
    }

        @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void OnClickShare(){

        Bitmap bitmap =getBitmapFromView(photoView);
        try {
            File file = new File(this.getExternalCacheDir(),"logicchip.png");
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();
            file.setReadable(true, false);
            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
            intent.setType("image/*");
            startActivity(Intent.createChooser(intent, "Share image via"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap getBitmapFromView(View view) {
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(),Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable!=null) {
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        }   else{
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return returnedBitmap;
    }


}