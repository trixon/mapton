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
package se.trixon.mapton.jxmapviewer2;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javafx.scene.Node;
import org.jxmapviewer.viewer.GeoPosition;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.api.MapController;
import se.trixon.mapton.core.api.MapEngine;
import se.trixon.mapton.core.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class JxMapViewerMapEngine extends MapEngine {

    public static final String LOG_TAG = "JxMapViewer2";
    private JxMapViewerMapController mController;
    private MapKit mMapKit;

    public JxMapViewerMapEngine() {
    }

    @Override
    public MapController getController() {
        return mController;
    }

    @Override
    public String getName() {
        return "JxMapViewer2";
    }

    @Override
    public Node getStyleView() {
        return null;
    }

    @Override
    public Object getUI() {
        if (mMapKit == null) {
            init();
            mController = new JxMapViewerMapController(mMapKit);
        }

        return mMapKit;
    }

    private void init() {
        mMapKit = new MapKit();

        final GeoPosition gp = new GeoPosition(Mapton.MYLAT, Mapton.MYLON);

        mMapKit.setZoom(5);
        mMapKit.setAddressLocation(gp);

        mMapKit.getMainMap().addMouseMotionListener(new MouseMotionAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                GeoPosition geoPosition = mMapKit.getMainMap().convertPointToGeoPosition(e.getPoint());
                mController.setLatLonMouse(mController.toLatLon(geoPosition));
            }
        });

        NbLog.v(LOG_TAG, "Loaded and ready");
    }

}
