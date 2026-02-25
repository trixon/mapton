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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.render.Renderable;
import java.net.URLEncoder;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Callable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MAnnotation;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.MSearchProviderManager;
import org.mapton.api.MTemporalManager;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.butterfly_format.types.BBasePoint;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class BaseManager<T extends BBase> extends MBaseDataManager<T> {

    private static BBase sCurrItem;
    private static BaseManager< BBase> sCurrManager;
    private static BBase sPrevItem;
    private static BaseManager< BBase> sPrevManager;
    protected boolean mFirstLoad = true;
    protected int mTrendLoadCounter = 0;
    protected OffsetManager mOffsetManager = OffsetManager.getInstance();
    private Butterfly mButterfly;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();
    private final BooleanProperty mDisabledSearchProperty = new SimpleBooleanProperty(true);

    static {
        Mapton.getGlobalState().addListener(gsce -> {
            if (ObjectUtils.allNotNull(getCurrManager(), getCurrItem())) {
                getCurrManager().setSelectedItemAfterReset(getCurrItem());
            }
        }, MKey.OBJECT_RESELECT);
    }

    public static BBase getCurrItem() {
        return sCurrItem;
    }

    public static BaseManager<BBase> getCurrManager() {
        return sCurrManager;
    }

    public static void restoreManagerAndItem() {
        if (ObjectUtils.allNotNull(sPrevManager, sPrevItem)) {
            sPrevManager.setSelectedItem(null);
            sPrevManager.setSelectedItem(sPrevItem);
        }
    }

    public BaseManager(Class<T> typeParameterClass) {
        super(typeParameterClass);

        mButterflyManager.butterflyProperty().addListener((p, o, n) -> {
            mButterfly = n;
            FxHelper.runLater(() -> load(n));
        });

        if (mButterflyManager.getButterfly() != null) {
            mButterfly = mButterflyManager.getButterfly();
            FxHelper.runLater(() -> load(mButterfly));
        }

        selectedItemProperty().addListener((p, o, n) -> {
            var disabled = n == null || StringUtils.isAnyBlank(n.getExternalSysId(), n.getExternalSysKey());
            mDisabledSearchProperty.setValue(disabled);
            storeManagerAndItem(n);
            var objectMeasurements = getObjectMeasurements(n);
            if (objectMeasurements != Boolean.FALSE) {
                Mapton.getGlobalState().put(BKey.OBJECT_MEASUREMENTS, objectMeasurements);
            }
        });

        var drr = new DelayedResetRunner(300, () -> {
            //TODO This probably needs some improvements
            if (getCurrManager() == this && getSelectedItem() != null) {
                var selectedItem = getSelectedItem();
                FxHelper.runLaterDelayed(500, () -> {
                    selectedItemProperty().set(null);
                    FxHelper.runLaterDelayed(10, () -> {
                        selectedItemProperty().set(selectedItem);
                    });
                });
            }
        });

        ChangeListener<LocalDate> temporalChangeListener = (p, o, n) -> {
            drr.reset();
        };

        MTemporalManager.getInstance().lowDateProperty().addListener(temporalChangeListener);
        MTemporalManager.getInstance().highDateProperty().addListener(temporalChangeListener);
    }

    public BooleanProperty disabledSearchProperty() {
        return mDisabledSearchProperty;
    }

    public void displayAnnotation(T t) {
        if (t instanceof BBasePoint p) {
            var annotationList = getObjectAnnotation(t);
            if (annotationList != null && !annotationList.isEmpty()) {
                var sb = new StringBuilder();
                for (int i = 0; i < annotationList.size(); i++) {
                    var s = annotationList.get(i);
                    if (i == 0) {
                        sb.append("<b>").append(s).append("</b><br />");
                    } else {
                        sb.append(s).append("<br />");
                    }

                }
                var latLon = new MLatLon(p.getLat(), p.getLon());
                var annotation = new MAnnotation(latLon, sb.toString(), getTypeParameterClass());
                Mapton.getGlobalState().put(MKey.ANNOTATIONS, annotation);
            }
        }
    }

    public Callable<List<String>> getCopyNamesCallable() {
        return () -> {
            return getTimeFilteredItems().stream().map(p -> p.getName()).toList();
        };
    }

    public List<String> getDefaultAnnotation(BOptionsBase options, T t) {
        if (t instanceof BXyzPoint p && options.isPlotAnnotation()) {
            var ext = p.extOrNull();
            return List.of(p.getName(),
                    ext.getDateLatest() != null ? ext.getDateLatest().toLocalDate().toString() : "-",
                    ext.getAlarmPercentString(ext),
                    ext.deltaZero().getDelta(3)
            );
        } else {
            return null;
        }
    }

    public Callable<String> getExternalSysUrlCallable() {
        return () -> {
            return MSearchProviderManager.getInstance().getUrl(getSelectedItem().getExternalSysId(), URLEncoder.encode(getSelectedItem().getExternalSysKey(), "UTF-8"));
        };
    }

    @Override
    public MLatLon getLatLonForItem(T t) {
        if (t instanceof BBasePoint cp) {
            try {
                return new MLatLon(cp.getLat(), cp.getLon());
            } catch (NullPointerException e) {
                //
            }
        }

        return null;
    }

    @Override
    public Object getMapIndicator(T t) {
        if (t instanceof BBasePoint) {
            try {
                var latLon = getLatLonForItem(t);
                return new Renderable[]{WWHelper.createIndicator(latLon), WWHelper.createIndicatorPole(latLon)};
            } catch (NullPointerException e) {
                //
            }
        }

        return null;
    }

    public List<String> getObjectAnnotation(T t) {
        return null;
    }

    public Object getObjectMeasurements(T t) {
        return null;
    }

    public void initObjectToItemMap() {
        synchronized (getAllItems()) {
            for (var item : getAllItems()) {
                getAllItemsMap().put(item.getName(), item);
            }
        }
    }

    public abstract void load(Butterfly butterfly);

    public void registerLayerBundle(LayerBundle layerBundle, BOptionsView optionsView) {
        selectedItemProperty().addListener((p, o, n) -> {
            if (optionsView.isPlotSelected()) {
                layerBundle.repaint();
            }
        });

        getTimeFilteredItems().addListener((ListChangeListener.Change<? extends T> c) -> {
            layerBundle.repaint();
        });

        layerBundle.getParentLayer().addPropertyChangeListener("Enabled", pce -> {
            boolean enabled = layerBundle.getParentLayer().isEnabled();
            updateTemporal(enabled);

            if (enabled) {
                layerBundle.repaint();
            }
        });
    }

    public void setSelectedItemAfterReset(T item, boolean fromMap) {
        super.setSelectedItem(item);
        displayAnnotation(item);
    }

    @Override
    protected MLatLonBox getTimeFilteredExtents() {
        var latLons = getTimeFilteredItems().stream()
                .filter(p -> p instanceof BBasePoint)
                .map(p -> (BBasePoint) p)
                .filter(p -> ObjectUtils.allNotNull(p.getLat(), p.getLon()))
                .map(p -> new MLatLon(p.getLat(), p.getLon()))
                .toList();

        return new MLatLonBox(latLons);
    }

    private void storeManagerAndItem(BBase item) {
        if (ObjectUtils.allNotNull(item) && item != sCurrItem) {
            sPrevManager = sCurrManager;
            sPrevItem = sCurrItem;
            sCurrManager = (BaseManager<BBase>) this;
            sCurrItem = item;
        }

    }
}
