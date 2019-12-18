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
package org.mapton.addon.mapollage_ww;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.commons.io.FilenameUtils;
import org.mapton.addon.photos_nb.api.Mapo;
import org.mapton.addon.photos_nb.api.MapoPhoto;
import org.mapton.addon.photos_nb.api.MapoSettings;
import org.mapton.addon.photos_nb.api.MapoSettings.SplitBy;
import org.mapton.addon.photos_nb.api.MapoSource;
import org.mapton.addon.photos_nb.api.MapoSourceManager;
import org.mapton.api.MKey;
import org.mapton.api.MTemporalManager;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageLayerBundle extends LayerBundle {

    private final SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss");
    private final IconLayer mIconLayer = new IconLayer();
    private final ArrayList<LineNode> mLineNodes = new ArrayList<>();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private final RenderableLayer mRenderableLayer = new RenderableLayer();
    private MapoSettings mSettings;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private HashMap<String, MTemporalRange> mTemporalRanges;

    public MapollageLayerBundle() {
        init();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mIconLayer);
        getLayers().add(mRenderableLayer);

        setPopulated(true);
    }

    private String getCatKey(String category, String value) {
        return String.format("%s#%s", category, value);
    }

    private String getPattern(SplitBy splitBy) {
        switch (splitBy) {
            case NONE:
                return "'NO_SPLIT'";
            case HOUR:
                return "yyyyMMddHH";
            case DAY:
                return "yyyyMMdd";
            case WEEK:
                return "yyyyww";
            case MONTH:
                return "yyyyMM";
            case YEAR:
                return "yyyy";
            default:
                return null;
        }
    }

    private void init() {
        mIconLayer.setName(Dict.PHOTOS.toString());
        setCategoryAddOns(mIconLayer);
        attachTopComponentToLayer("PhotosTopComponent", mIconLayer);

        mIconLayer.setEnabled(true);

        mRenderableLayer.setPickEnabled(false);
        mRenderableLayer.setName(String.format("Mapollage - %s", Dict.Geometry.PATHS.toString()));
        mRenderableLayer.setEnabled(true);

        setVisibleInLayerManager(mRenderableLayer, false);
        setName(Dict.PHOTOS.toString());
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

        mIconLayer.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("Enabled")) {
                mRenderableLayer.setEnabled(mIconLayer.isEnabled());
                if (mIconLayer.isEnabled()) {
                    refresh();
                    mTemporalManager.putAll(mTemporalRanges);
                } else {
                    mTemporalRanges = mTemporalManager.getAndRemoveSubSet(Mapo.KEY_TEMPORAL_PREFIX);
                }
            }
        });
    }

    private void plotTrack(BasicShapeAttributes attributes, ArrayList<Position> positions) {
        Path path = new Path(positions);
        path.setFollowTerrain(true);
        path.setAttributes(attributes);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        mRenderableLayer.addRenderable(path);
    }

    private void plotTracks() {
        BasicShapeAttributes trackAttributes = new BasicShapeAttributes();
        trackAttributes.setDrawOutline(true);
        trackAttributes.setOutlineOpacity(0.8);
        trackAttributes.setOutlineWidth(mSettings.getWidth());
        trackAttributes.setOutlineMaterial(new Material(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mSettings.getColorTrack()))));

        BasicShapeAttributes gapAttributes = (BasicShapeAttributes) trackAttributes.copy();
        gapAttributes.setOutlineMaterial(new Material(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mSettings.getColorGap()))));

        Collections.sort(mLineNodes, (LineNode o1, LineNode o2) -> o1.getDate().compareTo(o2.getDate()));
        SimpleDateFormat dateFormat = new SimpleDateFormat(getPattern(mSettings.getSplitBy()));

        TreeMap<String, ArrayList<LineNode>> periodLineNodeMap = new TreeMap<>();
        mLineNodes.forEach((node) -> {
            periodLineNodeMap.computeIfAbsent(dateFormat.format(node.getDate()), k -> new ArrayList<>()).add(node);
        });

        if (mSettings.isPlotTracks()) {
            //Add track
            for (ArrayList<LineNode> nodes : periodLineNodeMap.values()) {
                if (nodes.size() > 1) {
                    ArrayList<Position> positions = new ArrayList<>();

                    nodes.forEach((node) -> {
                        positions.add(Position.fromDegrees(node.getLat(), node.getLon()));
                    });

                    plotTrack(trackAttributes, positions);
                }
            }
        }

        if (mSettings.isPlotGaps()) {
            //Add gap
            ArrayList<LineNode> previousNodes = null;
            for (ArrayList<LineNode> nodes : periodLineNodeMap.values()) {
                if (previousNodes != null) {
                    LineNode prevLast = previousNodes.get(previousNodes.size() - 1);
                    LineNode currentFirst = nodes.get(0);

                    ArrayList<Position> positions = new ArrayList<>();
                    positions.add(Position.fromDegrees(prevLast.getLat(), prevLast.getLon()));
                    positions.add(Position.fromDegrees(currentFirst.getLat(), currentFirst.getLon()));
                    plotTrack(gapAttributes, positions);
                }

                previousNodes = nodes;
            }
        }
    }

    private void refresh() {
        if (!mIconLayer.isEnabled()) {
            return;
        }

        mIconLayer.removeAllIcons();
        mRenderableLayer.removeAllRenderables();
        mLineNodes.clear();

        for (MapoSource source : mManager.getItems()) {
            if (source.isVisible()) {
                for (MapoPhoto photo : source.getCollection().getPhotos()) {
                    boolean validDate;

                    try {
                        validDate = mTemporalManager.isValid(photo.getDate());
                    } catch (NullPointerException e) {
                        continue;
                    }

                    if (validDate) {
                        String absolutePath = new File(source.getThumbnailDir(), String.format("%s.jpg", photo.getChecksum())).getAbsolutePath();
                        UserFacingIcon icon = new UserFacingIcon(absolutePath, Position.fromDegrees(photo.getLat(), photo.getLon()));
                        int downSample = 10;
                        icon.setSize(new Dimension(photo.getWidth() / downSample, photo.getHeight() / downSample));
                        icon.setHighlightScale(downSample);

                        icon.setValue(WWHelper.KEY_RUNNABLE_HOOVER, (Runnable) () -> {
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

                        icon.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                            File f = new File(photo.getPath());
                            if (f.isFile()) {
                                SystemHelper.desktopOpen(new File(photo.getPath()));
                            } else {
                                NbMessage.error(Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), String.format(Dict.Dialog.MESSAGE_FILE_NOT_FOUND.toString(), photo.getPath()));
                            }
                        });

                        mLineNodes.add(new LineNode(photo.getDate(), photo.getLat(), photo.getLon()));

                        mIconLayer.addIcon(icon);
                    }
                }
            }
        }

        if (mLineNodes.size() > 1) {
            plotTracks();
        }

        LayerBundleManager.getInstance().redraw();
    }

}
