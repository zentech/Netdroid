package org.pctechtips.netdroid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by George on 5/30/2017.
 */

public class NetstatAdapter extends ArrayAdapter<String> {

    public NetstatAdapter(Context context, int num, ArrayList<String> openPorts) {
        super(context, 0, openPorts);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        String item = getItem(position);

        /*splitting item by more than one space*/
        String netstatLine[] = item.split(" +");

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_stat, parent, false);
        }
        // Lookup view for data population
        TextView protocolTxtView = convertView.findViewById(R.id.protocol);
        TextView addressTxtView = convertView.findViewById(R.id.address);
        TextView stateTxtView = convertView.findViewById(R.id.state);

        // Populate the data into the template view using the data object
        protocolTxtView.setText(netstatLine[0]);
        /* if state is listening then get local address and port
        * instead of remote address*/
        if (netstatLine[5].equals("LISTEN")) {
            addressTxtView.setText(netstatLine[3]);
        } else {
            addressTxtView.setText(netstatLine[4]);
        }
        stateTxtView.setText(netstatLine[5]);
        // Return the completed view to render on screen
        return convertView;
    }
}
