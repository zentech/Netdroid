package org.pctechtips.netdroid.runnable;

import org.pctechtips.netdroid.Node;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * IpScanRunnable will try to establish a connection with
 * retmote host using Socket class on port 7. This way populating
 * /proc/net/arp table on localhost.
 */

public class IpScanRunnable implements Callable<Node> {
    private static final String TAG = "IPSCANRUNNABLE";
    private final static int TIMEOUT = 1000;
    private final static int PORT = 7;
    private String subnet;
    private Integer startIp;
    private Integer stopIp;
    private Node node;

    public IpScanRunnable(String subnet, int start, int stop) {
        this.subnet = subnet;
        this.startIp = start;
        this.stopIp = stop;

    }

    /**
     * establish Socket connection on port 7 (echo) remote host
     */

    @Override
    public Node call() {
        Socket socket = null;
        for(int i = startIp; i < stopIp; i++) {
            String ip = subnet + "." + i;
            socket = new Socket();
            try {
                InetAddress ipAdd = InetAddress.getByName(ip);
                node = new Node(startIp+"");
                byte[] ipBytes = ipAdd.getAddress();
                socket.setTcpNoDelay(true);
                socket.connect(new InetSocketAddress(InetAddress.getByAddress(ipBytes), PORT), TIMEOUT);
            }
            catch (Exception ex){

            }
        }
        return node;
    }

}

