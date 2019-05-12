package org.openhab.binding.freeboxv5.model;

public class DhcpLease {

    public final String mac;
    public final String ip;

    public DhcpLease(final String mac, final String ip) {
        this.mac = mac;
        this.ip = ip;
    }

    @Override
    public String toString() {
        return '{' + mac + ',' + ip + '}';
    }
}
