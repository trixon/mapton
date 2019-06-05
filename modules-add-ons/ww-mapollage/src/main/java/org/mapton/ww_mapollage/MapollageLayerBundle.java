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
package org.mapton.ww_mapollage;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.Map;
import org.apache.commons.io.FilenameUtils;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoPhoto;
import org.mapton.mapollage.api.MapoSettings;
import org.mapton.mapollage.api.MapoSource;
import org.mapton.mapollage.api.MapoSourceManager;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.mapton.worldwind.api.WWUtil;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageLayerBundle extends LayerBundle {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    private final IconLayer mLayer = new IconLayer();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private MapoSettings mSettings;

    public MapollageLayerBundle() {
        init();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);

        setPopulated(true);
    }

    private String getCatKey(String category, String value) {
        return String.format("%s#%s", category, value);
    }

    private void init() {
        mLayer.setName("Mapollage");
        mLayer.setEnabled(true);
        setName("Mapollage");
    }

    private void initListeners() {
        GlobalState globalState = Mapton.getGlobalState();
        globalState.addListener((GlobalStateChangeEvent evt) -> {
            refresh();
        }, Mapo.KEY_MAPO);

        globalState.addListener((GlobalStateChangeEvent evt) -> {
            mSettings = evt.getValue();
            refresh();
        }, Mapo.KEY_SETTINGS_UPDATED);

        mLayer.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("Enabled") && mLayer.isEnabled()) {
                refresh();
            }
        });
    }

    private void plotPaths() {
        System.out.println("doPlot");
    }

    private void refresh() {
        if (!mLayer.isEnabled()) {
            return;
        }

        mLayer.removeAllIcons();
        if (mSettings.isPlotPaths()) {
            plotPaths();
        }

        for (MapoSource source : mManager.getItems()) {
            if (source.isVisible()) {
                for (MapoPhoto photo : source.getCollection().getPhotos()) {
                    LocalDate localDate = photo.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                    boolean validDate = mSettings.getLowDate().compareTo(localDate) * localDate.compareTo(mSettings.getHighDate()) >= 0;

                    if (validDate) {
                        String absolutePath = new File(source.getThumbnailDir(), String.format("%s.jpg", photo.getChecksum())).getAbsolutePath();
                        UserFacingIcon icon = new UserFacingIcon(absolutePath, Position.fromDegrees(photo.getLat(), photo.getLon()));
                        int downSample = 10;
                        icon.setSize(new Dimension(photo.getWidth() / downSample, photo.getHeight() / downSample));
                        icon.setHighlightScale(downSample);

                        icon.setValue(WWUtil.KEY_RUNNABLE_HOOVER, (Runnable) () -> {
                            Map<String, Object> propertyMap = new LinkedHashMap<>();
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.NAME.toString()), FilenameUtils.getBaseName(photo.getPath()));
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.DATE.toString()), mDateFormat.format(photo.getDate()));
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.PATH.toString()), photo.getPath());
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.LATITUDE.toString()), photo.getLat());
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.LONGITUDE.toString()), photo.getLon());
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.ALTITUDE.toString()), photo.getAltitude());
                            propertyMap.put(getCatKey(Dict.PHOTO.toString(), Dict.BEARING.toString()), photo.getBearing());
                            propertyMap.put(getCatKey(Dict.SOURCE.toString(), Dict.SOURCE.toString()), source.getName());
                            propertyMap.put(getCatKey(Dict.SOURCE.toString(), Dict.DESCRIPTION.toString()), source.getDescriptionString());
                            propertyMap.put(getCatKey(Dict.SOURCE.toString(), Dict.CACHE.toString()), source.getThumbnailDir().getAbsolutePath());

                            Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyMap);
                        });

                        icon.setValue(WWUtil.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                            File f = new File(photo.getPath());
                            if (f.isFile()) {
                                SystemHelper.desktopOpen(new File(photo.getPath()));
                            } else {
                                NbMessage.error(Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), String.format(Dict.Dialog.MESSAGE_FILE_NOT_FOUND.toString(), photo.getPath()));
                            }
                        });

                        mLayer.addIcon(icon);
                    }
                }
            }
        }

        LayerBundleManager.getInstance().redraw();
    }

}
