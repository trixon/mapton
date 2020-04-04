/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.mapjfx;

import com.sothawo.mapjfx.Coordinate;
import com.sothawo.mapjfx.MapLabel;
import com.sothawo.mapjfx.Marker;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.Set;
import javafx.collections.ListChangeListener;
import javax.imageio.ImageIO;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.openide.util.Exceptions;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPlotter {

    private final MBookmarkManager mBookmarkManager = MBookmarkManager.getInstance();
    private final MapJfxMapEngine mEngine;
    private final Set<Marker> mMarkers = new HashSet<>();

    public BookmarkPlotter(MapJfxMapEngine engine) {
        mEngine = engine;
        mBookmarkManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            updatePlacemarks();
        });

        updatePlacemarks();
    }

    private void updatePlacemarks() {
        mMarkers.forEach((marker) -> {
            mEngine.getMapView().removeMarker(marker);
        });

        mMarkers.clear();

        BufferedImage whitePinBufferedImage = GraphicsHelper.getBufferedImage(getClass().getResource("plain-white.png"));
        Image si = GraphicsHelper.scaleImage(whitePinBufferedImage, new Dimension(48, 48));
        whitePinBufferedImage = GraphicsHelper.toBufferedImage(si);
//        whitePinBufferedImage = ImageScaler.getInstance().getScaledImage(whitePinBufferedImage, new Dimension(32, 32));

        for (MBookmark bookmark : mBookmarkManager.getItems()) {
            if (bookmark.isDisplayMarker()) {
                String markerFilename = String.format("%s.png", bookmark.getColor());
                File markerFile = new File(mEngine.getCacheDir(), markerFilename);
                if (!markerFile.isFile()) {
                    BufferedImage colorizedImage = GraphicsHelper.colorize(whitePinBufferedImage, Color.decode("#" + bookmark.getColor()));
                    try {
                        ImageIO.write(colorizedImage, "png", markerFile);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }

                Coordinate coordinate = new Coordinate(bookmark.getLatitude(), bookmark.getLongitude());
                MapLabel mapLabel = new MapLabel(bookmark.getName()).setPosition(coordinate).setVisible(true);
//                Marker marker = Marker.createProvided(Marker.Provided.RED).setPosition(coordinate).setVisible(true).attachLabel(mapLabel);
//                Marker marker = new Marker(getClass().getResource("plain-white.png")).setPosition(coordinate).setVisible(true).attachLabel(mapLabel);
                Marker marker = null;
                try {
                    int x = -whitePinBufferedImage.getWidth() / 2 + 10;
                    int y = -whitePinBufferedImage.getHeight();
                    marker = new Marker(markerFile.toURI().toURL(), x, y).setPosition(coordinate).setVisible(true).attachLabel(mapLabel);
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                }

                mEngine.getMapView().addMarker(marker);
                mMarkers.add(marker);
            }
        }
    }
}
