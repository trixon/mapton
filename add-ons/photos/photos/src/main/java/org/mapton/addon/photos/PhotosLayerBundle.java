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
package org.mapton.addon.photos;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.IconLayer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.UserFacingIcon;
import java.awt.Dimension;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import javafx.scene.Node;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.mapton.addon.photos.api.Mapo;
import org.mapton.addon.photos.api.MapoSettings;
import org.mapton.addon.photos.api.MapoSettings.SplitBy;
import org.mapton.addon.photos.api.MapoSourceManager;
import org.mapton.addon.photos.ui.OptionsView;
import org.mapton.api.MKey;
import org.mapton.api.MTemporalManager;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class PhotosLayerBundle extends LayerBundle {

    private final FastDateFormat mDateFormat = FastDateFormat.getInstance("yyyy-MM-dd HH.mm.ss");
    private final IconLayer mLayer = new IconLayer();
    private final ArrayList<LineNode> mLineNodes = new ArrayList<>();
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private OptionsView mOptionsView;
    private final RenderableLayer mRenderableLayer = new RenderableLayer();
    private MapoSettings mSettings;
    private final MTemporalManager mTemporalManager = MTemporalManager.getInstance();
    private ConcurrentHashMap<String, MTemporalRange> mTemporalRanges;

    public PhotosLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = new OptionsView();
        }

        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer, mRenderableLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private String getCatKey(String category, String value) {
        return "%s#%s".formatted(category, value);
    }

    private String getPattern(SplitBy splitBy) {
        return switch (splitBy) {
            case NONE ->
                "'NO_SPLIT'";
            case HOUR ->
                "yyyyMMddHH";
            case DAY ->
                "yyyyMMdd";
            case WEEK ->
                "yyyyww";
            case MONTH ->
                "yyyyMM";
            case YEAR ->
                "yyyy";
            default ->
                null;
        };
    }

    private void init() {
        mLayer.setName(Dict.PHOTOS.toString());
        setCategoryAddOns(mLayer);
        attachTopComponentToLayer("PhotosTopComponent", mLayer);

        mLayer.setEnabled(true);

        mRenderableLayer.setPickEnabled(false);
        mRenderableLayer.setEnabled(true);

        setName(Dict.PHOTOS.toString());

        setParentLayer(mLayer);
        setAllChildLayers(mRenderableLayer);
    }

    private void initListeners() {
        var globalState = Mapton.getGlobalState();
        globalState.addListener(gsce -> {
            repaint();
        }, Mapo.KEY_MAPO);

        globalState.addListener(gsce -> {
            mSettings = gsce.getValue();
            repaint();
        }, Mapo.KEY_SETTINGS_UPDATED);

        mLayer.addPropertyChangeListener("Enabled", pce -> {
            if (mLayer.isEnabled()) {
                repaint();
                mTemporalManager.putAll(mTemporalRanges);
            } else {
                mTemporalRanges = mTemporalManager.getAndRemoveSubSet(Mapo.KEY_TEMPORAL_PREFIX);
            }
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            if (!mLayer.isEnabled() || mSettings == null) {
                return;
            }

            removeAllIcons();
            removeAllRenderables();
            mLineNodes.clear();

            for (var source : mManager.getItems()) {
                if (source.isVisible()) {
                    for (var photo : source.getCollection().getPhotos()) {
                        boolean validDate;

                        try {
                            validDate = mTemporalManager.isValid(photo.getDate());
                        } catch (NullPointerException e) {
                            continue;
                        }

                        if (validDate) {
                            String absolutePath = new File(source.getThumbnailDir(), "%s.jpg".formatted(photo.getChecksum())).getAbsolutePath();
                            var userFacingIcon = new UserFacingIcon(absolutePath, Position.fromDegrees(photo.getLat(), photo.getLon()));
                            int downSample = 10;
                            userFacingIcon.setSize(new Dimension(photo.getWidth() / downSample, photo.getHeight() / downSample));
                            userFacingIcon.setHighlightScale(downSample);

                            userFacingIcon.setValue(WWHelper.KEY_RUNNABLE_HOOVER, (Runnable) () -> {
                                var propertyMap = new LinkedHashMap<String, Object>();
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

                            userFacingIcon.setValue(WWHelper.KEY_RUNNABLE_LEFT_DOUBLE_CLICK, (Runnable) () -> {
                                var file = new File(photo.getPath());
                                if (file.isFile()) {
                                    SystemHelper.desktopOpen(new File(photo.getPath()));
                                } else {
                                    NbMessage.error(Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), Dict.Dialog.MESSAGE_FILE_NOT_FOUND.toString().formatted(photo.getPath()));
                                }
                            });

                            mLineNodes.add(new LineNode(photo.getDate(), photo.getLat(), photo.getLon()));
                            mLayer.addIcon(userFacingIcon);
                        }
                    }
                }
            }

            if (mLineNodes.size() > 1) {
                plotTracks();
            }

            setDragEnabled(false);
        });
    }

    private void plotTrack(BasicShapeAttributes attributes, ArrayList<Position> positions) {
        var path = new Path(positions);
        path.setFollowTerrain(true);
        path.setAttributes(attributes);
        path.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        mRenderableLayer.addRenderable(path);
    }

    private void plotTracks() {
        var trackAttributes = new BasicShapeAttributes();
        trackAttributes.setDrawOutline(true);
        trackAttributes.setOutlineOpacity(0.8);
        trackAttributes.setOutlineWidth(mSettings.getWidth());
        trackAttributes.setOutlineMaterial(new Material(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mSettings.getColorTrack()))));

        var gapAttributes = (BasicShapeAttributes) trackAttributes.copy();
        gapAttributes.setOutlineMaterial(new Material(FxHelper.colorToColor(FxHelper.colorFromHexRGBA(mSettings.getColorGap()))));

        Collections.sort(mLineNodes, Comparator.comparing(LineNode::getDate));
        var dateFormat = FastDateFormat.getInstance(getPattern(mSettings.getSplitBy()));
        var periodLineNodeMap = new TreeMap<String, ArrayList<LineNode>>();

        mLineNodes.forEach(node -> {
            periodLineNodeMap.computeIfAbsent(dateFormat.format(node.getDate()), k -> new ArrayList<>()).add(node);
        });

        if (mSettings.isPlotTracks()) {
            //Add track
            for (var nodes : periodLineNodeMap.values()) {
                if (nodes.size() > 1) {
                    var positions = new ArrayList<Position>();

                    nodes.forEach(node -> {
                        positions.add(Position.fromDegrees(node.getLat(), node.getLon()));
                    });

                    plotTrack(trackAttributes, positions);
                }
            }
        }

        if (mSettings.isPlotGaps()) {
            //Add gap
            ArrayList<LineNode> previousNodes = null;
            for (var nodes : periodLineNodeMap.values()) {
                if (previousNodes != null) {
                    var prevLast = previousNodes.get(previousNodes.size() - 1);
                    var currentFirst = nodes.get(0);
                    var positions = new ArrayList<Position>();

                    positions.add(Position.fromDegrees(prevLast.getLat(), prevLast.getLon()));
                    positions.add(Position.fromDegrees(currentFirst.getLat(), currentFirst.getLon()));
                    plotTrack(gapAttributes, positions);
                }

                previousNodes = nodes;
            }
        }
    }
}
