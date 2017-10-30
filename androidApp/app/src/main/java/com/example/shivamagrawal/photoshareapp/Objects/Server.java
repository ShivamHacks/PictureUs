package com.example.shivamagrawal.photoshareapp.Objects;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Server {

    private static String baseURL = "http://10.0.0.11:3000";
    //private static String baseURL = "http://pictureus.herokuapp.com";
    private static String getFullURL(String extension) { return baseURL + extension; }

    // Users
    public static String signupURL = getFullURL("/api/users/signup");
    public static String loginURL = getFullURL("/api/users/login");
    public static String verifyURL = getFullURL("/api/users/verify");
    public static String deleteAccountURL = getFullURL("/api/users/deleteAccount");

    // Groups and events
    public static String createGroupURL = getFullURL("/api/groups/createGroup");
    public static String getAllGroupsURL = getFullURL("/api/groups/getAllGroups");
    public static String getGroupInfoURL = getFullURL("/api/groups/getGroupInfo");
    public static String editGroupURL = getFullURL("/api/groups/editGroup");
    public static String leaveGroupURL = getFullURL("/api/groups/leaveGroup");

    // Photos
    public static String uploadPhotoURL = getFullURL("/api/photos/upload");
    public static String getAllPhotosURL = getFullURL("/api/photos/getAll");

    // StringRequest Generators

    public static StringRequest POST(final Map<String, String> params, String url,
                                     Response.Listener<String> cbSuccess, Response.ErrorListener cbError) {
        return new StringRequest(Request.Method.POST, url, cbSuccess, cbError){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                return params;
            }
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type","application/x-www-form-urlencoded");
                return headers;
            }
        };
    }

    public static StringRequest GET(final Map<String, String> params, String url,
                                    Response.Listener<String> cbSuccess, Response.ErrorListener cbError) {
        return new StringRequest(Request.Method.GET, url, cbSuccess, cbError) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = params;
                headers.put("Content-Type","application/x-www-form-urlencoded");
                return headers;
            }
        };
    }

    public static void makeRequest(Context context, StringRequest sr) {
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(sr);
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPref = context.getSharedPreferences("main", Context.MODE_PRIVATE);
        return sharedPref.getString("token", null);
    }

}
