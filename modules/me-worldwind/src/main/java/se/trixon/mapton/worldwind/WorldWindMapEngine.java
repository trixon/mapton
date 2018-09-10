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
package se.trixon.mapton.worldwind;

import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Position;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javafx.scene.Node;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.api.MEngine;
import se.trixon.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEngine.class)
public class WorldWindMapEngine extends MEngine {

    public static final String LOG_TAG = "WorldWind";
    private WorldWindowPanel mMap;
    private StyleView mStyleView;

    public WorldWindMapEngine() {
        mStyleView = new StyleView();
    }

    @Override
    public String getName() {
        return "WorldWind (NASA)";
    }

    @Override
    public Node getStyleView() {
        return mStyleView;
    }

    @Override
    public Object getUI() {
        if (mMap == null) {
            init();
            initListeners();
        }

        return mMap;
    }

    @Override
    public void onWhatsHere(String s) {
    }

    private void init() {
        mMap = new WorldWindowPanel();

        NbLog.v(LOG_TAG, "Loaded and ready");
    }

    private void initListeners() {
        mMap.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                maybeShowPopup(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                maybeShowPopup(e);
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger() && e.isShiftDown()) {
                    displayContextMenu(e.getLocationOnScreen());
                }
            }
        });

        mMap.addPositionListener((PositionEvent pe) -> {
            Position position = pe.getPosition();
            if (position != null) {
                setLatLonMouse(toLatLon(position));
            } else {
//                setLatLonMouse(null);
            }
        });

    }

    private MLatLon toLatLon(Position p) {
        return new MLatLon(
                p.getLatitude().getDegrees(),
                p.getLongitude().getDegrees()
        );
    }
}
