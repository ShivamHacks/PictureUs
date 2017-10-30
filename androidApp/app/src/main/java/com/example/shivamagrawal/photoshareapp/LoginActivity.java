package com.example.shivamagrawal.photoshareapp;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shivamagrawal.photoshareapp.Objects.CountriesAdapter;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginActivity extends AppCompatActivity {

    EditText phoneNumber;
    AutoCompleteTextView countryCode;
    EditText password;
    FancyButton loginSubmit;
    Context context;
    String internationalNumber;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        context = this;

        sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        phoneNumber = (EditText) findViewById(R.id.login_phone_number);

        countryCode = (AutoCompleteTextView) findViewById(R.id.login_countryACTV);
        countryCode.setAdapter(new CountriesAdapter(context));

        password = (EditText) findViewById(R.id.login_password);

        loginSubmit = (FancyButton) findViewById(R.id.login_submit);
        loginSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check()) {
                    submit();
                }
            }
        });

    }

    private boolean check() {
        boolean allGood = true;
        // check if any fields are empty
        if (TextUtils.isEmpty(phoneNumber.getText().toString().trim())
                || TextUtils.isEmpty(countryCode.getText().toString().trim())
                || TextUtils.isEmpty(password.getText().toString().trim())) {
            showErrorDialog("Required fields are empty");
            allGood = false;
        }
        return allGood;
    }

    private void submit() {
        Map<String, String> params = new HashMap<String, String>();

        internationalNumber = countryCode.getText().toString() +
                PhoneNumberUtils.normalizeNumber(phoneNumber.getText().toString());

        params.put("phoneNumber", internationalNumber);
        params.put("password", password.getText().toString());

        StringRequest sr = Server.POST(params, Server.loginURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body != null) {
                            try {
                                Log.d("TOKEN", body.getString("token"));
                                saveToken(body.getString("token"));
                            } catch (JSONException e) {
                                ResponseHandler.errorToast(context, "An error occured");
                                e.printStackTrace();
                            }
                        } else { ResponseHandler.errorToast(context, "An error occured"); }
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

        editor.putString("countryCode", countryCode.getText().toString());
        editor.commit();
    }

    private void saveToken(String token) {
        editor.putString("token", token);
        editor.putBoolean("loggedIn", true);
        editor.commit();

        sendBroadcast(new Intent(
                MainActivity.updateGroupsFilterString));

        finish();
    }

    private void showErrorDialog(String message) {
        new AlertDialog.Builder(context).setTitle(message)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                }).create().show();
    }
}
