package org.pctechtips.netdroid.activity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.pctechtips.netdroid.PingAdapter;
import org.pctechtips.netdroid.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class PingActivity extends AppCompatActivity {

    ArrayList<String> pingResult;
    ListView listViewPing;
    Toolbar myToolbar;
    PingAdapter pingAdapter;
    EditText hostIp;
    private TaskPingHost pingHost;
    Button pingBtn;
    private AdView mAdView;
    private AdRequest adRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ping);

        /* initilize ads app-id*/
        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");*/
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6077312877853191~2989221860");
        mAdView = findViewById(R.id.adView_ping);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //setting the ActionBar for the activity
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        //adding back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        pingResult = new ArrayList<String>();
        hostIp = findViewById(R.id.host_ip); //getting hostname/ip address to ping
        listViewPing = findViewById(R.id.ping_output);

        //inflating the adapter
        pingAdapter = new PingAdapter(this, R.layout.list_ping, pingResult);
        listViewPing.setAdapter(pingAdapter);

        //click event listener for ping button
        pingBtn = findViewById(R.id.ping_btn);
        pingBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //stargin asynctask ping host
                pingHost = new TaskPingHost();
                pingHost.execute();
            }
        });
    }


    /*
   * this method will handle action to back arrow in toolbar.
   * send to previous activity.
   */
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*
    * {@TaskPingHost} class will ping host by ip or hostname
    */
    private class TaskPingHost extends AsyncTask<Void, String, Void> {

        String pingCmd = "/system/bin/ping -c 10 ";
        String host;
        String output[];

        @Override
        protected void onPreExecute() {
            //clear arraylist
            pingResult.clear();
            host = hostIp.getText().toString();
            //putting ping command together
            pingCmd = pingCmd + host;
        }

        @Override
        protected Void doInBackground(Void... params) {
            //calling ping command from android bash
            try {
                Process p = Runtime.getRuntime().exec(pingCmd);
                BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    publishProgress(inputLine);
                }
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //only add to ArrayList<Node> if host is alive on network
            if(values[0] != null && values[0].length() > 0) {
                //parsing the bytes response from ping
                if(values[0].contains("bytes") && !values[0].contains("PING")){ //parsing the packets of ping output
                    //updatingn packets from ping command
                    pingResult.add(values[0]);
                    pingAdapter.notifyDataSetInvalidated();
                }
            }
        }
    }
}
