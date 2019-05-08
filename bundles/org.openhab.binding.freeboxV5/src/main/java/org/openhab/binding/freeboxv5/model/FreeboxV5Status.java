package org.openhab.binding.freeboxv5.model;

public class FreeboxV5Status {

    public String model;
    public String fwversion;
    public long uptime;

    // Phone
    public final PhoneStatus phone = new PhoneStatus();

    // ADSL
    public String adsl_state;
    public String adsl_protocol;
    public String adsl_mode;

    @Override
    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append(FreeboxV5Status.class.getName());
        buff.append('{');
        buff.append("model=").append(model);
        buff.append(", ");
        buff.append("fwversion=").append(fwversion);
        buff.append(", ");
        buff.append("uptime=").append(Long.toString(uptime));
        buff.append(", ADSL={");
        buff.append("state=").append(adsl_state);
        buff.append(", ");
        buff.append("protocol=").append(adsl_protocol);
        buff.append(", ");
        buff.append("mode=").append(adsl_mode);
        buff.append("}");
        buff.append('}');
        return buff.toString();
    }
}