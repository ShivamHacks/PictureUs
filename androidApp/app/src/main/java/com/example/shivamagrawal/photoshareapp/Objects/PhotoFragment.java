package com.example.shivamagrawal.photoshareapp.Objects;


import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.content.pm.ActivityInfo;
import java.util.ArrayList;
import com.example.shivamagrawal.photoshareapp.R;

public class PhotoFragment extends DialogFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        RelativeLayout view = (RelativeLayout) inflater
                .inflate(R.layout.photo_view_pager_layout, container, false);

        ViewPager slideShow = (ViewPager) view.findViewById(R.id.photo_view_pager);
        ImageButton backButton = (ImageButton) view.findViewById(R.id.finish_photo_pager);

        ArrayList<Photo> photos = getArguments().getParcelableArrayList("photos");
        int currentPosition = getArguments().getInt("position");

        PhotoAdapter adapter = new PhotoAdapter(getActivity(), photos);
        slideShow.setAdapter(adapter);
        slideShow.setCurrentItem(currentPosition);

        final PhotoFragment thisFragment = this;

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction().remove(thisFragment).commit();
            }
        });

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL,
                android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        setShowsDialog(true);
    }


}
