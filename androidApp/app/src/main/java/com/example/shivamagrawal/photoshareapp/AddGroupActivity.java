package com.example.shivamagrawal.photoshareapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.view.LayoutInflater;
import android.content.Context;

import android.telephony.PhoneNumberUtils;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shivamagrawal.photoshareapp.Objects.ContactsHelper;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.util.Log;

import mehdi.sakout.fancybuttons.FancyButton;

public class AddGroupActivity extends AppCompatActivity {

    Toolbar toolbar;
    LinearLayout membersListLayout;
    LayoutInflater inflater;
    FancyButton submitButton;
    EditText groupName;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_group);

        context = this;

        toolbar = (Toolbar) findViewById(R.id.add_group_activity_tool_bar);
        toolbar.setTitle("Add Group");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        membersListLayout = (LinearLayout) findViewById(R.id.members_list);
        inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        membersListLayout.addView(ContactsHelper.createACTV(context, inflater, membersListLayout)); // create First ACTV

        submitButton = (FancyButton) findViewById(R.id.submit_new_group_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

        groupName = (EditText) findViewById(R.id.editText_group_name);

    }

    private void submit() {

        // Define params
        Map<String, String> params = new HashMap<String, String>();

        // TODO: deal with iso

        // Put params if valid
        if (!(TextUtils.isEmpty(groupName.getText().toString())))
            params.put("groupName", groupName.getText().toString());

        if (membersListLayout.getChildCount() > 0) {

            List<String> phoneNumbers = new ArrayList<String>();
            for (int i = 0; i < membersListLayout.getChildCount(); i++) {

                EditText memberET = (EditText) membersListLayout.getChildAt(i);
                String unFormatted = memberET.getText().toString().replaceAll("<.*?>", ""); // remove name
                if (!TextUtils.isEmpty(unFormatted)) {
                    String member = PhoneNumberUtils.normalizeNumber(unFormatted);
                    if (member.indexOf("+") == -1)
                        member = ContactsHelper.internationalize(context, member);
                    if (!phoneNumbers.contains(member) && !TextUtils.isEmpty(member))
                        phoneNumbers.add(member);
                }

            }

            for (int j = 0; j < phoneNumbers.size(); j++)
                params.put("members[" + j + "]", phoneNumbers.get(j));

        }

        // Post to server
        params.put("token", Server.getToken(context));
        StringRequest sr = Server.POST(params, Server.createGroupURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d("RES", res);
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body != null) {
                            sendBroadcast(new Intent(MainActivity.updateGroupsFilterString));
                            finish();
                        }
                        else ResponseHandler.errorToast(context, "An error occured");
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
