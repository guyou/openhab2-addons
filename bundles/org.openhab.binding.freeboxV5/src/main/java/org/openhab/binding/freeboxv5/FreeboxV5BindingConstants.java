/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freeboxv5;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FreeboxBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class FreeboxV5BindingConstants {

    public static final String BINDING_ID = "freeboxV5";

    // List of all Bridge Type UIDs
    public static final ThingTypeUID FREEBOX_BRIDGE_TYPE_SERVER = new ThingTypeUID(BINDING_ID, "server");

    // List of all Thing Type UIDs
    public static final ThingTypeUID FREEBOX_THING_TYPE_PHONE = new ThingTypeUID(BINDING_ID, "phone");

    // All supported Bridge types
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_TYPES_UIDS = Collections
            .singleton(FREEBOX_BRIDGE_TYPE_SERVER);

    // All supported Thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(FREEBOX_BRIDGE_TYPE_SERVER, FREEBOX_THING_TYPE_PHONE).collect(Collectors.toSet()));

    // List of properties
    public static final String API_BASE_URL = "apiBaseUrl";
    public static final String API_VERSION = "apiVersion";

    // List of all Group Channel ids
    public static final String STATE = "state";
    public static final String ANY = "any";
    public static final String ACCEPTED = "accepted";
    public static final String MISSED = "missed";
    public static final String OUTGOING = "outgoing";

    // List of all Channel ids
    public static final String FWVERSION = "fwversion";
    public static final String UPTIME = "uptime";
    public static final String RESTARTED = "restarted";
    public static final String ADSL_STATE = "adsl_state";
    public static final String ADSL_MODE = "adsl_mode";
    public static final String ADSL_PROTO = "adsl_protocol";
    public static final String ADSL_ATM = "adsl_atm";
    public static final String ADSL_ATTEN = "adsl_attenuation";
    public static final String ADSL_NOISE = "adsl_noise_margin";
    public static final String ADSL_FEC = "adsl_fec";
    public static final String ADSL_CRC = "adsl_crc";
    public static final String ADSL_HEC = "adsl_hec";
    public static final String WIFI_STATUS = "wifi_status";
    public static final String WIFI_MODEL = "wifi_model";
    public static final String WIFI_CHANNEL = "wifi_channel";
    public static final String WIFI_NET_STATE = "wifi_net_state";
    public static final String WIFI_SSID = "wifi_ssid";
    public static final String WIFI_TYPE = "wifi_type";
    public static final String WIFI_FREEWIFI = "wifi_freewifi";
    public static final String WIFI_FREEWIFI_SEC = "wifi_freewifi_secure";
    public static final String ONHOOK = "onhook";
    public static final String RINGING = "ringing";
    public static final String REBOOT = "reboot";
    public static final String REACHABLE = "reachable";
    public static final String PLAYURL = "playurl";
    public static final String STOP = "stop";

    public static final String NETWORK_MAC = "network_mac";
    public static final String NETWORK_IPV6 = "network_ipv6";
    public static final String NETWORK_IP_PUBLIC = "network_ip_public";
    public static final String NETWORK_ROUTER = "network_router";
    public static final String NETWORK_IP_DMZ = "network_ip_dmz";
    public static final String NETWORK_IP_PRIVATE = "network_ip_private";
    public static final String NETWORK_IP_FREEPLAYER = "network_ip_freeplayer";
    public static final String NETWORK_PING = "network_ping";
    public static final String NETWORK_WOL_PROXY = "network_wol_proxy";
    public static final String NETWORK_DHCP = "network_dhcp";
}
