package com.example.shivamagrawal.photoshareapp.Objects;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.example.shivamagrawal.photoshareapp.R;

public class CountriesAdapter extends ArrayAdapter<String> {

    List<String> countries = new ArrayList<String>();

    public CountriesAdapter(Context context) {
        super(context, android.R.layout.simple_list_item_1, new ArrayList<String>());
        Collections.addAll(countries, context.getResources().getStringArray(R.array.countries));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(android.R.layout.simple_list_item_1, null);
            holder = new ViewHolder();
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String country = getItem(position);
        if (country != null) {
            holder.countryTV = (TextView) convertView.findViewById(android.R.id.text1);
            holder.countryTV.setText(country);
        }

        return convertView;
    }

    private static class ViewHolder {
        TextView countryTV;
    }

    @Override
    public Filter getFilter() {
        return CustomFilter;
    }

    private Filter CustomFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object ResultValue) {
            return ResultValue.toString().substring(
                    ResultValue.toString().indexOf("(") + 1 ,
                    ResultValue.toString().indexOf(")"));
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            FilterResults results = new FilterResults();

            if (constraint == null || constraint.length() == 0) {
                results.values = countries;
                results.count = countries.size();
            } else {
                ArrayList<String> NewValues = new ArrayList<String>();

                for (String country : countries) {
                    if (country.toLowerCase().startsWith(constraint.toString()))
                        NewValues.add(country);
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
                addAll((ArrayList<String>) results.values);
            notifyDataSetChanged();
        }
    };
}
