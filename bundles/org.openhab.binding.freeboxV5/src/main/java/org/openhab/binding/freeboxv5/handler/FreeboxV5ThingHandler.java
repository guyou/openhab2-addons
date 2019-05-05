/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.freeboxv5.handler;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.freeboxv5.FreeboxV5BindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxV5ThingHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier
 */
public class FreeboxV5ThingHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5ThingHandler.class);

    private ScheduledFuture<?> phoneJob;
    private ScheduledFuture<?> callsJob;
    private FreeboxV5Handler bridgeHandler;
    private Calendar lastPhoneCheck;
    private String netAddress;
    private String airPlayName;
    private String airPlayPassword;

    public FreeboxV5ThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if ((getThing().getStatus() == ThingStatus.OFFLINE)
                && ((getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.BRIDGE_OFFLINE)
                        || (getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR))) {
            return;
        }
    }

    @Override
    public void initialize() {
        logger.debug("initializing handler for thing {}", getThing().getUID());
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThing(null, null);
        } else {
            initializeThing(bridge.getHandler(), bridge.getStatus());
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        initializeThing((getBridge() == null) ? null : getBridge().getHandler(), bridgeStatusInfo.getStatus());
    }

    private void initializeThing(ThingHandler thingHandler, ThingStatus bridgeStatus) {
        logger.debug("initializeThing {}", bridgeStatus);
        if (thingHandler != null && bridgeStatus != null) {

            if (bridgeStatus == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);

                bridgeHandler = (FreeboxV5Handler) thingHandler;

                if (getThing().getThingTypeUID().equals(FreeboxV5BindingConstants.FREEBOX_THING_TYPE_PHONE)) {
                    lastPhoneCheck = Calendar.getInstance();

                    if (phoneJob == null || phoneJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone state job every {} seconds...", pollingInterval);
                            phoneJob = scheduler.scheduleAtFixedRate(phoneRunnable, 1, pollingInterval,
                                    TimeUnit.SECONDS);
                        }
                    }

                    if (callsJob == null || callsJob.isCancelled()) {
                        long pollingInterval = getConfigAs(FreeboxPhoneConfiguration.class).refreshPhoneCallsInterval;
                        if (pollingInterval > 0) {
                            logger.debug("Scheduling phone calls job every {} seconds...", pollingInterval);
                            callsJob = scheduler.scheduleAtFixedRate(callsRunnable, 1, pollingInterval,
                                    TimeUnit.SECONDS);
                        }
                    }

                } else if (getThing().getThingTypeUID().equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_DEVICE)) {
                    netAddress = getConfigAs(FreeboxNetDeviceConfiguration.class).macAddress;
                } else if (getThing().getThingTypeUID()
                        .equals(FreeboxBindingConstants.FREEBOX_THING_TYPE_NET_INTERFACE)) {
                    netAddress = getConfigAs(FreeboxNetInterfaceConfiguration.class).ipAddress;
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private final Runnable phoneRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Polling phone state...");

            try {
                fetchPhone();

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

            } catch (Throwable t) {
                if (t instanceof FreeboxException) {
                    logger.error("Phone state job - FreeboxException: {}", ((FreeboxException) t).getMessage());
                } else if (t instanceof Exception) {
                    logger.error("Phone state job - Exception: {}", ((Exception) t).getMessage());
                } else if (t instanceof Error) {
                    logger.error("Phone state job - Error: {}", ((Error) t).getMessage());
                } else {
                    logger.error("Phone state job - Unexpected error");
                }
                StringWriter sw = new StringWriter();
                if ((t instanceof RuntimeException) && (t.getCause() != null)) {
                    t.getCause().printStackTrace(new PrintWriter(sw));
                } else {
                    t.printStackTrace(new PrintWriter(sw));
                }
                logger.error("{}", sw);
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }

        }
    };

    private final Runnable callsRunnable = new Runnable() {
        @Override
        public void run() {
            logger.debug("Polling phone calls...");

            try {
                fetchNewCalls();

                if (getThing().getStatus() == ThingStatus.OFFLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

            } catch (Throwable t) {
                if (t instanceof FreeboxException) {
                    logger.error("Phone calls job - FreeboxException: {}", ((FreeboxException) t).getMessage());
                } else if (t instanceof Exception) {
                    logger.error("Phone calls job - Exception: {}", ((Exception) t).getMessage());
                } else if (t instanceof Error) {
                    logger.error("Phone calls job - Error: {}", ((Error) t).getMessage());
                } else {
                    logger.error("Phone calls job - Unexpected error");
                }
                StringWriter sw = new StringWriter();
                if ((t instanceof RuntimeException) && (t.getCause() != null)) {
                    t.getCause().printStackTrace(new PrintWriter(sw));
                } else {
                    t.printStackTrace(new PrintWriter(sw));
                }
                logger.error("{}", sw);
                if (getThing().getStatus() == ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }

        }
    };

    @Override
    public void dispose() {
        logger.debug("Disposing handler for thing {}", getThing().getUID());
        if (phoneJob != null && !phoneJob.isCancelled()) {
            phoneJob.cancel(true);
            phoneJob = null;
        }
        if (callsJob != null && !callsJob.isCancelled()) {
            callsJob.cancel(true);
            callsJob = null;
        }
        super.dispose();
    }

}
