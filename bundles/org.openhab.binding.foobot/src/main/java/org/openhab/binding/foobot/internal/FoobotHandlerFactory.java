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
package org.openhab.binding.foobot.internal;

import static org.openhab.binding.foobot.internal.FoobotBindingConstants.*;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.eclipse.smarthome.io.net.http.HttpClientFactory;
import org.openhab.binding.foobot.internal.handler.FoobotAccountHandler;
import org.openhab.binding.foobot.internal.handler.FoobotDeviceHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link FoobotHandlerFactory} is responsible for creating things and thing handlers.
 *
 * @author Divya Chauhan - Initial contribution
 * @author George Katsis - Add Bridge thing type
 * @author Hilbrand Bouwkamp - Completed implementation
 */
@Component(service = ThingHandlerFactory.class, configurationPid = "binding.foobot")
@NonNullByDefault
public class FoobotHandlerFactory extends BaseThingHandlerFactory {

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPE_UIDS = Collections
            .unmodifiableSet(Stream.of(BRIDGE_TYPE_FOOBOTACCOUNT, THING_TYPE_FOOBOT).collect(Collectors.toSet()));

    public static final Set<ThingTypeUID> DISCOVERABLE_THING_TYPE_UIDS = Collections.singleton(THING_TYPE_FOOBOT);

    private final FoobotApiConnector connector = new FoobotApiConnector();

    private @NonNullByDefault({}) HttpClient httpClient;

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPE_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        final ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_FOOBOT)) {
            return new FoobotDeviceHandler(thing, connector);
        } else if (thingTypeUID.equals(BRIDGE_TYPE_FOOBOTACCOUNT)) {
            return new FoobotAccountHandler((Bridge) thing, connector);
        }
        return null;
    }

    @Reference
    protected void setHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = httpClientFactory.getCommonHttpClient();
        connector.setHttpClient(httpClient);
    }

    protected void unsetHttpClientFactory(HttpClientFactory httpClientFactory) {
        this.httpClient = null;
        connector.setHttpClient(null);
    }
}
