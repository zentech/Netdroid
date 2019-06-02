package org.pctechtips.netdroid;

/*
* {@HostAdapter} clas is an ArrayAdapter for MainActivity
* */

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by george on 5/4/17.
 */

public class HostAdapter extends ArrayAdapter<String> {

    String[] localHostInfo;

    public HostAdapter(Context context, int num, ArrayList<String> hostInfo) {
        super(context, 0, hostInfo);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String item = getItem(position);
        item = item.trim();
        //splitting the string using separator "space" " " for private interface
        //Log.v("ITEM ", item);
        if(item.contains(":")) {
            localHostInfo = item.split("\\s");
        }
        else {
            //when splitting by more than one space use \\s+ for public iface
            localHostInfo = item.split("\\s+");
        }

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_main, parent, false);
        }
        // Lookup view for data population
        TextView hostLblTextView = convertView.findViewById(R.id.host_label);
        TextView hostDataTextView = convertView.findViewById(R.id.host_data);
        // Populate the data into the template view using the data object
        hostLblTextView.setText(localHostInfo[0]);
        hostDataTextView.setText(localHostInfo[1]);
        // Return the completed view to render on screen
        return convertView;
    }
}
