package org.openhab.binding.freeboxv5.model;

public class FreeboxV5Status {

    public String fwversion;
    public long uptime;

    public final PhoneStatus phone = new PhoneStatus();

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(FreeboxV5Status.class.getName());
        buff.append('{');
        buff.append("fwversion=").append(fwversion);
        buff.append(", ");
        buff.append("uptime=").append(Long.toString(uptime));
        buff.append('}');
        return buff.toString();
    }
}