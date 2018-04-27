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
package se.trixon.mapton.core;

import com.lynden.gmapsfx.GoogleMapView;
import com.lynden.gmapsfx.javascript.object.GoogleMap;
import com.lynden.gmapsfx.javascript.object.MapOptions;
import java.text.DateFormat;
import org.openide.awt.StatusDisplayer;
import se.trixon.mapton.core.map.MapTopComponent;

/**
 *
 * @author Patrik Karlström
 */
public class Mapton {

    public static final String LOG_TAG = "Mapton";
    private MapTopComponent mMapTopComponent;

    public static void clearStatusText() {
        setStatusText("");
    }

    public static DateFormat getDefaultDateFormat() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    }

    public static Mapton getInstance() {
        return Holder.INSTANCE;
    }

    public static void setStatusText(String text, int importance) {
        StatusDisplayer.getDefault().setStatusText(text, importance);
    }

    public static void setStatusText(String text) {
        setStatusText(text, StatusDisplayer.IMPORTANCE_ANNOTATION);
    }

    private Mapton() {
    }

    public GoogleMap getMap() {
        return getMapTopComponent().getMap();
    }

    public MapOptions getMapOptions() {
        return getMapTopComponent().getMapOptions();
    }

    public MapTopComponent getMapTopComponent() {

        return mMapTopComponent;
    }

    public GoogleMapView getMapView() {
        return getMapTopComponent().getMapView();
    }

    public void setMapTopComponent(MapTopComponent mapTopComponent) {
        mMapTopComponent = mapTopComponent;
    }

    private static class Holder {

        private static final Mapton INSTANCE = new Mapton();
    }
}
