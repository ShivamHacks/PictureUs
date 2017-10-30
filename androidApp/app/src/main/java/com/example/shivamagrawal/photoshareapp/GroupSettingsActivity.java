package com.example.shivamagrawal.photoshareapp;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;

import android.content.Intent;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shivamagrawal.photoshareapp.Objects.Contact;
import com.example.shivamagrawal.photoshareapp.Objects.ContactsHelper;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mehdi.sakout.fancybuttons.FancyButton;

public class GroupSettingsActivity extends AppCompatActivity {

    Toolbar toolbar;
    LinearLayout membersListLayout;
    LayoutInflater inflater;
    FancyButton addMembersButton;
    FancyButton leaveGroupButton;
    FancyButton submitButton;
    EditText groupNameET;
    Context context;

    String groupName;

    List<String> currentMembers = new ArrayList<String>();
    ListView existingMembers;
    ArrayAdapter<String> membersAdapter;

    String groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_settings);
        context = this;

        // Toolbar
        toolbar = (Toolbar) findViewById(R.id.group_settings_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Group Name
        groupNameET = (EditText) findViewById(R.id.group_name_edittext);

        // Get Launch data
        Bundle extras = getIntent().getExtras();
        groupID = extras.getString("groupID");
        groupName = extras.getString("groupName");
        getSupportActionBar().setTitle(groupName);
        groupNameET.setText(groupName);

        // Get group data
        getGroupData();

        // Existing members
        existingMembers = (ListView) findViewById(R.id.editgroup_existing_members_listview);
        membersAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, currentMembers);
        existingMembers.setAdapter(membersAdapter);
        justifyListViewHeightBasedOnChildren(existingMembers);

        // New Members
        membersListLayout = (LinearLayout) findViewById(R.id.groupsettings_add_members_list);
        inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        addMembersButton = (FancyButton) findViewById(R.id.groupsettings_addmembers_button);
        addMembersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (membersListLayout.getChildCount() == 0) {
                    membersListLayout.addView(ContactsHelper.createACTV(context, inflater, membersListLayout));
                } else {
                    AutoCompleteTextView lastACTV = (AutoCompleteTextView) membersListLayout
                            .getChildAt(membersListLayout.getChildCount() - 1);
                    if (TextUtils.isEmpty(lastACTV.getText().toString().trim())) { lastACTV.requestFocus(); }
                    else {
                        AutoCompleteTextView newACTV = ContactsHelper.createACTV(context, inflater, membersListLayout);
                        newACTV.requestFocus();
                        membersListLayout.addView(newACTV);
                    }
                }
            }
        });

        leaveGroupButton = (FancyButton) findViewById(R.id.editgroup_leave_button);
        leaveGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(context).setTitle("Leave Group?")
                        .setPositiveButton(android.R.string.yes,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leaveGroup();
                            }
                        }).setNegativeButton(android.R.string.no, null).create().show();
            }
        });

        // Finished Button
        submitButton = (FancyButton) findViewById(R.id.editgroup_submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submit();
            }
        });

    }

    private void getGroupData() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", Server.getToken(context));
        params.put("groupID", groupID);
        StringRequest sr = Server.GET(params, Server.getGroupInfoURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body != null) fillExistingMembersList(body);
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

    private void fillExistingMembersList(JSONObject body) {
        try {
            JSONArray membersJSON = body.getJSONArray("members");
            List<Contact> contacts = ContactsHelper.get(context);
            mainloop: for (int i = 0; i < membersJSON.length(); i++) {
                for (Contact c: contacts) {
                    if (membersJSON.get(i).equals(c.getNumber())
                            || membersJSON.get(i).equals(ContactsHelper
                            .internationalize(context, c.getNumber()))) {
                        currentMembers.add(c.getName());
                        continue mainloop;
                    }
                }
                currentMembers.add(membersJSON.getString(i));
            }
            membersAdapter.notifyDataSetChanged();
            justifyListViewHeightBasedOnChildren(existingMembers);
        } catch(JSONException e) {
            e.printStackTrace();
        }
    }

    private void justifyListViewHeightBasedOnChildren (ListView listView) {
        // http://stackoverflow.com/questions/12212890/disable-listview-scrolling
        ListAdapter adapter = listView.getAdapter();
        if (adapter == null) return;

        ViewGroup vg = listView;
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, vg);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams par = listView.getLayoutParams();
        par.height = totalHeight + (listView.getDividerHeight() * (adapter.getCount() - 1));
        listView.setLayoutParams(par);
        listView.requestLayout();
    }

    private void submit() {
        // Define params
        Map<String, String> params = new HashMap<String, String>();
        params.put("groupID", groupID);

        // Put params if valid
        if (!(groupName.equals(groupNameET.getText().toString())) &&
                !(TextUtils.isEmpty(groupNameET.getText().toString())))
            params.put("newName", groupNameET.getText().toString());

        if (membersListLayout.getChildCount() > 0) {

            List<String> phoneNumbers = new ArrayList<String>();
            for (int i = 0; i < membersListLayout.getChildCount(); i++) {

                EditText memberET = (EditText) membersListLayout.getChildAt(i);
                String unFormatted = memberET.getText().toString().replaceAll("<.*?>", ""); // remove name
                if (!TextUtils.isEmpty(unFormatted)) {
                    String member = PhoneNumberUtils.normalizeNumber(unFormatted);
                    if (member.indexOf("+") == -1)
                        member = ContactsHelper.internationalize(context, member);

                    if (!phoneNumbers.contains(member)
                            && !currentMembers.contains(member)
                            && !TextUtils.isEmpty(member))
                        phoneNumbers.add(member);
                }

            }

            for (int j = 0; j < phoneNumbers.size(); j++)
                params.put("newMembers[" + j + "]", phoneNumbers.get(j));

        }

        // Post to server
        params.put("token", Server.getToken(context));
        StringRequest sr = Server.POST(params, Server.editGroupURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
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

    private void leaveGroup() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("groupID", groupID);
        params.put("token", Server.getToken(context));
        StringRequest sr = Server.POST(params, Server.leaveGroupURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
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
