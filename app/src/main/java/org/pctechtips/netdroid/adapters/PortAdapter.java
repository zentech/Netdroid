package org.pctechtips.netdroid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by george on 5/2/2017.
 * @{PortAdapter} Custome ArrayAdapter for port scan activity
 */

public class PortAdapter extends ArrayAdapter<String> {
    public PortAdapter(Context context, int num, ArrayList<String> openPorts) {
        super(context, 0, openPorts);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
         String item = getItem(position);
        //splitting string into port and service eg: 53>Domain Name Service
        String[] portData = item.split(">");
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_port, parent, false);
        }
        // Lookup view for data population
        TextView portNumTxtView = convertView.findViewById(R.id.port_number);
        TextView portStateTxtView = convertView.findViewById(R.id.open_state);
        TextView serviceTxtView = convertView.findViewById(R.id.service_name);

        // Populate the data into the template view using the data object
        portNumTxtView.setText(portData[0]);
        portStateTxtView.setText("Open!");
        serviceTxtView.setText(portData[1]);
        // Return the completed view to render on screen
        return convertView;
    }
}
