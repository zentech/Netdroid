package org.pctechtips.netdroid.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import org.pctechtips.netdroid.Node;
import org.pctechtips.netdroid.R;

import java.util.ArrayList;


public class NodeAdapter extends ArrayAdapter<Node>{
    String hostname = "";

    public NodeAdapter(Context context, int num, ArrayList<Node> allHost) {
        super(context, 0, allHost);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        org.pctechtips.netdroid.Node host = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(org.pctechtips.netdroid.R.layout.list_network, parent, false);
        }
        // Lookup view for data population
        ImageView nodeHostImgView = convertView.findViewById(R.id.host_icon);
        TextView hostnameTxtView = convertView.findViewById(R.id.host_name);
        TextView nodeIpTxtView = convertView.findViewById(R.id.ip_address);
        TextView nodeMacTxtView = convertView.findViewById(R.id.mac_address);
        ImageView nodeArrowImgView = convertView.findViewById(R.id.port_scan_arrow);
        // Populate the data into the template view using the data object
        nodeHostImgView.setImageResource(R.drawable.ic_computer_white_36dp);
        if(!host.getHostName().equals("")) {
            hostname = host.getHostName();
        }
        else if(!host.getNetBios().equals("")){
            hostname = host.getHostName();
        }
        else {
            hostname = host.getIp();
        }
        Log.v("HOST", hostname);
        hostnameTxtView.setText(hostname);
        nodeIpTxtView.setText(host.getIp());
        nodeMacTxtView.setText(host.getMac());
        nodeArrowImgView.setImageResource(R.drawable.ic_next_button);
        // Return the completed view to render on screen
        return convertView;
    }
}
