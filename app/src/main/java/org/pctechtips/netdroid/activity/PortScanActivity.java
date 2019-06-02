package org.pctechtips.netdroid.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.pctechtips.netdroid.Node;
import org.pctechtips.netdroid.PortAdapter;
import org.pctechtips.netdroid.R;
import org.pctechtips.netdroid.dbhelper.DatabaseHelper;
import org.pctechtips.netdroid.runnable.PortScanRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by jlvaz on 3/7/2017.
 */

public class PortScanActivity extends AppCompatActivity {

    String ipAddress;
    ArrayList<String> openPorts;
    private PortAdapter portAdapter;
    Toolbar myToolbar;
    TextView statusMsg;
    ProgressBar progressBar;
    private PopupWindow popUpWindow;
    private LinearLayout linearListPort;
    private ImageView scanListImg;
    private TextView ipLableTxtView;
    private TextView portsLableTxtView;
    private TextView ipOutputTxtView;
    private TextView portOutputTxtView;
    private TextView portNumTxtView;
    private TextView portOpenTxtView;
    private TextView scanLabelTxtView;
    private DatabaseHelper portsDB;
    private AdView mAdView;
    private AdRequest adRequest;
    private  ListView portList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.pctechtips.netdroid.R.layout.scan_list);

        /* initilize ads app-id*/
        /*MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");*/
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6077312877853191~2989221860");
        mAdView = findViewById(R.id.adView_ip_scan);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        /*reference and inflate custom toolbar*/
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        //adding back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        Intent portIntent = getIntent();
        ipAddress = portIntent.getStringExtra("host");

        /* getting an instance of database */
        portsDB = DatabaseHelper.getDatabaseInstance(this);

        /*
        * reference views for scan_list.xml
        */
        progressBar = findViewById(R.id.progress_bar);
        scanListImg = findViewById(R.id.scan_list_icon);
        scanListImg.setImageResource(R.drawable.ic_search_white_36dp);

        /* labels for port scan results (port / type)*/
        ipLableTxtView = findViewById(R.id.scan_label_1);
        portsLableTxtView = findViewById(R.id.scan_label_2);
        ipOutputTxtView = findViewById(R.id.scan_output_1);
        portOutputTxtView = findViewById(R.id.scan_output_2);
        portNumTxtView = findViewById(R.id.host_port_label);
        portOpenTxtView = findViewById(R.id.ip_open_label);
        scanLabelTxtView = findViewById(R.id.scan_label);
        portNumTxtView.setText("Port");
        portOpenTxtView.setText("State / Service");
        scanLabelTxtView.setText(""); //hiding this label for port scanning

        ipLableTxtView.setText("IP: ");
        portsLableTxtView.setText("ports: ");


        //referencing LinearLayout in activity_main.xml for popup window
        linearListPort = findViewById(R.id.scan_list_linear);

        //setting the adapter
        portList = findViewById(R.id.scan_list);
        openPorts = new ArrayList<>();
        portAdapter = new PortAdapter(this, R.layout.list_port, openPorts);
        portList.setAdapter(portAdapter);

        //scanning ports
        PortScanTask portScan = new PortScanTask();
        portScan.execute();

        progressBar.setClickable(false);
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

    private class PortScanTask extends AsyncTask<Void, String, Void> {
        final int NUM_OF_PORTS = 1024; //well known ports
        final int NUM_OF_THREADS = (NUM_OF_PORTS / 4); //256 threads
        List<Future> portResult = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUM_OF_THREADS);
        int startPort = 0;
        int endPort = (NUM_OF_PORTS / NUM_OF_THREADS); //end port for every thread - 4
        int progress = 0;


        @Override
        protected void onPreExecute() {
            openPorts.clear();
//            statusMsg.setText("Scanning ports..");
            progressBar.setMax(NUM_OF_PORTS);
            progressBar.setProgress(100);
        }

        @Override
        protected Void doInBackground(Void... params) {
             /* starging threads and adjusting port range - 4 ports by every thread */
            for(int i = 0; i < NUM_OF_THREADS; i++ ) {
                PortScanRunnable portscanCall = new PortScanRunnable(ipAddress, startPort, endPort, portsDB);
                Future<Node> result = executor.submit(portscanCall);
                portResult.add(result);
                startPort = endPort+1;
                endPort = startPort + (NUM_OF_PORTS / NUM_OF_THREADS); //4 ports
            }

            executor.shutdown();
            try {
                executor.awaitTermination(3, TimeUnit.SECONDS);
                executor.shutdownNow();
            } catch (InterruptedException e) {
                return null;
            }

            for(Future<Node> node: portResult) {
                try {
                    for(String port: node.get().getOpenPorts()) {
                        publishProgress(port);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            //only add to
            if(values[0] != null) {
                openPorts.add(values[0]);
                portAdapter.notifyDataSetInvalidated();
            }

            progressBar.setProgress(progress++);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            ipOutputTxtView.setText(ipAddress);
            portOutputTxtView.setText(openPorts.size()+" open ports");
//            statusMsg.setText(ipAddress + " Ports");
            progressBar.setProgress(NUM_OF_PORTS);
        }
    }
}

