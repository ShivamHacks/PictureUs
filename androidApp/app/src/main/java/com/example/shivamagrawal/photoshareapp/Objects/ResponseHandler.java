package com.example.shivamagrawal.photoshareapp.Objects;

import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.widget.Toast;

public class ResponseHandler {

    private Context context;
    private String res;
    private JSONObject body;

    public ResponseHandler(Context context, String res) {
        this.context = context;
        this.res = res;
    }

    public JSONObject parseRes() {
        try {
            JSONObject results = new JSONObject(res);
            if (results.getBoolean("success")) {
                return results;
            } else if (!results.getBoolean("success")) {
                errorToast(context, results.getString("message"));
                return null;
            } else {
                errorToast(context, "An error occured");
                return null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void errorToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
