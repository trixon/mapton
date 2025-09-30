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
package org.mapton.butterfly_alarm;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.AlarmManager;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class AlarmLayerBundle extends BfLayerBundle {

    private final AlarmAttributeManager mAttributeManager = AlarmAttributeManager.getInstance();
    private final AlarmManager mManager = AlarmManager.getInstance();
    private final AlarmOptionsView mOptionsView;

    public AlarmLayerBundle() {
        init();
        initRepaint();
        mOptionsView = new AlarmOptionsView(this);

        initListeners();
        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        initCommons(Mapton.addWarning(Bundle.CTL_AlarmAction(), 1), "", "ActTopComponent");

        mLabelLayer.setEnabled(true);
        mLayer.setMaxActiveAltitude(6000);
        mPinLayer.setMaxActiveAltitude(10000);
        mLabelLayer.setMaxActiveAltitude(10000);
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BAlarm> c) -> {
            repaint();
        });

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = mLayer.isEnabled();
            mManager.updateTemporal(enabled);

            if (enabled) {
                repaint();
            }
        });

        mOptionsView.labelByProperty().addListener((p, o, n) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            if (!mLayer.isEnabled()) {
                return;
            }

            var pointBy = mOptionsView.getPointBy();
            switch (pointBy) {
                case NONE -> {
                    mPinLayer.setEnabled(false);
                }
                case PIN -> {
                    mPinLayer.setEnabled(true);
                }
                default ->
                    throw new AssertionError();
            }

            synchronized (mManager.getTimeFilteredItems()) {
                for (var alarm : mManager.getTimeFilteredItems()) {
                    var mapObjects = new ArrayList<AVListImpl>();
                    if (ObjectUtils.allNotNull(alarm.getLat(), alarm.getLon())) {
                        var position = Position.fromDegrees(alarm.getLat(), alarm.getLon());
                        var labelPlacemark = plotLabel(alarm, mOptionsView.getLabelBy(), position);

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(alarm, position, labelPlacemark));
                    }

//                    mapObjects.add(plotArea(area));
                    var leftClickRunnable = (Runnable) () -> {
                        mManager.setSelectedItemAfterReset(alarm);
                    };

                    var leftDoubleClickRunnable = (Runnable) () -> {
                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                    };

                    mapObjects.stream().filter(r -> r != null).forEach(r -> {
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                    });
                }
            }

            setDragEnabled(false);
        });
    }

//    private AVListImpl plotArea(BAreaActivity area) {
//        var attrs = mAttributeManager.getSurfaceAttributes(area);
//        var attrsHighlight = mAttributeManager.getSurfaceHighlightAttributes(area);
//        Renderable renderable = null;
//        try {
//            var geometry = area.getGeometry();
//            if (geometry instanceof LineString lineString) {
//                var surfaceObject = new SurfacePolyline(attrs, WWHelper.positionsFromGeometry(lineString, 0));
//                surfaceObject.setHighlightAttributes(attrsHighlight);
//                renderable = surfaceObject;
//                mLayer.addRenderable(renderable);
//            } else if (geometry instanceof Polygon polygon) {
//                var surfaceObject = new SurfacePolygon(attrs, WWHelper.positionsFromGeometry(polygon, 0));
//                surfaceObject.setHighlightAttributes(attrsHighlight);
//                renderable = surfaceObject;
//                mLayer.addRenderable(renderable);
//            }
//        } catch (Exception ex) {
//            Exceptions.printStackTrace(ex);
//        }
//
//        return (AVListImpl) renderable;
//    }
    private PointPlacemark plotLabel(BAlarm p, AlarmLabelBy labelBy, Position position) {
        if (labelBy == AlarmLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    private PointPlacemark plotPin(BAlarm area, Position position, PointPlacemark labelPlacemark) {
//        var attrs = mAttributeManager.getPinAttributes(area);

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
//        placemark.setAttributes(attrs);
//        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

        mPinLayer.addRenderable(placemark);
        if (labelPlacemark != null) {
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_ON, (Runnable) () -> {
                labelPlacemark.setHighlighted(true);
            });
            placemark.setValue(WWHelper.KEY_RUNNABLE_HOOVER_OFF, (Runnable) () -> {
                labelPlacemark.setHighlighted(false);
            });
        }

        return placemark;
    }

}
