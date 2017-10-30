package com.example.shivamagrawal.photoshareapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.SharedPreferences;
import android.content.Context;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.example.shivamagrawal.photoshareapp.Objects.Group;
import com.example.shivamagrawal.photoshareapp.Objects.GroupAdapter;
import com.example.shivamagrawal.photoshareapp.Objects.ResponseHandler;
import com.example.shivamagrawal.photoshareapp.Objects.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView noGroupsTV;
    ListView groupsList;
    GroupAdapter groupAdapter;
    ArrayList<Group> groups = new ArrayList<Group>();
    Context context;

    SharedPreferences sharedPref;
    SharedPreferences.Editor editor;

    BroadcastReceiver updateGroups = null;
    boolean recieverRegistered = false;
    public static String updateGroupsFilterString = "com.shivamagrawal.pictureus";
    IntentFilter updateGroupsFilter =
            new IntentFilter(updateGroupsFilterString);

    private boolean onCreateRunned = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;

        sharedPref = this.getSharedPreferences("main", Context.MODE_PRIVATE);
        editor = sharedPref.edit();

        updateGroups = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("UPDATE GROUPS", "RECIEVED");
                if (intent != null) getGroupsFromServer();
            }
        };

        toolbar = (Toolbar) findViewById(R.id.main_activity_tool_bar);
        setSupportActionBar(toolbar);

        groupsList = (ListView) findViewById(R.id.list_groups);
        groupAdapter = new GroupAdapter(this, groups);
        groupsList.setAdapter(groupAdapter);

        noGroupsTV = (TextView) findViewById(R.id.nogroup_textview);

    }

    private void getGroupsFromServer() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("token", Server.getToken(context));
        StringRequest sr = Server.GET(params, Server.getAllGroupsURL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        JSONObject body = new ResponseHandler(context, res).parseRes();
                        if (body != null) changeGroupList(body);
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

    private void changeGroupList(JSONObject body) {
        try {
            groups.clear();
            JSONArray groupsJSON = body.getJSONArray("groups");
            if (groupsJSON.length() == 0) {
                noGroupsTV.setText("No Groups. Create one by clicking the plus button");
            } else {
                noGroupsTV.setVisibility(View.GONE);
                for (int i = 0; i < groupsJSON.length(); i++) {
                    JSONObject group = new JSONObject(groupsJSON.get(i).toString());
                    groups.add(new Group(group.getString("groupID"), group.getString("groupName")));
                }
                saveGroupsLocally();
            }
            groupAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            ResponseHandler.errorToast(context, "An error occured");
        }
    }

    private void saveGroupsLocally() {
        editor.remove("groups");
        editor.commit();
        Set<String> stringGroups = new HashSet<String>();
        for (Group g: groups)
            stringGroups.add(g.toString());
        editor.putStringSet("groups", stringGroups);
        editor.commit();
    }

    private void getSavedGroups() {
        groups.clear();
        Set<String> stringGroups = sharedPref.getStringSet("groups", null);
        if (stringGroups != null && stringGroups.size() != 0) {
            noGroupsTV.setVisibility(View.GONE);
            Iterator<String> iterator = stringGroups.iterator();
            while(iterator.hasNext())
                groups.add(Group.unString(iterator.next()));
            groupAdapter.notifyDataSetChanged();
        } else {
            noGroupsTV.setText("No Groups. Create one by clicking the plus button");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!recieverRegistered) {
            registerReceiver(updateGroups, updateGroupsFilter);
            recieverRegistered = true;
        }

        boolean loggedIn = sharedPref.getBoolean("loggedIn", false);
        if (loggedIn) {
            if (onCreateRunned) {
                sendBroadcast(new Intent(updateGroupsFilterString));
            } else {
                getSavedGroups();
            }
        } else {
            Intent loginOrSignUp = new Intent(this, LoginOrSignUpActivity.class);
            startActivity(loginOrSignUp);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        if (recieverRegistered) {
            unregisterReceiver(updateGroups);
            recieverRegistered = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_main_refresh:
                sendBroadcast(new Intent(updateGroupsFilterString));
                return true;
            case R.id.action_main_add:
                Intent addGroup = new Intent(this, AddGroupActivity.class);
                startActivity(addGroup);
                return true;
            case R.id.action_main_settings:
                Intent accountSettings = new Intent(this, AccountSettingsActivity.class);
                startActivity(accountSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
