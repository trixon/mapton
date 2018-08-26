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

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import javax.swing.JToolTip;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.NbLog;
import se.trixon.mapton.core.api.MapEngine;
import se.trixon.mapton.core.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapEngine.class)
public class JxMapViewerMapEngine extends MapEngine {

    public static final String LOG_TAG = "JxMapViewer2";

    private JXMapKit mMapKit;

    public JxMapViewerMapEngine() {
    }

    @Override
    public String getName() {
        return "JxMapViewer2";
    }

    @Override
    public Object getUI() {
        if (mMapKit == null) {
            init();
        }

        return mMapKit;
    }

    private void init() {
        mMapKit = new JXMapKit();
        TileFactoryInfo info = new OSMTileFactoryInfo();
        DefaultTileFactory tileFactory = new DefaultTileFactory(info);
        mMapKit.setTileFactory(tileFactory);

        final GeoPosition gp = new GeoPosition(Mapton.MYLAT, Mapton.MYLON);

        final JToolTip tooltip = new JToolTip();
        tooltip.setTipText("Mölndal");
        tooltip.setComponent(mMapKit.getMainMap());
        mMapKit.getMainMap().add(tooltip);

        mMapKit.setZoom(5);
        mMapKit.setAddressLocation(gp);

        mMapKit.getMainMap().addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseDragged(MouseEvent e) {
                // ignore
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                JXMapViewer map = mMapKit.getMainMap();

                // convert to world bitmap
                Point2D worldPos = map.getTileFactory().geoToPixel(gp, map.getZoom());

                // convert to screen
                Rectangle rect = map.getViewportBounds();
                int sx = (int) worldPos.getX() - rect.x;
                int sy = (int) worldPos.getY() - rect.y;
                Point screenPos = new Point(sx, sy);

                // check if near the mouse
                if (screenPos.distance(e.getPoint()) < 20) {
                    screenPos.x -= tooltip.getWidth() / 2;

                    tooltip.setLocation(screenPos);
                    tooltip.setVisible(true);
                } else {
                    tooltip.setVisible(false);
                }
            }
        });

        NbLog.v(LOG_TAG, "Loaded and ready");
    }

}
