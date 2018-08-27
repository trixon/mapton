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

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapContextMenuProvider {

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
        return getMapController().getLatitude();
    }

    protected double getLatitudeProj() {
        return getMapController().getLatitudeProj();
    }

    protected double getLongitude() {
        return getMapController().getLongitude();
    }

    protected double getLongitudeProj() {
        return getMapController().getLongitudeProj();
    }

    protected int getZoom() {
        return getMapController().getZoom();
    }

    private MapController getMapController() {
        return Mapton.getController();
    }

    public enum ContextType {
        COPY, EXTRAS, OPEN;
    }
}
