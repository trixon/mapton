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
package org.mapton.gmapsfx.api;

import java.io.IOException;
import java.io.InputStream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.commons.io.IOUtils;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MapStyle {

    private final StringProperty mName = new SimpleStringProperty();
    private final StringProperty mStyle = new SimpleStringProperty();

    public static String getStyle(String name) {
        String style = "";

        for (MapStyle mapStyle : Lookup.getDefault().lookupAll(MapStyle.class)) {
            if (mapStyle.getName().equalsIgnoreCase(name)) {
                style = mapStyle.getStyle();
            }
        }

        return style;
    }

    public MapStyle(String name, String resourceName) {
        setName(name);
        setStyle(loadFromResource(resourceName));
    }

    public final String getName() {
        return mName.get();
    }

    public final String getStyle() {
        return mStyle.get();
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public final void setName(String value) {
        mName.set(value);
    }

    public final void setStyle(String value) {
        mStyle.set(value);
    }

    public StringProperty styleProperty() {
        return mStyle;
    }

    private String loadFromResource(String filename) {
        InputStream inputStream = getClass().getResourceAsStream("/" + SystemHelper.getPackageAsPath(getClass()) + filename);

        try {
            return IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return "";
    }

}
