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
package org.mapton.jxmapviewer2;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import org.apache.commons.io.FileUtils;
import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.OSMTileFactoryInfo;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.TileCache;
import org.jxmapviewer.viewer.TileFactoryInfo;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MapKit extends JXMapKit {

    private JLabel mCopyrightNoticeLabel;
    private JXMapViewer mMap = getMainMap();
    private DefaultTileFactory mTileFactory;
    private TileFactoryInfo mTileFactoryInfo;

    public MapKit() {
        init();

        mTileFactoryInfo = new OSMTileFactoryInfo();
        mTileFactory = new DefaultTileFactory(mTileFactoryInfo);
        mTileFactory.setThreadPoolSize(2);
        mTileFactory.setUserAgent(getClass().getName());

        mTileFactory.setTileCache(new TileCache() {
            private File mCacheDir;

            @Override
            public BufferedImage get(URI uri) throws IOException {
                File tileFile = getCachedFile(uri);

                if (tileFile.isFile()) {
                    long tileAge = System.currentTimeMillis() - tileFile.lastModified();
                    if (tileAge < TimeUnit.DAYS.toMillis(14)) {
                        try {
                            // Tile exists in cache and has a valid age.
                            return ImageIO.read(tileFile);
                        } catch (Exception e) {
                            // nvm - fallback to osm
                        }
                    }
                }

                // Tile did not exist, was too old, or failed to load
                return super.get(uri);
            }

            @Override
            public void put(URI uri, byte[] bimg, BufferedImage bufferedImage) {
                try {
                    super.put(uri, bimg, bufferedImage);
                    File tileFile = getCachedFile(uri);
                    FileUtils.forceMkdirParent(tileFile);
                    ImageIO.write(bufferedImage, "png", tileFile);
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (NullPointerException ex) {
                    System.err.println(ex.getMessage());
                }
            }

            private File getCacheDir() {
                if (mCacheDir == null) {
                    mCacheDir = new File(Mapton.getCacheDir(), "jxmapviewer2");
                }

                return mCacheDir;
            }

            private File getCachedFile(URI uri) {
                String uriString = uri.toString().replace("?", "$");
                int beginIndex = uriString.indexOf("//") + 2;

                return new File(getCacheDir(), uriString.substring(beginIndex));
            }
        });

        setTileFactory(mTileFactory);
        try {
            mMap.setLoadingImage(ImageIO.read(MapKit.class.getResource("mapton32.png")));
        } catch (Exception ex) {
            //nvm - use default
        }

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.SOUTHWEST;

        try {
            setCopyrightNotice("<html>&nbsp;© <a href=\"\">OpenStreetMap</a> contributors&nbsp;<html>", new URI("https://www.openstreetmap.org/copyright"), gridBagConstraints);
        } catch (URISyntaxException ex) {
            // nvm
        }
    }

    public JLabel getCopyrightNoticeLabel() {
        return mCopyrightNoticeLabel;
    }

    public void removeCopyrightNotice() {
        if (mCopyrightNoticeLabel != null) {
            mMap.remove(mCopyrightNoticeLabel);
        }
    }

    public void setCopyrightNotice(String text, URI uri, GridBagConstraints gridBagConstraints) {
        removeCopyrightNotice();

        mCopyrightNoticeLabel = new JLabel();
        mCopyrightNoticeLabel.setText(text);
        mCopyrightNoticeLabel.setForeground(Color.BLACK);
        mCopyrightNoticeLabel.setBackground(Color.WHITE);
        mCopyrightNoticeLabel.setOpaque(true);
        mCopyrightNoticeLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mCopyrightNoticeLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (uri != null && mCopyrightNoticeLabel.isEnabled()) {
                    SystemHelper.desktopBrowse(uri.toString());
                }
            }
        });

        mMap.add(mCopyrightNoticeLabel, gridBagConstraints);
    }

    private void init() {
        getZoomSlider().setVisible(false);
        getZoomInButton().setVisible(false);
        getZoomOutButton().setVisible(false);
    }

}
