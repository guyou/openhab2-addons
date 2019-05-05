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
package org.openhab.binding.spotify.internal.api.model;

/**
 * Spotify Web Api Image data class.
 *
 * @author Andreas Stenlund - Initial contribution
 * @author Hilbrand Bouwkamp - Moved to it's own class
 */
public class Image {

    private Integer height;
    private String url;
    private Integer width;

    public Integer getHeight() {
        return height;
    }

    public String getUrl() {
        return url;
    }

    public Integer getWidth() {
        return width;
    }
}
