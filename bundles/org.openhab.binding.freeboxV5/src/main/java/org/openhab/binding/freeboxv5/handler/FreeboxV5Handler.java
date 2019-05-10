/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freeboxv5.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.freeboxv5.FreeboxV5BindingConstants;
import org.openhab.binding.freeboxv5.config.FreeboxV5ServerConfiguration;
import org.openhab.binding.freeboxv5.model.FreeboxV5Status;
import org.openhab.binding.freeboxv5.model.UpDownValue;
import org.openhab.binding.freeboxv5.parser.FreeboxV5StatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxV5Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - updated to a bridge handler and delegate few things to another handler
 * @author Guilhem Bonnefille - adaptation to FreeboxV5 model
 */
public class FreeboxV5Handler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5Handler.class);

    private final FreeboxV5StatusParser parser = new FreeboxV5StatusParser();

    private ScheduledFuture<?> globalJob;

    private long previousUptime;

    public FreeboxV5Handler(Bridge bridge) {
        super(bridge);

        globalJob = null;
    }

    @Override
    public void initialize() {
        logger.debug("initializing Freebox Server handler for thing {}", getThing().getUID());

        FreeboxV5ServerConfiguration configuration = getConfigAs(FreeboxV5ServerConfiguration.class);
        if ((configuration != null) && StringUtils.isNotEmpty(configuration.fqdn)) {
            updateStatus(ThingStatus.OFFLINE);

            if (globalJob == null || globalJob.isCancelled()) {
                long pollingInterval = configuration.refreshInterval;
                logger.debug("Scheduling server state update every {} seconds...", pollingInterval);
                globalJob = scheduler.scheduleAtFixedRate(this::updateServerState, 1, pollingInterval,
                        TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Freebox Server FQDN not set in the thing configuration");
        }
    }

    private void updateServerState() {
        logger.debug("Polling server state...");

        FreeboxV5ServerConfiguration configuration = getConfigAs(FreeboxV5ServerConfiguration.class);
        String result = null;
        try {
            result = HttpUtil.executeUrl("GET", "http://" + configuration.fqdn + "/pub/fbx_info.txt", 5000);

            InputStream resultIS = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));

            FreeboxV5Status status = parser.parse(resultIS);
            updateStatus(ThingStatus.ONLINE);

            updateServerState(status);
        } catch (IOException e) {
            logger.debug("Failed to parse data from {}", configuration.fqdn, e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }

        logger.debug("Server state polled.");
    }

    private void updateServerState(FreeboxV5Status status) {
        Map<String, String> properties = editProperties();
        if (StringUtils.isNotEmpty(status.model)) {
            properties.put(Thing.PROPERTY_MODEL_ID, status.model);
        }
        if (StringUtils.isNotEmpty(status.fwversion)) {
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, status.fwversion);
        }
        updateProperties(properties);

        updateChannelDecimalState("general", FreeboxV5BindingConstants.UPTIME, status.uptime);
        updateChannelSwitchState("general", FreeboxV5BindingConstants.RESTARTED, status.uptime < previousUptime);
        previousUptime = status.uptime;

        // ADSL
        updateChannelStringState("adsl", FreeboxV5BindingConstants.ADSL_STATE, status.adsl_state);
        updateChannelStringState("adsl", FreeboxV5BindingConstants.ADSL_MODE, status.adsl_mode);
        updateChannelStringState("adsl", FreeboxV5BindingConstants.ADSL_PROTO, status.adsl_protocol);

        updateChannelIntegerState(FreeboxV5BindingConstants.ADSL_ATM, status.adsl_atm);
        updateChannelDoubleState(FreeboxV5BindingConstants.ADSL_NOISE, status.adsl_noise_margin);
        updateChannelDoubleState(FreeboxV5BindingConstants.ADSL_ATTEN, status.adsl_attenuation);
        updateChannelIntegerState(FreeboxV5BindingConstants.ADSL_FEC, status.adsl_fec);
        updateChannelIntegerState(FreeboxV5BindingConstants.ADSL_CRC, status.adsl_crc);
        updateChannelIntegerState(FreeboxV5BindingConstants.ADSL_HEC, status.adsl_hec);

        // Wifi
        updateChannelSwitchState("wifi", FreeboxV5BindingConstants.WIFI_STATUS, status.wifi_state);
        updateChannelStringState("wifi", FreeboxV5BindingConstants.WIFI_MODEL, status.wifi_model);
        updateChannelDecimalState("wifi", FreeboxV5BindingConstants.WIFI_CHANNEL, status.wifi_channel);
        updateChannelSwitchState("wifi", FreeboxV5BindingConstants.WIFI_NET_STATE, status.wifi_net_state);
        updateChannelStringState("wifi", FreeboxV5BindingConstants.WIFI_SSID, status.wifi_ssid);
        updateChannelStringState("wifi", FreeboxV5BindingConstants.WIFI_TYPE, status.wifi_type);
        updateChannelSwitchState("wifi", FreeboxV5BindingConstants.WIFI_FREEWIFI_SEC, status.wifi_freewifi_secure);
        updateChannelSwitchState("wifi", FreeboxV5BindingConstants.WIFI_FREEWIFI, status.wifi_freewifi);

        // Network
        updateChannelStringState("network", FreeboxV5BindingConstants.NETWORK_MAC, status.network_mac);
        updateChannelStringState("network", FreeboxV5BindingConstants.NETWORK_IP_PUBLIC, status.network_ip_public);
        updateChannelSwitchState("network", FreeboxV5BindingConstants.NETWORK_IPV6, status.network_ipv6);
        updateChannelSwitchState("network", FreeboxV5BindingConstants.NETWORK_ROUTER, status.network_router);
        updateChannelStringState("network", FreeboxV5BindingConstants.NETWORK_IP_PRIVATE, status.network_ip_private);
        updateChannelStringState("network", FreeboxV5BindingConstants.NETWORK_IP_DMZ, status.network_ip_dmz);
        updateChannelStringState("network", FreeboxV5BindingConstants.NETWORK_IP_FREEPLAYER,
                status.network_ip_freeplayer);
        updateChannelSwitchState("network", FreeboxV5BindingConstants.NETWORK_PING, status.network_ping);
        updateChannelSwitchState("network", FreeboxV5BindingConstants.NETWORK_WOL_PROXY, status.network_wol_proxy);
        updateChannelSwitchState("network", FreeboxV5BindingConstants.NETWORK_DHCP, status.network_dhcp);

    }

    private void updateChannelStringState(String group, String channel, String state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new StringType(state));
    }

    private void updateChannelSwitchState(String group, String channel, boolean state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), state ? OnOffType.ON : OnOffType.OFF);
    }

    private void updateChannelDecimalState(String group, String channel, int state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DecimalType(state));
    }

    private void updateChannelDecimalState(String group, String channel, long state) {
        updateState(new ChannelUID(getThing().getUID(), group, channel), new DecimalType(state));
    }

    private void updateChannelDoubleState(String channel, UpDownValue<Double> state) {
        updateState(new ChannelUID(getThing().getUID(), channel, channel + "_up"), new DecimalType(state.up));
        updateState(new ChannelUID(getThing().getUID(), channel, channel + "_down"), new DecimalType(state.down));
    }

    private void updateChannelIntegerState(String channel, UpDownValue<Integer> state) {
        updateState(new ChannelUID(getThing().getUID(), channel, channel + "_up"), new DecimalType(state.up));
        updateState(new ChannelUID(getThing().getUID(), channel, channel + "_down"), new DecimalType(state.down));
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Freebox Server handler for thing {}", getThing().getUID());
        if (globalJob != null && !globalJob.isCancelled()) {
            globalJob.cancel(true);
            globalJob = null;
        }
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            return;
        }
        if (getThing().getStatus() == ThingStatus.UNKNOWN || (getThing().getStatus() == ThingStatus.OFFLINE
                && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR)) {
            return;
        }
        switch (channelUID.getId()) {
            default:
                logger.debug("Thing {}: unexpected command {} from channel {}", getThing().getUID(), command,
                        channelUID.getId());
                break;
        }
    }
}
