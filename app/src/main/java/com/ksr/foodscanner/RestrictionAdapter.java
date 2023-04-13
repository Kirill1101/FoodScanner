package com.ksr.foodscanner;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class RestrictionAdapter extends ArrayAdapter<String> {

    private LayoutInflater inflater;
    private int layout;
    private ArrayList<String> restrictionList;

    RestrictionAdapter(Context context, int resource, ArrayList<String> restriction) {
        super(context, resource, restriction);
        this.restrictionList = restriction;
        this.layout = resource;
        this.inflater = LayoutInflater.from(context);
    }

    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder viewHolder;
        if(convertView==null){
            convertView = inflater.inflate(this.layout, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        else{
            viewHolder = (ViewHolder) convertView.getTag();
        }
        final String restriction = restrictionList.get(position);

        viewHolder.restrictionView.setText(restriction);

        viewHolder.removeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                restrictionList.remove(restriction);
                notifyDataSetChanged();
            }
        });

        return convertView;
    }

    private class ViewHolder {
        final Button removeButton;
        final TextView restrictionView;
        ViewHolder(View view){
            removeButton = view.findViewById(R.id.removeRestrictionButton);
            restrictionView = view.findViewById(R.id.restrictionView);
        }
    }
}
