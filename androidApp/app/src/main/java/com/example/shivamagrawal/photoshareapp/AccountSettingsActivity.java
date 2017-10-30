package com.example.shivamagrawal.photoshareapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.support.v7.app.AlertDialog;
import android.content.DialogInterface;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class AccountSettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    FancyButton logoutButton;
    FancyButton deleteAccount;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_settings);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.account_settings_activity_tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        logoutButton = (FancyButton) findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent returnData = new Intent();
                returnData.putExtra("logout", true);
                setResult(RESULT_OK, returnData);
                clearPrefs();
            }
        });

        deleteAccount = (FancyButton) findViewById(R.id.delete_account_button);

        deleteAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).setTitle("Delete Account?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                deleteAccount();

                                Intent returnData = new Intent();
                                returnData.putExtra("deleteAccount", true);
                                setResult(RESULT_OK, returnData);
                                clearPrefs();
                            }
                        }).setNegativeButton(android.R.string.no, null).create().show();
            }
        });

    }

    private void clearPrefs() {
        SharedPreferences sharedPref = context
                .getSharedPreferences("main", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.clear();
        editor.commit();
        finish();
    }

    private void deleteAccount() {
        // Post to server
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", Server.getToken(context));
        StringRequest sr = Server.POST(params, Server.deleteAccountURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body == null) ResponseHandler.errorToast(context, "An error occured");
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        ResponseHandler.errorToast(context, "An error occured");
                    }
                }
        );
        Server.makeRequest(context, sr);
    }
}
