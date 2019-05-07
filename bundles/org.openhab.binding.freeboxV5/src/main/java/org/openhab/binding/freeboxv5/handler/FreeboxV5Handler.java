/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freeboxv5.handler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.net.http.HttpUtil;
import org.openhab.binding.freeboxv5.config.FreeboxV5ServerConfiguration;
import org.openhab.binding.freeboxv5.model.FreeboxV5Status;
import org.openhab.binding.freeboxv5.parser.FreeboxV5StatusParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang.StringUtils;

/**
 * The {@link FreeboxV5Handler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - updated to a bridge handler and delegate few things to another handler
 * @author Guilhem Bonnefille - adaptation to FreeboxV5
 */
public class FreeboxV5Handler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5Handler.class);

    private ScheduledFuture<?> globalJob;
    private final long uptime;

    public FreeboxV5Handler(Bridge bridge) {
        super(bridge);

        globalJob = null;
        uptime = -1;
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
                globalJob = scheduler.scheduleAtFixedRate(globalRunnable, 1, pollingInterval, TimeUnit.SECONDS);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Freebox Server FQDN not set in the thing configuration");
        }
    }

    private final Runnable globalRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Polling server state...");

            FreeboxV5ServerConfiguration configuration = getConfigAs(FreeboxV5ServerConfiguration.class);
            String result = null;
            boolean httpsRequestOk = false;
            try {
                result = HttpUtil.executeUrl("GET", "http://" + configuration.fqdn + "/pub/fbx_info.txt", 5000);
            } catch (IOException e) {
                logger.debug("Can't connect to {} with HTTP", configuration.fqdn, e);
            }

            FreeboxV5StatusParser parser = new FreeboxV5StatusParser();
	        InputStream resultIS = new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8));
            try {
                FreeboxV5Status status = parser.parse(resultIS);

                Map<String, String> properties = editProperties();
                boolean update = false;
                if (StringUtils.isNotEmpty(status.fwversion)
                        && !status.fwversion.equals(properties.get(Thing.PROPERTY_FIRMWARE_VERSION))) {
                    update = true;
                    properties.put(Thing.PROPERTY_FIRMWARE_VERSION, status.fwversion);
                }
                if (update) {
                    updateProperties(properties);
                }
            } catch(IOException e) {
                // FIXME
            }
        }
    };

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
