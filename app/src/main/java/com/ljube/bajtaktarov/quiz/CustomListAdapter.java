package com.ljube.bajtaktarov.quiz;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final String[] itemname;
    private final String[] imgid;

    public CustomListAdapter(Activity context, String[] itemname, String[] imgid) {
        super(context, R.layout.custom_listview, itemname);
        this.context = context;
        this.itemname = itemname;
        this.imgid = imgid;
    }

    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.custom_listview, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        txtTitle.setText(itemname[position]);
        Picasso.with(getContext()).load(String.valueOf(imgid[position])).into(imageView);
        return rowView;
    }
}
