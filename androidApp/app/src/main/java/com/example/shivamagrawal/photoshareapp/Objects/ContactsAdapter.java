package com.example.shivamagrawal.photoshareapp.Objects;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.content.Context;
import android.widget.Filter;

import java.util.List;
import java.util.ArrayList;

public class ContactsAdapter extends ArrayAdapter<Contact> {

    List<Contact> members;

    public ContactsAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_2, new ArrayList<Contact>());
        members = ContactsHelper.get(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_2, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Contact contact = getItem(position);
        if (contact != null) {
            holder.contactName = (TextView) convertView.findViewById(android.R.id.text1);
            holder.contactNumber = (TextView) convertView.findViewById(android.R.id.text2);
            holder.contactName.setText(contact.getName());
            holder.contactNumber.setText(contact.getNumber());
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView contactName;
        TextView contactNumber;
    }

    @Override
    public Filter getFilter() {
        return CustomFilter;
    }

    private Filter CustomFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object ResultValue) {
            Contact contact = (Contact) ResultValue;
            return contact.getNumber() + " <" + contact.getName() + ">";
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            List<Contact> allMembers = members;

            if (constraint == null || constraint.length() == 0) {
                results.values = allMembers;
                results.count = allMembers.size();
            } else {
                ArrayList<Contact> NewValues = new ArrayList<Contact>();

                for (Contact contact : allMembers) {
                    if (contact.getNumber().indexOf(constraint.toString()) != -1)
                        NewValues.add(contact);
                }
                results.values = NewValues;
                results.count = NewValues.size();
            }

            return results;
        }

        @Override
        protected void publishResults(CharSequence Constraint, FilterResults results) {
            clear();
            if (results.count > 0)
                addAll((ArrayList<Contact>) results.values);
            notifyDataSetChanged();
        }
    };

}
