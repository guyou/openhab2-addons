/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.freeboxv5.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
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
import org.openhab.binding.freeboxv5.PhoneStatusListener;
import org.openhab.binding.freeboxv5.model.PhoneStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxV5PhoneHandler} is responsible for handling everything associated to
 * any Freebox thing types except the bridge thing type.
 *
 * @author Laurent Garnier - Initial contribution
 * @author Laurent Garnier - use new internal API manager
 * @author Guilhem Bonnefille - update to V5 model
 */
public class FreeboxV5PhoneHandler extends BaseThingHandler implements PhoneStatusListener {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5PhoneHandler.class);

    private FreeboxV5Handler bridgeHandler;

    public FreeboxV5PhoneHandler(Thing thing) {
        super(thing);
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
    public void handleCommand(ChannelUID channelUID, Command command) {
    	// Nothing to do
    }
    
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged {}", bridgeStatusInfo);
        Bridge bridge = getBridge();
        if (bridge == null) {
            initializeThing(null, bridgeStatusInfo.getStatus());
        } else {
            initializeThing(bridge.getHandler(), bridgeStatusInfo.getStatus());
        }
    }

    private void initializeThing(ThingHandler bridgeHandler, ThingStatus bridgeStatus) {
        if (bridgeHandler != null && bridgeStatus != null) {
            if (bridgeStatus == ThingStatus.ONLINE) {
                this.bridgeHandler = (FreeboxV5Handler) bridgeHandler;

                if (getThing().getThingTypeUID().equals(FreeboxV5BindingConstants.FREEBOX_THING_TYPE_PHONE)) {
                	this.bridgeHandler.registerPhoneStatusListener(this);
                }
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED);
        }
    }

    @Override
    public void update(PhoneStatus phoneStatus) {
        logger.debug("Updating phone state...");
    	if (phoneStatus.on) {
            updateGroupChannelSwitchState(FreeboxV5BindingConstants.ONHOOK, phoneStatus.onhook);
            updateGroupChannelSwitchState(FreeboxV5BindingConstants.RINGING, phoneStatus.ringing);
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    private void updateGroupChannelSwitchState(String channel, boolean state) {
        updateState(new ChannelUID(getThing().getUID(), channel), state ? OnOffType.ON : OnOffType.OFF);
    }

    @Override
    public void dispose() {
    	if (this.bridgeHandler != null)
    		this.bridgeHandler.unregisterPhoneStatusListener(this);
    	super.dispose();
    }
}
