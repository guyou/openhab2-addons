package org.openhab.binding.freeboxv5.model;

public class FreeboxV5Status {

    public String fwversion;
    public int uptime;

    public PhoneStatus phone;

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(FreeboxV5Status.class.getName());
        buff.append('{');
        buff.append("fwversion=").append(fwversion);
        buff.append('}');
        return buff.toString();
    }
}