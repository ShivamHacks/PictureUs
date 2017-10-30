package com.example.shivamagrawal.photoshareapp;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.text.TextUtils;
import android.util.Log;
import android.content.Context;

import java.util.Map;
import java.util.HashMap;

import com.android.volley.toolbox.StringRequest;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.shivamagrawal.photoshareapp.Objects.CountriesAdapter;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONException;
import org.json.JSONObject;

import mehdi.sakout.fancybuttons.FancyButton;

public class SignUpActivity extends AppCompatActivity {

    EditText phoneNumber;
    AutoCompleteTextView countryCode;
    EditText password1;
    EditText password2;
    EditText verificationCode;
    FancyButton signUpSubmit;
    FancyButton signUpVerify;
    Context context;
    String userID;
    String internationalNumber;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        context = this;

        sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        phoneNumber = (EditText) findViewById(R.id.signup_phone_number);

        countryCode = (AutoCompleteTextView) findViewById(R.id.signup_countryACTV);
        countryCode.setAdapter(new CountriesAdapter(context));

        password1 = (EditText) findViewById(R.id.signup_password_1);
        password2 = (EditText) findViewById(R.id.signup_password_2);
        verificationCode = (EditText) findViewById(R.id.signup_verification_code);

        signUpSubmit = (FancyButton) findViewById(R.id.signup_submit);
        signUpSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (check()) {
                    submit();
                }
            }
        });

        signUpVerify = (FancyButton) findViewById(R.id.signup_verify);
        signUpVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                verify();
            }
        });
    }

    private boolean check() {
        boolean allGood = true;
        // check if passwords match
        if (!(password1.getText().toString().equals(password2.getText().toString()))) {
            allGood = false;
            showErrorDialog("Passwords do not match");
        }
        // check if any fields are empty
        if (TextUtils.isEmpty(phoneNumber.getText().toString().trim())
                || TextUtils.isEmpty(countryCode.getText().toString().trim())
                || TextUtils.isEmpty(password1.getText().toString().trim())
                || TextUtils.isEmpty(password2.getText().toString().trim())) {
            showErrorDialog("Required fields are empty");
            allGood = false;
        }
        return allGood;
    }

    private void submit() {
        Map<String, String> params = new HashMap<String, String>();

        internationalNumber = countryCode.getText().toString() +
                PhoneNumberUtils.normalizeNumber(phoneNumber.getText().toString());

        Log.d("NUM", internationalNumber);

        params.put("phoneNumber", internationalNumber);
        params.put("password", password1.getText().toString());

        StringRequest sr = Server.POST(params, Server.signupURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d("RES", res);
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body != null) {
                            try {
                                userID = body.getString("userID");
                            } catch (JSONException e) {
                                ResponseHandler.errorToast(context, "An error occured");
                                e.printStackTrace();
                            }
                        } else {
                            ResponseHandler.errorToast(context, "An error occured");
                        }
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

    private void verify() {
        if (TextUtils.isEmpty(verificationCode.getText().toString().trim()) || userID.equals("")) {
            ResponseHandler.errorToast(context, "An error occurred");
        } else {

            Map<String, String> params = new HashMap<String, String>();
            params.put("phoneNumber", internationalNumber);
            params.put("verificationCode", verificationCode.getText().toString());
            params.put("userID", userID);

            StringRequest sr = Server.POST(params, Server.verifyURL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String res) {
                            JSONObject body = new ResponseHandler(context, res).parseRes();
                            if (body != null) {
                                try {
                                    saveToken(body.getString("token"));
                                } catch (JSONException e) {
                                    ResponseHandler.errorToast(context, "An error occured");
                                    e.printStackTrace();
                                }
                            } else {
                                ResponseHandler.errorToast(context, "An error occured");
                            }
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

    private void saveToken(String token) {
        editor.putString("token", token);
        editor.putBoolean("loggedIn", true);
        editor.commit();

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
