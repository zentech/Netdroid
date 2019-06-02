package org.pctechtips.netdroid.runnable;

import android.util.Log;

import org.pctechtips.netdroid.Node;
import org.pctechtips.netdroid.dbhelper.DatabaseHelper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by jlvaz on 3/31/2017.
 */

public class PortScanRunnable implements Callable<Node> {
    private int startPort;
    private int endPort;
    private String ipAddress;
    int progressBar;
    int timeOut = 1000; //for how long try connection in ms
    public List<String> ports;
    private DatabaseHelper portsDB;
    private Node node;


    public PortScanRunnable(String ip, int startPort, int endPort, DatabaseHelper db) {
        ipAddress = ip;
        this.startPort = startPort;
        this.endPort = endPort;
        ports = new ArrayList<>();
        portsDB = db;
        node = new Node(ip);
    }

    @Override
    public Node call() {
        //Log.v("PORTRANGE", startPort+" "+endPort);
        for (int i = startPort; i <= endPort; i++) {
            try {
                Log.v("PORT: ", i+"");
                //establishing connection to every port
                Socket socket = new Socket();
                SocketAddress address = new InetSocketAddress(ipAddress, i);
                socket.connect(address, timeOut);
                progressBar = i;
                    /*creating object port and setting progress regardless
                    * a given port is open or not. Then if port is open set ip
                    * and port number*/
                if (socket.isConnected()) {
                    Log.v("OPENPORT", i+"");
                    socket.close();
                    /* adding open port and resolving port service from database */
                    node.openPorts.add(i + ">" + portsDB.getPortService(i));
                }
            } catch (UnknownHostException e) {
                //e.printStackTrace();
            } catch (SocketTimeoutException e) {
               // e.printStackTrace();
            } catch (ConnectException e) {
               // e.printStackTrace();
            } catch (IOException e) {
               // e.printStackTrace();
            }
        }
        return node;
    }

    /*
    * returns List<String> of open ports for given Node
    */
    public List<String> getOpenPorts() {
        return ports;
    }


}
