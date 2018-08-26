/* 
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.api;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import se.trixon.mapton.core.map.MapController;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapContextMenuProvider {

    protected final MapController mMapController = MapController.getInstance();

    public EventHandler<ActionEvent> getAction() {
        return (event) -> {
        };
    }

    public abstract String getName();

    public abstract ContextType getType();

    public String getUrl() {
        return "";
    }

    protected double getLatitude() {
        return mMapController.getLatitude();
    }

    protected double getLatitudeProj() {
        return mMapController.getLatitudeProj();
    }

    protected double getLongitude() {
        return mMapController.getLongitude();
    }

    protected double getLongitudeProj() {
        return mMapController.getLongitudeProj();
    }

    protected int getZoom() {
        return mMapController.getZoom();
    }

    public enum ContextType {
        COPY, EXTRAS, OPEN;
    }
}
