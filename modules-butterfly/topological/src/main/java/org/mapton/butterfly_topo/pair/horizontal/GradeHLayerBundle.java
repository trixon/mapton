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
package org.mapton.butterfly_topo.pair.horizontal;

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
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import org.mapton.butterfly_topo.TopoBaseLayerBundle;
import org.mapton.butterfly_topo.TopoLabelBy;
import org.mapton.butterfly_topo.TopoLayerBundle;
import org.mapton.butterfly_topo.pair.GradeAttributeManager;
import org.mapton.butterfly_topo.pair.PairManagerBase;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GradeHLayerBundle extends TopoBaseLayerBundle {

    private final GradeAttributeManager mAttributeManager = GradeAttributeManager.getInstance();

    private final ResourceBundle mBundle = NbBundle.getBundle(PairManagerBase.class);
    private final Pair1Manager mManager = Pair1Manager.getInstance();
    private Pair1OptionsView mOptionsView;

    public GradeHLayerBundle() {
        init();
        initRepaint();
//        mOptionsView = new TopoOptionsView(this);
//        mGraphicRenderer = new GraphicRenderer(mLayer, mOptionsView.getComponentCheckModel());
        initListeners();
//        mAttributeManager.setColorBy(mOptionsView.getColorBy());

        mManager.setInitialTemporalState(WWHelper.isStoredAsVisible(mLayer, mLayer.isEnabled()));
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = (Pair1OptionsView) TabOptionsViewProvider.getInstance(Pair1OptionsView.class);
            if (mOptionsView != null) {
                mOptionsView.setLayerBundle(this);
            }
        }

        return mOptionsView;
    }

    public PointPlacemark plotLabel(BTopoPointPair p, TopoLabelBy labelBy, Position position) {
//        if (labelBy == TopoLabelBy.NONE) {
//            return null;
//        }

        String label;
        try {
//            label = mOptionsView.getLabelBy().getLabel(p);
            label = p.getName();
        } catch (Exception e) {
            label = "ERROR %s <<<<<<<<".formatted(p.getName());
        }

        var placemark = new PointPlacemark(position);
        placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText(label);
        mLabelLayer.addRenderable(placemark);

        return placemark;
    }

    public PointPlacemark plotPin(BTopoPointPair p, Position position, PointPlacemark labelPlacemark) {
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
        getLayers().addAll(mLayer, mLabelLayer, mSymbolLayer, mPinLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName(mBundle.getString("tilt_h"));
        setCategory(mLayer, SDict.TOPOGRAPHY.toString());
        setName(mBundle.getString("tilt_h"));
        attachTopComponentToLayer("TopoTopComponent", mLayer);
        mLabelLayer.setMaxActiveAltitude(2000);
        setParentLayer(mLayer);
        setAllChildLayers(mLabelLayer, mSymbolLayer, mPinLayer);
        mLayer.setPickEnabled(true);

        mLayer.setEnabled(false);
        setVisibleInLayerManager(mLayer, false);
        connectToOtherBundle(TopoLayerBundle.class, Pair1OptionsView.class.getName());
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoPointPair> c) -> {
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
        }, Pair1OptionsView.class.getName());
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();

            if (!mLayer.isEnabled()) {
                return;
            }
            mManager.getTimeFilteredItems().stream()
                    .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                    .forEachOrdered(p -> {
                        var position = Position.fromDegrees(p.getLat(), p.getLon());
                        var labelPlacemark = plotLabel(p, null, position);
                        var mapObjects = new ArrayList<AVListImpl>();

                        mapObjects.add(labelPlacemark);
                        mapObjects.add(plotPin(p, position, labelPlacemark));
                    });

//            for (var p : new ArrayList<>(mManager.getTimeFilteredItems())) {
//                if (ObjectUtils.allNotNull(p.getLat(), p.getLon())) {
//                    var position = Position.fromDegrees(p.getLat(), p.getLon());
//                    var labelPlacemark = plotLabel(p, mOptionsView.getLabelBy(), position);
//                    var mapObjects = new ArrayList<AVListImpl>();
//
//                    mapObjects.add(labelPlacemark);
//                    mapObjects.add(plotPin(p, position, labelPlacemark));
//                    mapObjects.addAll(plotSymbol(p, position, labelPlacemark));
//                    mapObjects.addAll(plotIndicators(p, position));
//
//                    mGraphicRenderer.plot(p, position, mapObjects);
//
//                    var leftClickRunnable = (Runnable) () -> {
//                        mManager.setSelectedItemAfterReset(p);
//                    };
//
//                    var leftDoubleClickRunnable = (Runnable) () -> {
//                        Almond.openAndActivateTopComponent((String) mLayer.getValue(WWHelper.KEY_FAST_OPEN));
//                        mGraphicRenderer.addToAllowList(p.getName());
//                        repaint();
//                    };
//
//                    mapObjects.stream().filter(r -> r != null).forEach(r -> {
//                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
//                        r.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, leftDoubleClickRunnable);
//                    });
//                }
//            }
            setDragEnabled(false);
        });
    }

}
