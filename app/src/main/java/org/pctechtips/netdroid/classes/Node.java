package org.pctechtips.netdroid;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by george on 2/14/17.
 */

public class Node {
    public String ip;
    public String mac;
    public String hostName = "";
    public String remark;
    public boolean isReachable;
    public int progressBar;
    public String netBios = "";
    public ArrayList<String> allInterface = new ArrayList<>();
    public List<String> openPorts = new ArrayList();

    public Node() { }

    public Node(String ip) {
        this.ip = ip;
    }

    public Node(String ip, String mac) {
        this.ip = ip;
        this.mac = mac;
    }

    public String getNetBios() {
        return netBios;
    }

    public void setNetBios(String netBios) {
        this.netBios = netBios;
    }

    public ArrayList<String> getAllInterface() {
        return allInterface;
    }

    public List<String> getOpenPorts() {
        return openPorts;
    }

    public void setProgressBar(int progressBar) {
        this.progressBar = progressBar;
    }

    public String getIp() {
        return ip;
    }

    public String getHostName() {
        return hostName;
    }

    public String getMac() {
        return mac;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public List<String> getPorts() { return openPorts; }

    public int getProgressBar() { return progressBar; }

    public String getRemark() {
        return remark;
    }

    public boolean isReachable() {
        return isReachable;
    }

    @Override
    public String toString() {
        return "IP: " + ip + "\n" +
                "MAC: " + mac + "\n" +
                "HostName:\t" + hostName + "\n" +
                "isReachable: " + isReachable +
                "\n" + remark;
    }
}