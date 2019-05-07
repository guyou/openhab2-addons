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
package org.openhab.binding.freeboxv5;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.freeboxv5.handler.FreeboxV5Handler;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link FreeboxV5HandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Laurent Garnier - several thing types and handlers + discovery service
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.freeboxV5")
public class FreeboxV5HandlerFactory extends BaseThingHandlerFactory {

    private final Logger logger = LoggerFactory.getLogger(FreeboxV5HandlerFactory.class);

    /*
     * private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
     * .concat(FreeboxV5BindingConstants.SUPPORTED_BRIDGE_TYPES_UIDS.stream(),
     * FreeboxV5BindingConstants.SUPPORTED_THING_TYPES_UIDS.stream())
     * .collect(Collectors.toSet());
     */
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .singleton(FreeboxV5BindingConstants.FREEBOX_BRIDGE_TYPE_SERVER);

    @Override
    protected void activate(ComponentContext componentContext) {
        super.activate(componentContext);
    };

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    public Thing createThing(ThingTypeUID thingTypeUID, Configuration configuration, ThingUID thingUID,
            ThingUID bridgeUID) {
        if (thingTypeUID.equals(FreeboxV5BindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            return super.createThing(thingTypeUID, configuration, thingUID, null);
        } else if (FreeboxV5BindingConstants.SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID)) {
            ThingUID newThingUID;
            if (bridgeUID != null && thingUID != null) {
                newThingUID = new ThingUID(thingTypeUID, bridgeUID, thingUID.getId());
            } else {
                newThingUID = thingUID;
            }
            return super.createThing(thingTypeUID, configuration, newThingUID, bridgeUID);
        }
        throw new IllegalArgumentException(
                "The thing type " + thingTypeUID + " is not supported by the Freebox binding.");
    }

    @Override
    protected ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(FreeboxV5BindingConstants.FREEBOX_BRIDGE_TYPE_SERVER)) {
            FreeboxV5Handler handler = new FreeboxV5Handler((Bridge) thing);
            return handler;
        }

        return null;
    }

}
