package org.openhab.binding.freeboxv5.model;

import java.util.ArrayList;
import java.util.List;

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

    public UpDownValue<Integer> adsl_atm;
    public UpDownValue<Double> adsl_noise_margin;
    public UpDownValue<Double> adsl_attenuation;
    public UpDownValue<Integer> adsl_fec;
    public UpDownValue<Integer> adsl_crc;
    public UpDownValue<Integer> adsl_hec;

    // Wifi
    public boolean wifi_state;
    public String wifi_model;
    public int wifi_channel;
    public boolean wifi_net_state;
    public String wifi_ssid;
    public String wifi_type;
    public boolean wifi_freewifi_secure;
    public boolean wifi_freewifi;

    // Network
    public String network_mac;
    public String network_ip_public;
    public boolean network_ipv6;
    public boolean network_router;
    public String network_ip_private;
    public String network_ip_dmz;
    public String network_ip_freeplayer;
    public boolean network_ping;
    public boolean network_wol_proxy;
    public boolean network_dhcp;
    public String[] network_dyn_range;
    public List<DhcpLease> network_leases = new ArrayList<>();

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
        buff.append(", Network={");
        buff.append("ip_public=").append(network_ip_public);
        buff.append(", ");
        buff.append("ip_private=").append(network_ip_private);
        buff.append(", ");
        buff.append("freeplayer=").append(network_ip_freeplayer);
        buff.append(", ");
        buff.append("dmz=").append(network_ip_dmz);
        buff.append(", ");
        buff.append("ipv6=").append(network_ipv6);
        buff.append(", ");
        buff.append("router=").append(network_router);
        buff.append(", ");
        buff.append("ping=").append(network_ping);
        buff.append(", ");
        buff.append("wol=").append(network_wol_proxy);
        buff.append(", ");
        buff.append("dhcp=").append(network_dhcp);
        buff.append(", ");
        buff.append("leases=").append(network_leases);
        buff.append("}");
        buff.append('}');
        return buff.toString();
    }
}