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

import java.util.HashSet;
import javafx.collections.ListChangeListener;
import org.jxmapviewer.viewer.DefaultWaypoint;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.Waypoint;
import org.jxmapviewer.viewer.WaypointPainter;
import se.trixon.mapton.api.MBookmark;
import se.trixon.mapton.api.MBookmarkManager;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPlotter {

    private final MBookmarkManager mBookmarkManager = MBookmarkManager.getInstance();
    private final JxMapViewerMapEngine mEngine;

    public BookmarkPlotter(JxMapViewerMapEngine engine) {
        mEngine = engine;
        mBookmarkManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            updatePlacemarks();
        });

        updatePlacemarks();
    }

    private void updatePlacemarks() {
        HashSet<Waypoint> waypoints = new HashSet<Waypoint>();

        for (MBookmark bookmark : mBookmarkManager.getItems()) {
            if (bookmark.isDisplayMarker()) {
                GeoPosition geoPosition = new GeoPosition(bookmark.getLatitude(), bookmark.getLongitude());
                DefaultWaypoint defaultWaypoint = new DefaultWaypoint(geoPosition);
                waypoints.add(defaultWaypoint);
            }
        }

        WaypointPainter<Waypoint> waypointPainter = new WaypointPainter<>();
        waypointPainter.setWaypoints(waypoints);
        mEngine.getMap().setOverlayPainter(waypointPainter);
    }
}
