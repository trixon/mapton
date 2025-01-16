/*
 * Copyright 2024 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.util.HashMap;

/**
 *
 * @author Patrik Karlström
 */
public enum PinPaddle {
    N_BLANK("http://maps.google.com/mapfiles/kml/paddle/wht-blank.png", 0),
    N_CIRCLE("http://maps.google.com/mapfiles/kml/paddle/wht-circle.png", 0),
    N_DIAMOND("http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png", 0),
    N_SQUARE("http://maps.google.com/mapfiles/kml/paddle/wht-square.png", 0),
    N_STAR("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png", 0),
    E_BLANK("http://maps.google.com/mapfiles/kml/paddle/wht-blank.png", 90),
    E_CIRCLE("http://maps.google.com/mapfiles/kml/paddle/wht-circle.png", 90),
    E_DIAMOND("http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png", 90),
    E_SQUARE("http://maps.google.com/mapfiles/kml/paddle/wht-square.png", 90),
    E_STAR("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png", 90),
    S_BLANK("http://maps.google.com/mapfiles/kml/paddle/wht-blank.png", 180),
    S_CIRCLE("http://maps.google.com/mapfiles/kml/paddle/wht-circle.png", 180),
    S_DIAMOND("http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png", 180),
    S_SQUARE("http://maps.google.com/mapfiles/kml/paddle/wht-square.png", 180),
    S_STAR("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png", 180),
    W_BLANK("http://maps.google.com/mapfiles/kml/paddle/wht-blank.png", 270),
    W_CIRCLE("http://maps.google.com/mapfiles/kml/paddle/wht-circle.png", 270),
    W_DIAMOND("http://maps.google.com/mapfiles/kml/paddle/wht-diamond.png", 270),
    W_SQUARE("http://maps.google.com/mapfiles/kml/paddle/wht-square.png", 270),
    W_STAR("http://maps.google.com/mapfiles/kml/paddle/wht-stars.png", 270);
    private final String mAddress;
    private final double mHeading;
    private final HashMap<Double, Offset> mHeadingToOffset = new HashMap<>();

    private PinPaddle(String address, double heading) {
        mAddress = address;
        mHeading = heading;
        mHeadingToOffset.put(0d, Offset.BOTTOM_CENTER);
        mHeadingToOffset.put(90d, Offset.LEFT_CENTER);
        mHeadingToOffset.put(180d, Offset.TOP_CENTER);
        mHeadingToOffset.put(270d, Offset.RIGHT_CENTER);
    }

    public void apply(PointPlacemarkAttributes attrs) {
        attrs.setImageAddress(mAddress);
        attrs.setImageOffset(Offset.BOTTOM_CENTER);
    }

    public PointPlacemarkAttributes applyToCopy(PointPlacemarkAttributes attrs) {
        var a = new PointPlacemarkAttributes(attrs);
        a.setImageAddress(mAddress);
        a.setHeading(mHeading);
        a.setScale(attrs.getScale() * 1.1);
        a.setImageOffset(mHeadingToOffset.getOrDefault(mHeading, Offset.CENTER));
        return a;
    }

    public String getAddress() {
        return mAddress;
    }
}
