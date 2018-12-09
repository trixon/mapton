/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_grid;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MCooTrans;
import org.mapton.api.MDict;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import static org.mapton.ww_grid.Options.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class GridController extends LayerBundle {

    private int mAltitudeMode;
    private BasicShapeAttributes mEquatorAttributes;
    private final BasicShapeAttributes mGridAttributes = new BasicShapeAttributes();
    private final RenderableLayer mLayer = new RenderableLayer();
    private BasicShapeAttributes mLocalGridAttributes;
    private final LocalGridManager mManager = LocalGridManager.getInstance();
    private final Options mOptions = Options.getInstance();
    private BasicShapeAttributes mPolarAttributes;
    private BasicShapeAttributes mTropicAttributes;

    public GridController() {
        mLayer.setName(MDict.GRID.toString());
        init();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        initAttributes();
        refresh();

        mOptions.getPreferences().addPreferenceChangeListener((event) -> {
            refresh();
        });
    }

    private void draw(Position pos1, Position pos2, BasicShapeAttributes attributes) {
        Path path = new Path(pos1, pos2);
        path.setAttributes(attributes);
        path.setFollowTerrain(true);
        path.setAltitudeMode(mAltitudeMode);

        mLayer.addRenderable(path);
    }

    private void drawLatitude(double latitude, BasicShapeAttributes attributes) {
        Position pos0 = Position.fromDegrees(latitude, 0);
        Position pos1 = Position.fromDegrees(latitude, 180);
        Position pos2 = Position.fromDegrees(latitude, -180);

        draw(pos0, pos1, attributes);
        draw(pos0, pos2, attributes);
    }

    private void drawLongitude(double longitude, BasicShapeAttributes attributes) {
        Position pos0 = Position.fromDegrees(0, longitude);
        Position pos1 = Position.fromDegrees(90, longitude);
        Position pos2 = Position.fromDegrees(-90, longitude);

        draw(pos0, pos1, attributes);
        draw(pos0, pos2, attributes);
    }

    private void init() {
    }

    private void initAttributes() {
        mGridAttributes.setOutlineMaterial(Material.CYAN);
        mGridAttributes.setOutlineWidth(0.7);
        mGridAttributes.setDrawOutline(true);
        mGridAttributes.setOutlineOpacity(0.5);

        mEquatorAttributes = (BasicShapeAttributes) mGridAttributes.copy();
        mEquatorAttributes.setOutlineMaterial(Material.RED);
        mEquatorAttributes.setOutlineWidth(3.0);

        mTropicAttributes = (BasicShapeAttributes) mGridAttributes.copy();
        mTropicAttributes.setOutlineMaterial(Material.ORANGE);
        mTropicAttributes.setOutlineWidth(2.0);

        mPolarAttributes = (BasicShapeAttributes) mGridAttributes.copy();
        mPolarAttributes.setOutlineMaterial(Material.BLUE);
        mPolarAttributes.setOutlineWidth(2.0);

        mLocalGridAttributes = (BasicShapeAttributes) mGridAttributes.copy();
        mLocalGridAttributes.setOutlineMaterial(Material.YELLOW);
    }

    private void plotGlobal() {
//        System.out.println("PLOT GLOBAL");

        if (mOptions.is(KEY_GLOBAL_LATITUDES)) {
            for (int i = -90; i < 90; i += 15) {
                drawLatitude(i, mGridAttributes);
            }
        }

        if (mOptions.is(KEY_GLOBAL_LONGITUDES)) {
            for (int i = -180; i < 180; i += 15) {
                drawLongitude(i, i == 0 ? mEquatorAttributes : mGridAttributes);
            }
        }

        final double POLAR = 66.563167; //As of 2018-11-23
        final double TROPIC = 23.43683; //As of 2018-11-23

        if (mOptions.is(KEY_GLOBAL_POLAR_ARCTIC)) {
            drawLatitude(POLAR, mPolarAttributes);
        }

        if (mOptions.is(KEY_GLOBAL_TROPIC_CANCER)) {
            drawLatitude(TROPIC, mTropicAttributes);
        }

        if (mOptions.is(KEY_GLOBAL_EQUATOR)) {
            drawLatitude(0.0, mEquatorAttributes);
        }

        if (mOptions.is(KEY_GLOBAL_TROPIC_CAPRICORN)) {
            drawLatitude(-TROPIC, mTropicAttributes);
        }

        if (mOptions.is(KEY_GLOBAL_POLAR_ANTARCTIC)) {
            drawLatitude(-POLAR, mPolarAttributes);
        }
    }

    private void plotLocal() {
        System.out.println("PLOT LOCAL");
        ObservableList<LocalGrid> grids = mManager.getItems();
        if (grids != null) {
            for (LocalGrid grid : grids) {
                if (grid.isChecked()) {
                    plotLocal(grid);
                }
            }
            System.out.println("");
        }
    }

    private void plotLocal(LocalGrid grid) {
        System.out.println(grid.getName());
        System.out.println(ToStringBuilder.reflectionToString(grid, ToStringStyle.MULTI_LINE_STYLE));
        BasicShapeAttributes shapeAttributes = (BasicShapeAttributes) mLocalGridAttributes.copy();
        shapeAttributes.setOutlineWidth(grid.getLineWidth());
        MCooTrans cooTrans = MCooTrans.getCooTrans(grid.getCooTrans());

        double latMin = grid.getLatStart();
        double latMax = grid.getLatStart() + grid.getLatStep() * grid.getLatCount();
        double lonMin = grid.getLonStart();
        double lonMax = grid.getLonStart() + grid.getLonStep() * grid.getLonCount();

        for (int lonCount = 0; lonCount < grid.getLonCount() + 1; lonCount++) {
            double lon = grid.getLonStart() + lonCount * grid.getLonStep();
            Point2D p1 = cooTrans.toWgs84(latMin, lon);
            Point2D p2 = cooTrans.toWgs84(latMax, lon);

            Position pos1 = Position.fromDegrees(p1.getY(), p1.getX());
            Position pos2 = Position.fromDegrees(p2.getY(), p2.getX());

            draw(pos1, pos2, shapeAttributes);
        }

        for (int latCount = 0; latCount < grid.getLatCount() + 1; latCount++) {
            double lat = grid.getLatStart() + latCount * grid.getLatStep();
            Point2D p1 = cooTrans.toWgs84(lat, lonMin);
            Point2D p2 = cooTrans.toWgs84(lat, lonMax);

            Position pos1 = Position.fromDegrees(p1.getY(), p1.getX());
            Position pos2 = Position.fromDegrees(p2.getY(), p2.getX());

            draw(pos1, pos2, shapeAttributes);
        }
    }

    private void refresh() {
        mLayer.removeAllRenderables();
        mAltitudeMode = mOptions.is(KEY_GLOBAL_CLAMP_TO_GROUND) ? WorldWind.CLAMP_TO_GROUND : WorldWind.ABSOLUTE;

        if (mOptions.is(KEY_GLOBAL_PLOT)) {
            plotGlobal();
        }

        if (mOptions.is(KEY_LOCAL_PLOT)) {
            plotLocal();
        }

        LayerBundleManager.getInstance().redraw();
    }
}
