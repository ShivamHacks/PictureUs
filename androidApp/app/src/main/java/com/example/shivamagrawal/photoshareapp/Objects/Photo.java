package com.example.shivamagrawal.photoshareapp.Objects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.text.SimpleDateFormat;

public class Photo implements Parcelable {

    private String url;
    private String capturedAt;
    private String capturedBy;

    private long capturedAtNum;

    private Context context;
    private List<Contact> contacts;

    SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM dd, yyyy");

    public Photo(JSONObject photo,
                 Context context, List<Contact> contacts) {

        this.context = context;
        this.contacts = contacts;

        try {
            url = photo.getString("url");
            capturedAtNum = photo.getLong("capturedAt");
            capturedAt = getDate(photo.getLong("capturedAt"));
            capturedBy = getContact(photo.getString("capturedBy"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public Photo(Parcel in){
        this.url = in.readString();
        this.capturedAt = in.readString();
        this.capturedBy = in.readString();
    }

    public String getUrl() { return url; }
    public String getCapturedAt() { return capturedAt; }
    public String getCapturedBy() { return capturedBy; }

    public long getCapturedAtNum() { return capturedAtNum; }

    private String getContact(String number) {
        for (Contact c: contacts) {
            if (number.equals(c.getNumber())
                    || number.equals(ContactsHelper
                    .internationalize(context, c.getNumber()))) {
                return c.getName();
            }
        }
        return number;
    }

    private String getDate(long milliseconds) {
        return sdf.format(milliseconds);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(url);
        parcel.writeString(capturedAt);
        parcel.writeString(capturedBy);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Photo createFromParcel(Parcel in) { return new Photo(in); }
        public Photo[] newArray(int size) { return new Photo[size]; }
    };
}
