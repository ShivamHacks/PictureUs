package com.example.shivamagrawal.photoshareapp.Objects;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.shivamagrawal.photoshareapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.NumberParseException;

public class ContactsHelper {

    private static PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

    public static List<Contact> get(Context context) {
        // http://techblogon.com/read-multiple-phone-numbers-from-android-contacts-list-programmatically/
        List<Contact> contacts = new ArrayList<Contact>();
        Cursor cursor = context
                .getContentResolver()
                .query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        Integer contactsCount = cursor.getCount();
        if (contactsCount > 0) {
            while (cursor.moveToNext()) {
                String id = cursor
                        .getString(cursor.getColumnIndex(
                                ContactsContract.Contacts._ID));
                String contactName = cursor
                        .getString(cursor.getColumnIndex(
                                ContactsContract.Contacts.DISPLAY_NAME));
                if (Integer.parseInt(cursor
                        .getString(cursor.getColumnIndex(
                                ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCursor = context
                            .getContentResolver()
                            .query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id}, null);
                    while (pCursor.moveToNext()) {
                        String phoneNo = pCursor
                                .getString(pCursor.getColumnIndex(
                                        ContactsContract.CommonDataKinds.Phone.NUMBER));
                        contacts.add(new Contact(contactName, phoneNo));
                    }
                    pCursor.close();
                }
            }
            cursor.close();
        }
        return contacts;
    }

    public static AutoCompleteTextView createACTV(
            final Context context, final LayoutInflater inflater,
            final LinearLayout membersListLayout) {
        AutoCompleteTextView memberET = (AutoCompleteTextView)
                inflater.inflate(R.layout.add_member_edittext, null);
        memberET.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_NEXT) {
                    AutoCompleteTextView lastACTV =
                            (AutoCompleteTextView) membersListLayout
                            .getChildAt(membersListLayout.getChildCount() - 1);
                    if (TextUtils.isEmpty(lastACTV.getText().toString().trim()))
                        lastACTV.requestFocus();
                    else {
                        AutoCompleteTextView newACTV =
                                createACTV(context, inflater, membersListLayout);
                        newACTV.requestFocus();
                        membersListLayout.addView(newACTV);
                    }
                    handled = true;
                }
                return handled;
            }
        });
        ContactsAdapter adapter = new ContactsAdapter(context);
        memberET.setAdapter(adapter);
        return memberET;
    }

    public static String internationalize(Context context, String number) {
        SharedPreferences sharedPref =
                context.getSharedPreferences("main", Context.MODE_PRIVATE);
        return sharedPref.getString("countryCode", "") + number;
    }

}
