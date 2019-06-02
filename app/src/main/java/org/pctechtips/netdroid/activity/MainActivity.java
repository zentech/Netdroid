package org.pctechtips.netdroid.activity;

/*
* IpSCanner and PortScanner App for Android
*/

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import org.pctechtips.netdroid.BuildConfig;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;

import org.apache.commons.net.util.SubnetUtils;
import org.pctechtips.netdroid.HostAdapter;
import org.pctechtips.netdroid.R;
import org.pctechtips.netdroid.classes.DownloadTask;
import org.pctechtips.netdroid.dbhelper.DatabaseHelper;

import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;


public class MainActivity extends AppCompatActivity {

    TextView txtWifiName;
    ArrayList<String> localIfaceInfo; //holds all local interface config
    ArrayList<String> allInterfaceAdd; //all interface 
    public String ipv4 = "";
    public String ipv6 = "";
    Toolbar myToolbar;
    private LinearLayout linearMain;
    /*private LinearLayout linearWifiName;*/
    WifiManager wifiMan;
    WifiInfo wifiInfo;
    private ImageView ImgWifiIcon;
    private ProgressBar progressBar;
    private DhcpInfo dhcpInfo;
    private int cidr;
    private String mac;
    private AdView mAdView;
    private AdRequest adRequest;
    private ListView localNetListView;
    private HostAdapter localNetAdapter;
    private ImageView scanButton;
    private ConnectivityManager connMgr;
    private NetworkInfo netInfo;
    private DownloadTask dlPortTask;
    private DatabaseHelper dbHelper;
    private TaskPublicNet taskPublicNet;
    private String hostName = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* initilize ads app-id*/
        MobileAds.initialize(getApplicationContext(), "ca-app-pub-3940256099942544~3347511713");
//        MobileAds.initialize(getApplicationContext(), "ca-app-pub-6077312877853191~2989221860");
        /*mAdView = (AdView) findViewById(R.id.adView_main);
        adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);*/

        //setting the ActionBar for the activity
        myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        localIfaceInfo = new ArrayList<>();
        allInterfaceAdd = new ArrayList<>();

        //referencing views
        //ImgWifiIcon = (ImageView) findViewById(R.id.wifi_icon_id);
        progressBar = findViewById(R.id.progress_bar);
        txtWifiName = findViewById(R.id.wifi_name);
        scanButton = findViewById(R.id.scan_button);


      /*  linearWifiName = (LinearLayout) findViewById(R.id.linear_wifi_name);
        //referencing LinearLayout in activity_main.xml for popup window*/
        linearMain = findViewById(R.id.linearLayout_main);


        //inflating adapter
        localNetListView  = findViewById(R.id.local_network);
        localNetAdapter = new HostAdapter(this, R.layout.list_main, localIfaceInfo);
        localNetListView.setAdapter(localNetAdapter);

        taskPublicNet = new TaskPublicNet();
        taskPublicNet.execute();

        if (BuildConfig.DEBUG){
            Log.v("DEBUG", "It's fucking debugging!!!!");
        }

        checkDatabase();

        /*
        * listener for LinearLayout WifiName: create intent depending of wifi connection state
        * if wifi is connected to AccessPoint then scan, if not send user to connect first!
        */
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(wifiInfo.getSupplicantState().equals(SupplicantState.COMPLETED)) {
                    Intent ipScanIntent = new Intent(getApplicationContext(), IpScanActivity.class);
                    ipScanIntent.putExtra("ip", ipv4);
                    ipScanIntent.putExtra("mac", mac);
                    ipScanIntent.putExtra("cidr", cidr);
                    ipScanIntent.putExtra("hostname", hostName);
                    startActivity(ipScanIntent);
                }
                else {
                    startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                }
            }
        });
    }


    /**
     * checking for database existance. if doesn't exist then download and install
     *
     */
    public void checkDatabase() {
        //download port file and create database if it doesn't exists
        if(!getDatabasePath(DatabaseHelper.DB_NAME).exists()) {
            dlPortTask = new DownloadTask(MainActivity.this);
            dlPortTask.execute();
        }
    }


    /*
    * method to convert ip from int to string format
    */
    private String getIpToString(int ip) {
        //getting local ip and converting from hex to decimal
        //int ipAddress = wifiInfo.getIpAddress();
        return String.format("%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8 & 0xff),
                (ip >> 16 & 0xff),
                (ip >> 24 & 0xff));
    }

    /**
     * method to convert byte format to string for mac address
     * @param mac
     * @return
     */
    private String macStringConvert(byte[] mac) {
        StringBuilder res1 = new StringBuilder();
        for (byte b : mac) {
            String hex = Integer.toHexString(b & 0xFF);
            if (hex.length() == 1)
                hex = "0".concat(hex);
            res1.append(hex.concat(":"));
        }

        if (res1.length() > 0) {
            res1.deleteCharAt(res1.length() - 1);
        }
        return res1.toString();
    }

    /**
     * calculate wifi channel access point is transmitting eg 1,6,11..
     * @param freq
     * @return
     */
    public static int convertFrequencyToChannel(int freq) {
        if (freq >= 2412 && freq <= 2484) {
            return (freq - 2412) / 5 + 1;
        } else if (freq >= 5170 && freq <= 5825) {
            return (freq - 5170) / 5 + 34;
        } else {
            return -1;
        }
    }

    /**
     * TaskPublicNet is a background process to get network interface information
     * @param
     */
    private class TaskPublicNet extends AsyncTask<Void, String, Void> {
        Enumeration<NetworkInterface> networkInterfaces = null;
        String output = "";
        int progress = 0;
        String strRegex = "\""; //removing quotes
        final static int PROGRESS_MAX = 100; //progress bar

        @Override
        protected void onPreExecute() {
            progressBar.setMax(PROGRESS_MAX);
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(1);

            connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            netInfo = connMgr.getActiveNetworkInfo();

            if (netInfo != null && netInfo.isConnected()) {
                txtWifiName.setText(netInfo.getExtraInfo().replace("\"", "").toUpperCase());
            }

            wifiMan = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
            wifiInfo = wifiMan.getConnectionInfo();
            dhcpInfo = wifiMan.getDhcpInfo();

        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                networkInterfaces = NetworkInterface.getNetworkInterfaces();
            } catch (SocketException e) {
                e.printStackTrace();
            }
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                try {
                    /*
                     * adding to localIfaceInfo arraylist all interface
                     * and calculating cidr for ipv4 address
                     */
                    for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                        //getting local ipv4 interface info
                        if (address.getBroadcast() != null && !address.getAddress().isLoopbackAddress()) {
                            byte[] macAdd = networkInterface.getHardwareAddress();
                            mac = macStringConvert(macAdd);
                            ipv4 = address.getAddress().getHostAddress();
                            hostName = address.getAddress().getCanonicalHostName();
                            cidr = address.getNetworkPrefixLength();
                            String ipAddr = ipv4+"/"+cidr;
                            allInterfaceAdd.add("ipv4 " + address.toString());
                            localIfaceInfo.add("Host: " + hostName);
                            localIfaceInfo.add("IPv4: " + ipv4);
                            localIfaceInfo.add("Mac: " + mac);
                            localIfaceInfo.add("Subnet: " + new SubnetUtils(ipAddr).getInfo().getNetmask());
                            localIfaceInfo.add("Network: " + new SubnetUtils(ipAddr).getInfo().getNetworkAddress());
                            continue;
                        }
                        //getting ipv6 interface information
                        if(address.toString().contains("wlan0") && !address.getAddress().isLoopbackAddress()) {
                            ipv6 = address.toString().substring(0, address.toString().indexOf("%"));
                            localIfaceInfo.add("IPv6 "+ ipv6.replaceAll("^/", ""));
                        }
                        allInterfaceAdd.add(address.toString());

                    }
                } catch (NullPointerException e) {
                    e.printStackTrace();
                } catch (SocketException e) {
                    e.printStackTrace();
                }
            }

            localIfaceInfo.add("DNS: " + getIpToString(dhcpInfo.dns1));
            localIfaceInfo.add("Gateway: " + getIpToString(dhcpInfo.gateway));
            localIfaceInfo.add("Signal: " + wifiInfo.getRssi() + "db");
            localIfaceInfo.add("Speed: " + wifiInfo.getLinkSpeed() + "Mbps");
            localIfaceInfo.add("SSID: " + netInfo.getExtraInfo().replace("\"", ""));
            localIfaceInfo.add("BSSID: " + wifiInfo.getBSSID());
            localIfaceInfo.add("Freq: " + wifiInfo.getFrequency() +"MHz");
            localIfaceInfo.add("Channel: "+ convertFrequencyToChannel(wifiInfo.getFrequency()));

            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {

            localIfaceInfo.add(values[0]);
            localNetAdapter.notifyDataSetInvalidated();

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressBar.setProgress(PROGRESS_MAX);

        }
    }


    /*
    * Infating the menu for toolbar
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    /*
    * actions for menu options in toolbar
    */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_ping) {
            Intent pingIntent = new Intent(MainActivity.this, PingActivity.class);
            startActivity(pingIntent);
            return true;
        }

        if (id == R.id.action_netstat) {
            Intent netstatIntent = new Intent(this, NetstatActivity.class);
            startActivity(netstatIntent);
            return true;
        }

        //Rate this app on Google play
        if (id == R.id.app_ratings) {
            Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            // To count with Play market backstack, After pressing back button,
            // to taken back to our application, we need to add following flags to intent.
            goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT |
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
            }
            return true;
        }
        if (id == R.id.action_refresh) {
            //reload this activity (refresh)
            Intent thisActivity = new Intent(this, MainActivity.class);
            startActivity(thisActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}