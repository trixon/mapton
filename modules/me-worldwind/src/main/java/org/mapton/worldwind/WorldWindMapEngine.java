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
package org.mapton.worldwind;

import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import gov.nasa.worldwind.Configuration;
import gov.nasa.worldwind.View;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.event.PositionEvent;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Box;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.geom.Vec4;
import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.HierarchyBoundsListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.prefs.PreferenceChangeEvent;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.scene.Node;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MAttribution;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.MapStyle;
import org.mapton.worldwind.ruler.RulerTabPane;
import org.openide.modules.Places;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEngine.class)
public class WorldWindMapEngine extends MEngine {

    public static final String LOG_TAG = "WorldWind";
    private boolean mInProgress;
    private boolean mInitialized;
    private final LayerView mLayerView;
    private JPanel mMainPanel;
    private WorldWindowPanel mMap;
    private double mOldAltitude;
    private double mOldGlobalZoom;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final RulerTabPane mRulerTabPane;
    private final StyleView mStyleView;
    private SwingNode mSwingNode;
    private long mZoomEpoch = System.currentTimeMillis();
    private final double[] mZoomLevels;

    public WorldWindMapEngine() {
        System.setProperty("mapton.cache", Places.getCacheDirectory().getAbsolutePath());
        Configuration.setValue(
                AVKey.DATA_FILE_STORE_CONFIGURATION_FILE_NAME,
                "org/mapton/worldwind/CacheLocationConfiguration.xml"
        );
        mStyleView = new StyleView();
        mLayerView = LayerView.getInstance();
        mRulerTabPane = RulerTabPane.getInstance();

        mZoomLevels = new double[]{
            48374812,//1 -- Flat
            35820953,//2
            24091408,//3
            13221640,//4
            8019342,//5
            3992765,//6
            1089305,//7 --Globe
            444742,//8
            244785,//9
            121500,//10
            56042,//11
            27946,//12
            17106,//13 15000?
            7692,//14
            3845,//15
            2340,//16
            959,//17
            482,//18
            294,//19
            148,//20
            84//21
        };
    }

    @Override
    public void create(Runnable postCreateRunnable) {
        if (mMainPanel == null) {
            initMainPanel();

            new Thread(() -> {
                init();
                initListeners();

                SwingHelper.runLater(() -> {
                    mMainPanel.removeAll();
                    mMainPanel.add(mMap, BorderLayout.CENTER);
                    postCreateRunnable.run();
                });
            }, getClass().getCanonicalName()).start();
        } else {
            postCreateRunnable.run();
        }
    }

    @Override
    public void fitToBounds(MLatLonBox latLonBox) {
        if (!mInitialized || (latLonBox.getLatitudeSpan() == 0 && latLonBox.getLongitudeSpan() == 0)) {
            return;
        }

        try {
            fitToBounds(toSector(latLonBox));
        } catch (NullPointerException e) {
            //nvm
        }
    }

    @Override
    public MLatLon getCenter() {
        Vec4 centerPoint = mMap.getView().getCenterPoint();
        Position centerPosition = mMap.getView().getGlobe().computePositionFromPoint(centerPoint);

        return toLatLon(centerPosition);
    }

    @Override
    public Node getLayerView() {
        return mLayerView;
    }

    public WorldWindowPanel getMap() {
        return mMap;
    }

    @Override
    public JComponent getMapComponent() {
        updateToolbarDocumentInfo();

        return mMainPanel;
    }

    @Override
    public Node getMapNode() {
        if (mSwingNode == null) {
            mSwingNode = new SwingNode();
            getMapComponent();
            refreshUI();
        }

        return mSwingNode;
    }

    @Override
    public String getName() {
        return "WorldWind";
    }

    @Override
    public Node getRulerView() {
        return mRulerTabPane;
    }

    @Override
    public Node getStyleView() {
        return mStyleView;
    }

    @Override
    public double getZoom() {
        return toGlobalZoom();
    }

    @Override
    public void onActivate() {
        var view = mMap.getView();
        view.setHeading(Angle.fromDegrees(mOptions.getDouble(KEY_VIEW_HEADING)));
        view.setPitch(Angle.fromDegrees(mOptions.getDouble(KEY_VIEW_PITCH)));
        view.setRoll(Angle.fromDegrees(mOptions.getDouble(KEY_VIEW_ROLL)));
    }

    @Override
    public void onClosing() {
        onDeactivate();
    }

    @Override
    public void onDeactivate() {
        var view = mMap.getView();
        mOptions.put(KEY_VIEW_HEADING, view.getHeading().getDegrees());
        mOptions.put(KEY_VIEW_PITCH, view.getPitch().getDegrees());
        mOptions.put(KEY_VIEW_ROLL, view.getRoll().getDegrees());
        mOptions.put(KEY_VIEW_ALTITUDE, view.getEyePosition().getAltitude());
    }

    @Override
    public void onStyleSwap() {
        String prevStyle = StringUtils.defaultIfBlank(mOptions.get(KEY_MAP_STYLE_PREV), DEFAULT_MAP_STYLE);
        String currentStyle = mOptions.get(KEY_MAP_STYLE);
        mOptions.put(KEY_MAP_STYLE, prevStyle);
        mOptions.put(KEY_MAP_STYLE_PREV, currentStyle);
    }

    @Override
    public void onWhatsHere(String s) {
        //aaaNbMessage.information(Dict.INFORMATION.toString(), s);
    }

    @Override
    public void panTo(MLatLon latLon, double zoom) {
        if (mInitialized && SystemHelper.age(mZoomEpoch) > 1000) {
            mMap.getView().goTo(toPosition(latLon), toLocalZoom(zoom));
        }
    }

    @Override
    public void panTo(MLatLon latLon) {
        if (!mInitialized) {
            return;
        }
        View view = mMap.getView();
        Position eyePosition = view.getCurrentEyePosition();
        Angle fieldOfView = view.getFieldOfView();

        mMap.getView().goTo(toPosition(latLon), mMap.getView().getEyePosition().getAltitude());
        try {
            view.setEyePosition(eyePosition);
            view.setFieldOfView(fieldOfView);
        } catch (Exception e) {
        }
    }

    @Override
    public void refreshUI() {
        SwingUtilities.invokeLater(() -> {
            mSwingNode.setContent(mMap);

            /*
            The map doesn't resize itself well on Windows so we have to force that.
             */
            if (SystemUtils.IS_OS_WINDOWS) {
                //FIXME Can this be done in a proper way?
                new Thread(() -> {
                    mSwingNode.setVisible(false);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ex) {
                        Exceptions.printStackTrace(ex);
                        Thread.currentThread().interrupt();
                    }
                    Platform.runLater(() -> {
                        mSwingNode.resize(1, 1);
                        mSwingNode.setVisible(true);
                    });
                }, getClass().getCanonicalName()).start();
            }
        });
    }

    private void fitToBounds(Sector sector) {
        WorldWindow wwd = mMap.getWwd();

        if (sector == null) {
            throw new IllegalArgumentException();
        }

        Box extent = Sector.computeBoundingBox(wwd.getModel().getGlobe(),
                wwd.getSceneController().getVerticalExaggeration(), sector);

        Angle fieldOfView = wwd.getView().getFieldOfView();
        double zoom = extent.getRadius() / fieldOfView.cosHalfAngle() / fieldOfView.tanHalfAngle();

        // Configure OrbitView to look at the center of the sector from our estimated distance. This causes OrbitView to
        // animate to the specified position over several seconds. To affect this change immediately use the following:
        // ((OrbitView) wwd.getView()).setCenterPosition(new Position(sector.getCentroid(), 0d));
        // ((OrbitView) wwd.getView()).setZoom(zoom);
        wwd.getView().goTo(new Position(sector.getCentroid(), 0d), zoom);
    }

    private void init() {
        mMap = new WorldWindowPanel(() -> {
            var zoom = mOptions.getDouble(KEY_VIEW_ALTITUDE, -1d);
            if (zoom != -1) {
                if (mMap.getView().getGlobe() != null) {
                    mMap.getView().goTo(toPosition(options().getMapCenter()), zoom);
                }
            }

        });
        mLayerView.refresh(mMap);
        mRulerTabPane.refresh(mMap);
        setImageRenderer(mMap.getImageRenderer());

        Mapton.getLog().i(LOG_TAG, "Loaded and ready");
    }

    private void initListeners() {
        mMap.addMouseListener(new MouseAdapter() {
            private Point mPoint;

            @Override
            public void mousePressed(MouseEvent e) {
                mPoint = e.getPoint();
                hideContextMenu();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                double distance = mPoint.distance(e.getPoint());
                if (e.getButton() == MouseEvent.BUTTON3 && distance < 3) {
                    displayContextMenu(e.getLocationOnScreen());
                }
            }

            private void maybeShowPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    displayContextMenu(e.getLocationOnScreen());
                }
            }
        });

        mMap.addPositionListener((PositionEvent pe) -> {
            Position position = pe.getPosition();
            if (position != null) {
                Double altitude = null;
                if (mMap.getView() != null && mMap.getView().getEyePosition() != null) {
                    altitude = mMap.getView().getEyePosition().getElevation();
                    if (Math.abs(mOldAltitude - altitude) > 1) {
                        double globalZoom = toGlobalZoom();
                        if (Math.abs(mOldGlobalZoom - globalZoom) > 1 / mZoomLevels.length) {
                            mZoomEpoch = System.currentTimeMillis();
                            mOldGlobalZoom = globalZoom;
                            Mapton.getInstance().zoomProperty().set(globalZoom);
                        }
                    }
                }
                setStatusMousePositionData(toLatLon(position), position.getElevation(), altitude);
            } else {
//                setStatusMousePositionData(null, null, null);
            }
        });

        mMap.addGLEventListener(new GLEventListener() {
            private boolean runOnce = true;

            @Override
            public void display(GLAutoDrawable drawable) {
                mInitialized = true;
                if (runOnce) {
                    initialized();
                    runOnce = false;
                    mMap.addHierarchyBoundsListener(new HierarchyBoundsListener() {
                        @Override
                        public void ancestorMoved(HierarchyEvent e) {
                            //nvm
                        }

                        @Override
                        public void ancestorResized(HierarchyEvent hierarchyEvent) {
                            try {
                                mMap.redrawNow();
                            } catch (Exception e) {
                            }
                        }
                    });
                }
            }

            @Override
            public void dispose(GLAutoDrawable drawable) {
            }

            @Override
            public void init(GLAutoDrawable drawable) {
            }

            @Override
            public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
            }
        });

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case ModuleOptions.KEY_MAP_STYLE:
                    updateToolbarDocumentInfo();
                    break;

                default:
                    break;
            }
        });

        Timer downloadTimer = new Timer(100, (event) -> {
            boolean inProgress = WorldWind.getRetrievalService().hasActiveTasks();
            if (mInProgress != inProgress) {
                mInProgress = inProgress;
                setStatusProgress(mInProgress ? -1 : 1);
            }
        });

        downloadTimer.start();
    }

    private void initMainPanel() {
        mMainPanel = new JPanel(new BorderLayout());
        var progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        mMainPanel.add(progressBar, BorderLayout.NORTH);
        var label = new JLabel(String.format("<html>%s<br/><br/><br/></html>", Dict.PATIENCE_IS_A_VIRTUE.toString()));
        label.setVerticalAlignment(SwingConstants.BOTTOM);
        label.setFont(label.getFont().deriveFont(label.getFont().getSize() * 2f));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        mMainPanel.add(label, BorderLayout.CENTER);
    }

    private double toGlobalZoom() {
        double altitude = mMap.getView().getEyePosition().getAltitude();
        int level = 0;
        for (int i = 0; i < mZoomLevels.length; i++) {
            if (altitude >= mZoomLevels[i]) {
                level = i;
                break;
            } else if (i == mZoomLevels.length - 1) {
                level = mZoomLevels.length;
            }
        }

        return level / (double) mZoomLevels.length;
    }

    private MLatLon toLatLon(Position p) {
        return new MLatLon(
                p.getLatitude().getDegrees(),
                p.getLongitude().getDegrees()
        );
    }

    private double toLocalZoom(double globalZoom) {
        return mZoomLevels[(int) ((mZoomLevels.length - 1) * globalZoom)];
    }

    private Position toPosition(MLatLon latLon) {
        Angle lat = Angle.fromDegreesLatitude(latLon.getLatitude());
        Angle lon = Angle.fromDegreesLongitude(latLon.getLongitude());

        return new Position(lat, lon, 0);
    }

    private Sector toSector(MLatLonBox latLonBox) {
        return new Sector(
                Angle.fromDegreesLatitude(latLonBox.getSouthWest().getLatitude()),
                Angle.fromDegreesLatitude(latLonBox.getNorthEast().getLatitude()),
                Angle.fromDegreesLongitude(latLonBox.getSouthWest().getLongitude()),
                Angle.fromDegreesLongitude(latLonBox.getNorthEast().getLongitude())
        );
    }

    private void updateToolbarDocumentInfo() {
        GlobalState globalState = Mapton.getGlobalState();
        String styleId = mOptions.get(KEY_MAP_STYLE, DEFAULT_MAP_STYLE);
        TreeMap<String, MAttribution> globalAttributions = globalState.get(MKey.DATA_SOURCES_WMS_ATTRIBUTIONS);
        LinkedHashMap<String, MAttribution> attributions = new LinkedHashMap<>();
        MapStyle mapStyle = MapStyle.getStyle(styleId);

        String[] layers = MapStyle.getLayers(styleId);
        if (layers != null) {
            for (String layer : layers) {
                if (globalAttributions.containsKey(layer)) {
                    attributions.put(layer, globalAttributions.get(layer));
                }
            }
        }

        MDocumentInfo documentInfo = new MDocumentInfo(mapStyle.getName(), attributions);

        globalState.put(MKey.MAP_DOCUMENT_INFO, documentInfo);
    }
}
