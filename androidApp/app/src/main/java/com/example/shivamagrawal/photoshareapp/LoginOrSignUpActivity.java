package com.example.shivamagrawal.photoshareapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.view.View;
import android.content.Intent;
import android.content.Context;

import mehdi.sakout.fancybuttons.FancyButton;

public class LoginOrSignUpActivity extends AppCompatActivity {

    FancyButton login;
    FancyButton signup;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_or_sign_up);
        context = this.getApplicationContext();

        login = (FancyButton) findViewById(R.id.login);
        signup = (FancyButton) findViewById(R.id.signup);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent logIn = new Intent(context, LoginActivity.class);
                finish();
                startActivity(logIn);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signUp = new Intent(context, SignUpActivity.class);
                finish();
                startActivity(signUp);
            }
        });
    }

}
