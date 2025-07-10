/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade.horizontal;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.PointPlacemark;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.Mapton;
import org.mapton.api.ui.forms.TabOptionsViewProvider;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.TopoBaseLayerBundle;
import org.mapton.butterfly_topo.TopoLayerBundle;
import org.mapton.butterfly_topo.grade.GradeAttributeManager;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.butterfly_topo.grade.horizontal.graphic.GraphicRenderer;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GradeHLayerBundle extends TopoBaseLayerBundle {

    private static final int PLOT_LIMIT = 10000;
    private final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();
    private final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    private final GraphicRenderer mGraphicRenderer;
    private final GradeHManager mManager = GradeHManager.getInstance();
    private GradeHOptionsView mOptionsView;

    public GradeHLayerBundle() {
        init();
        initRepaint();
        getOptionsView();
        mGraphicRenderer = new GraphicRenderer(mLayer, mPassiveLayer, mOptionsView.getComponentCheckModel());
        initListeners();
//        mAttributeManager.setColorBy(mOptionsView.getColorBy());

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = (GradeHOptionsView) TabOptionsViewProvider.getInstance(GradeHOptionsView.class);
            if (mOptionsView != null) {
                mOptionsView.setLayerBundle(this);
            }
        }

        return mOptionsView;
    }

    public PointPlacemark plotLabel(BTopoGrade p, GradeHLabelBy labelBy, Position position) {
        if (labelBy == GradeHLabelBy.NONE) {
            return null;
        } else {
            var label = labelBy.getLabel(p);
            p.setValue(BKey.PIN_NAME, label);
            var placemark = createPlacemark(position, label, mAttributeManager.getLabelPlacemarkAttributes(), mLabelLayer);

            return placemark;
        }
    }

    public PointPlacemark plotPin(BTopoGrade p, Position position, PointPlacemark labelPlacemark) {
        var attrs = mAttributeManager.getPinAttributes(p);

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(attrs);
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(attrs, 1.5));

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

    @Override
    public void populate() throws Exception {
        super.populate();
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        initCommons(mBundle.getString("grade_h"), SDict.TOPOGRAPHY.toString(), "TopoTopComponent");

        mLabelLayer.setMaxActiveAltitude(2000);
//        mMuteLayer.setPickEnabled(false);

        setVisibleInLayerManager(mLayer, false);
        connectToOtherBundle(TopoLayerBundle.class, GradeHOptionsView.class.getName());
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoGrade> c) -> {
            repaint();
        });

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = mLayer.isEnabled();
            mManager.updateTemporal(enabled);

            if (enabled) {
                repaint();
            }
        });

        Mapton.getGlobalState().addListener(gsce -> {
            mLayer.setEnabled(gsce.getValue());
            repaint();
        }, GradeHOptionsView.class.getName());

        ((GradeHOptionsView) getOptionsView()).labelByProperty().addListener((p, o, n) -> {
            repaint();
        });

        mOptionsView.plotPointProperty().addListener((p, o, n) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            mGraphicRenderer.reset();

            if (!mLayer.isEnabled()) {
                return;
            }

            var pointBy = mOptionsView.getPointBy();
            switch (pointBy) {
                case NONE -> {
                    mPinLayer.setEnabled(false);
                    mSymbolLayer.setEnabled(false);
                }
                case PIN -> {
                    mSymbolLayer.setEnabled(false);
                    mPinLayer.setEnabled(true);
                    mPinLayer.setMinActiveAltitude(Double.MIN_VALUE);
                    mPinLayer.setMaxActiveAltitude(Double.MAX_VALUE);
                }
                default ->
                    throw new AssertionError();
            }

            synchronized (mManager.getTimeFilteredItems()) {
                mManager.getTimeFilteredItems().stream()
                        .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                        .limit(PLOT_LIMIT)
                        .forEachOrdered(p -> {
                            var position = Position.fromDegrees(p.getLat(), p.getLon());
                            var labelPlacemark = plotLabel(p, null, position);
                            var mapObjects = new ArrayList<AVListImpl>();

                            mapObjects.add(labelPlacemark);
                            mapObjects.add(plotPin(p, position, labelPlacemark));
//                    mapObjects.addAll(plotSymbol(p, position, labelPlacemark));
                            mGraphicRenderer.plot(p, position, mapObjects);

                            var leftClickRunnable = (Runnable) () -> {
                                mManager.setSelectedItemAfterReset(p);
                            };

                            var leftDoubleClickRunnable = (Runnable) () -> {
                                Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
                                mGraphicRenderer.addToAllowList(p);
                                repaint();
                            };

                            mapObjects.stream().filter(r -> r != null).forEach(r -> {
                                r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
                                r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
                            });
                        });
            }

            setDragEnabled(false);
        });
    }

}
