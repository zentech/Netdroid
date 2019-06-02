package org.pctechtips.netdroid;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by george on 5/5/17.
 */

public class PingAdapter extends ArrayAdapter<String> {

    String pingIp;
    String packetNum;
    String packetTtl;
    String packetTime;
    String output[];

    public PingAdapter(Context context, int num, ArrayList<String> pingResult) {
        super(context, 0, pingResult);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        //eliminating any speical char eg: [](){}
        String tempOutput = getItem(position).replaceAll("[\\[\\](){}]", "");
        String[] output = tempOutput.split(" "); //spliting output into array

        /*ping packets differ in ouput. If our packet returns an array with more
        * than 8 element then remove everything before 4 element, else remove before
        * 3 element*/
        if(output.length > 8) {
            output = Arrays.copyOfRange(output, 4, output.length);
        }
        else {
            output = Arrays.copyOfRange(output, 3, output.length);
        }

        //extracting parenthesis from ip address
        pingIp = output[0].replaceAll("[():]","");
        packetNum = output[1].substring(output[1].indexOf("=")+1);
        packetTtl = output[2].substring(output[2].indexOf("=")+1);
        packetTime = output[3].substring(output[3].indexOf("=")+1);
        packetTime += output[4];

        //extracting packet number

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_ping, parent, false);
        }
        // Lookup view for data population
        TextView pNumTxtView = convertView.findViewById(R.id.ping_packet_num);
        TextView pAddrTxtView = convertView.findViewById(R.id.ping_address);
        TextView pTtlTxtView = convertView.findViewById(R.id.ping_packet_ttl);
        TextView pTimeTxtView = convertView.findViewById(R.id.ping_packet_time);
        // Populate the data into the template view using the data object
        pNumTxtView.setText(packetNum);
        pAddrTxtView.setText(pingIp);
        pTtlTxtView.setText(packetTtl);
        pTimeTxtView.setText(packetTime);
        // Return the completed view to render on screen
        return convertView;
    }
}
