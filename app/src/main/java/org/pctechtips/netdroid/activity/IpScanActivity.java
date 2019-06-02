package org.pctechtips.netdroid.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.pctechtips.netdroid.Node;
import org.pctechtips.netdroid.adapters.NodeAdapter;
import org.pctechtips.netdroid.R;
import org.pctechtips.netdroid.runnable.IpScanRunnable;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import jcifs.netbios.NbtAddress;


/**
 * Created by jlvaz on 3/1/2017.
 */

public class IpScanActivity extends AppCompatActivity {
    static String ip;
    ArrayList<Node> hostList;
    ListView scanList;
    String mac = "";
    NodeAdapter networkAdapter;
    private TextView statusMsg;
    private TextView ipLabelTxtView;
    private TextView networkTxtView;
    private TextView numOfHostTxtView;
    private TextView hostnameTxtView;
    private TaskScanNetwork scanNetwork;
    private ProgressBar scanProgress;
    Toolbar myToolbar;
    private PopupWindow popUpWindow;
    private LinearLayout linearListIp;
    static private int numOfHost;
    static private int cidr;
    private AdView mAdView;
    private AdRequest adRequest;
    private static final String ARP_TABLE = "/proc/net/arp";
    private static final String ARP_FLAG = "0x2";
    private static final int NETBIOS_SERVER = 0x20;
    private String hostName = "";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_list);

        /* initilize adMob app-id*/
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6077312877853191~2989221860");
        mAdView = findViewById(R.id.adView_ip_scan);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //setting the ActionBar for the activity
        myToolbar = findViewById(R.id.my_toolbar);
        myToolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(myToolbar);
        //adding back arrow to toolbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //getting ip addres from mainActivity
        Intent ipScanIntent = getIntent();
        ip = ipScanIntent.getStringExtra("ip");
        mac = ipScanIntent.getStringExtra("mac");
        cidr = ipScanIntent.getIntExtra("cidr", 0);
        hostName = ipScanIntent.getStringExtra("hostname");

        Log.v("SCAN", "scan avtivity");

        hostList = new ArrayList<>();
        scanProgress = findViewById(R.id.progress_bar);
        /* labels for ip scan results IPs and MACs*/
        ipLabelTxtView = findViewById(R.id.ip_open_label);
        ipLabelTxtView.setText("Hostname / IP Address");

        networkTxtView = findViewById(R.id.scan_output_1);
        numOfHostTxtView = findViewById(R.id.scan_output_2);

        hostnameTxtView = findViewById(R.id.host_name);

        //referencing LinearLayout in activity_main.xml for popup window
        linearListIp = findViewById(R.id.scan_list_linear);

        //inflating adapter
        scanList  = findViewById(R.id.scan_list);
        networkAdapter = new NodeAdapter(this, R.layout.list_network, hostList);
        scanList.setAdapter(networkAdapter);

        getNumOfHost();

        //scanning network
        scanNetwork = new TaskScanNetwork();
        scanNetwork.execute();

        //setting the listeners for individuals ip address to start port scan
        scanList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> paren, View view, int position, long id) {
                //cancel AsyncTask if item in list is clicked
                scanNetwork.cancel(true);
                //start PortScanActivity, passing ipAddress position, and passing extras,
                Node host = hostList.get(position);
                Intent portScanIntent = new Intent(IpScanActivity.this, PortScanActivity.class);
                portScanIntent.putExtra("host", host.getIp());
                startActivity(portScanIntent);
            }
        });
    }


    /*
    * calculating the max number of host in network
    * based on subnet cidr. first get how many bits
    * are left for host portion then (2 power of hostBits)
    */
    public void getNumOfHost() {
        final int MAX_ADDR_BITS = 32;
        double hostBits = MAX_ADDR_BITS - cidr;
        numOfHost = (int) (Math.pow(2.0, hostBits) - 2);
    }


    /*
    * AscynTask to scan the network
    * you should try different timeout for your network/devices
    * it will try to detect localhost ip addres and subnet. then
    * it will use subnet to scan network
    */
    private class TaskScanNetwork extends AsyncTask<Void, Void, Void> {
        static final int NUMTHREADS = 254;
        static final int NETBIOS_SERVER = 0x2;
        String subnet = ip.substring(0, ip.lastIndexOf("."));
        int startIp = 1;
        int range = (numOfHost / NUMTHREADS); //range to be scanned by every thread
        int stopIp = startIp + range;
        List<Future> resultList = new ArrayList<>();
        ExecutorService executor = Executors.newFixedThreadPool(NUMTHREADS);
        int hosts = 1;
        static final int TIME_OUT = 1000;


        @Override
        protected void onPreExecute() {
            hostList.clear();
            Node node = new Node(ip, mac);
            node.setHostName(hostName);
            hostList.add(node);
            scanProgress.setMax(numOfHost);
            scanProgress.setProgress((numOfHost * 10) / 100 ); //set progress bar at 10%
//            statusMsg.setText("Scanning " + subnet + ".0/" + cidr);
        }

        /* initialaze threads */
        @Override
        protected Void doInBackground(Void... params) {
            for(int i = 0; i < NUMTHREADS; i++) {
                IpScanRunnable ipScan = new IpScanRunnable(subnet, startIp, stopIp);
                Future<Node> result = executor.submit(ipScan);
                resultList.add(result);
                startIp = stopIp;
                stopIp = startIp + range;
            }

            try {
                new Thread().sleep(TIME_OUT);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }

            executor.shutdown();

            publishProgress();
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... param) {
            ExecutorService executorCached = Executors.newCachedThreadPool();
            //update progress bar
            /*scanProgress.setProgress(values[0].progressBar);*/
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(ARP_TABLE), StandardCharsets.UTF_8));
                reader.readLine(); // Skip header.
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.v("ARPFILE", line);
                    String[] arpLine = line.split("\\s+");
                    //if arp line contains flag 0x2 we parse host
                    if(arpLine[2].equals(ARP_FLAG)) {
                        final String ip = arpLine[0];
                        final String flag = arpLine[2];
                        final String mac = arpLine[3];
                        final Node node = new Node(ip, mac);
                        /**
                         * only creating a new thread for host in arp table to
                         * resolve canonical name and/or netbios names
                         */
                        executorCached.execute(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    InetAddress add = InetAddress.getByName(ip);
                                    String hostname = add.getCanonicalHostName();
                                    Log.v("HOSTNAME: ", hostname);
                                    node.setHostName(hostname);
                                } catch (UnknownHostException e) {
                                    return;
                                }

                                try {
                                    NbtAddress[] netbios = NbtAddress.getAllByAddress(ip);
                                    for (NbtAddress addr : netbios) {
                                        if (addr.getNameType() == NETBIOS_SERVER) {
                                            node.setNetBios(addr.getHostName());
                                            Log.v("NETBIOS: ", addr.getHostName());
                                            return;
                                        }
                                    }
                                } catch (UnknownHostException e) {
                                    //some host don't have 139 open so no Netbios resolution
                                }
                            }
                        });

                        hostList.add(node);
                        networkAdapter.notifyDataSetInvalidated();
//                        scanProgress.setProgress(node.progressBar);
                    }
                }
                reader.close();
            }
            catch (Exception ex) {

            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
//            ;
            networkTxtView.setText(subnet + ".0/ " + cidr);
            numOfHostTxtView.setText(hostList.size()+"");
            scanProgress.setProgress(254);
        }
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

}
