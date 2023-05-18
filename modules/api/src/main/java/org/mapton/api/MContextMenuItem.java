/*
 * Copyright 2023 Patrik Karlström.
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

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MContextMenuItem {

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
        return getEngine().getLockedLatitude();
    }

    protected double getLatitudeProj() {
        return getEngine().getLockedLatitudeProj();
    }

    protected double getLongitude() {
        return getEngine().getLockedLongitude();
    }

    protected double getLongitudeProj() {
        return getEngine().getLockedLongitudeProj();
    }

    protected double getZoom() {
        return getEngine().getZoom();
    }

    private MEngine getEngine() {
        return Mapton.getEngine();
    }

    public enum ContextType {
        COPY, EXTRAS, OPEN;
    }
}
