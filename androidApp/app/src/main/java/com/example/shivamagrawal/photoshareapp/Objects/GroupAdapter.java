package com.example.shivamagrawal.photoshareapp.Objects;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ImageButton;
import android.content.Context;
import android.util.Log;
import java.util.List;

import com.chauthai.swipereveallayout.SwipeRevealLayout;
import com.chauthai.swipereveallayout.ViewBinderHelper;
import com.example.shivamagrawal.photoshareapp.CameraActivity;
import com.example.shivamagrawal.photoshareapp.GalleryActivity;
import com.example.shivamagrawal.photoshareapp.GroupSettingsActivity;
import com.example.shivamagrawal.photoshareapp.R;

public class GroupAdapter extends ArrayAdapter<Group> {

    private final LayoutInflater mInflater;
    private final ViewBinderHelper binderHelper;
    private Context context;

    public GroupAdapter(Context context, List<Group> objects) {
        super(context, R.layout.group_list_item_layout, objects);
        mInflater = LayoutInflater.from(context);
        binderHelper = new ViewBinderHelper();
        this.context = context;

        // uncomment if you want to open only one row at a time
        binderHelper.setOpenOnlyOne(true);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.group_list_item_layout, parent, false);

            holder = new ViewHolder();
            holder.textView = (TextView) convertView.findViewById(R.id.group_item_name);
            holder.actionsView = convertView.findViewById(R.id.group_actions);
            holder.swipeLayout = (SwipeRevealLayout) convertView.findViewById(R.id.swipe_layout);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Group group = getItem(position);
        if (group != null) {
            binderHelper.bind(holder.swipeLayout, group.getID());
            holder.textView.setText(group.getName());
            // Buttons
            ImageButton actionPhoto = (ImageButton) holder.actionsView.findViewById(R.id.group_action_photo);
            ImageButton actionGallery = (ImageButton) holder.actionsView.findViewById(R.id.group_action_gallery);
            ImageButton actionSettings = (ImageButton) holder.actionsView.findViewById(R.id.group_action_settings);

            // Button actions
            actionPhoto.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("PHOTO ACTION", "" + position);
                    Intent capture = new Intent(context, CameraActivity.class);
                    capture.putExtra("groupID", group.getID());
                    context.startActivity(capture);
                }
            });
            actionGallery.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("GALLERY ACTION", "" + position);
                    Intent gallery = new Intent(context, GalleryActivity.class);
                    gallery.putExtra("groupID", group.getID());
                    gallery.putExtra("groupName", group.getName());
                    context.startActivity(gallery);
                }
            });
            actionSettings.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d("SETTINGS ACTION", "" + position);
                    Intent settings = new Intent(context, GroupSettingsActivity.class);
                    settings.putExtra("groupID", group.getID());
                    settings.putExtra("groupName", group.getName());
                    ((Activity) context).startActivityForResult(settings, 1);
                }
            });
            // also do button listeners here
        }

        return convertView;
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     */
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     */
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }

    private class ViewHolder {
        TextView textView;
        View actionsView;
        SwipeRevealLayout swipeLayout;
    }

}
