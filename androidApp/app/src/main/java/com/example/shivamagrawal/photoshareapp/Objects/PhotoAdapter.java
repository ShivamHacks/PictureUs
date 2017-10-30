package com.example.shivamagrawal.photoshareapp.Objects;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.support.v4.view.*;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;
import android.util.Log;
import android.widget.TextView;

import java.net.URL;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.Toast;
import android.app.Activity;

import com.bumptech.glide.Glide;
import com.example.shivamagrawal.photoshareapp.R;

import mehdi.sakout.fancybuttons.FancyButton;

public class PhotoAdapter extends PagerAdapter {

    Context context;
    LayoutInflater inflater;
    List<Photo> photos;

    ContentResolver cr;

    public PhotoAdapter(Context context, List<Photo> photos) {
        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.photos = photos;
        cr = context.getContentResolver();
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public RelativeLayout instantiateItem(ViewGroup container, int position) {
        RelativeLayout photoView = (RelativeLayout) inflater
                .inflate(R.layout.photo_view, container, false);

        final Photo photo = photos.get(position);

        ImageView imageView = (ImageView) photoView.findViewById(R.id.photo_imageview);
        Glide.with(context).load(photo.getUrl()).fitCenter().into(imageView);

        TextView capturedBy = (TextView) photoView.findViewById(R.id.photo_capturedBy);
        capturedBy.setText(photo.getCapturedBy() + " on "
                + photo.getCapturedAt());

        final ImageView savePhoto = (ImageView) photoView.findViewById(R.id.save_photo_button);
        savePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                savePhoto(photo);
            }
        });

        container.addView(photoView);
        return photoView;
    }

    private void savePhoto(final Photo photo) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    CapturePhotoUtils.insertImage(cr, photo);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "Photo saved!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((RelativeLayout) object);
    }
}