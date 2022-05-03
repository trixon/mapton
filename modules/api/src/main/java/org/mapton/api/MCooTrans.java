/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.api;

import javafx.geometry.Point2D;

/**
 *
 * @author Patrik Karlström
 */
public interface MCooTrans {

    public static MCooTrans getCooTrans(String name) {
        var items = MCrsManager.getInstance().getItems();
        for (var cooTrans : items) {
            if (cooTrans.getName().equalsIgnoreCase(name)) {
                return cooTrans;
            }
        }

        var cooTrans = items.get(0);
        MOptions.getInstance().setMapCooTrans(cooTrans.getName());

        return cooTrans;
    }

    Point2D fromWgs84(double latitude, double longitude);

    MBounds getBoundsProjected();

    MBounds getBoundsWgs84();

    double getLatitude(double latitude, double longitude);

    String getLatitudeString(double latitude, double longitude);

    double getLongitude(double latitude, double longitude);

    String getLongitudeString(double latitude, double longitude);

    String getName();

    String getString(double latitude, double longitude);

    default boolean isOrthogonal() {
        return true;
    }

    boolean isWithinProjectedBounds(double latitude, double longitude);

    boolean isWithinWgs84Bounds(double latitude, double longitude);

    Point2D toWgs84(double latitude, double longitude);

}
