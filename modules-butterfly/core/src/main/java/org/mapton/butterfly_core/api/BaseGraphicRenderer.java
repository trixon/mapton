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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BBase;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseGraphicRenderer<T extends Enum<T>, U extends BBase> {

    public static final double DEFAULT_AXIS_LENGTH = 2.0;
    public static final double PERCENTAGE_ALTITUDE = 50.0;
    public static final double PERCENTAGE_SIZE = 1.5;
    public static final double PERCENTAGE_SIZE_ALARM = PERCENTAGE_SIZE * 1.2;
    public static final double PERCENTAGE_SIZE_ALARM_HEIGHT = PERCENTAGE_SIZE * 0.05;

    private final BaseAttributeManager mAttributeManager = new BaseAttributeManager() {
    };
    private final RenderableLayer mInteractiveLayer;
    private final RenderableLayer mPassiveLayer;
    private final PlotLimiter mPlotLimiter;

    public BaseGraphicRenderer(RenderableLayer interactiveLayer, RenderableLayer passiveLayer, PlotLimiter plotLimiter) {
        mInteractiveLayer = interactiveLayer;
        mPassiveLayer = passiveLayer;
        mPlotLimiter = plotLimiter;
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer, Object plotLimiterKey, ArrayList<AVListImpl> mapObjects) {
        if (interactiveLayer) {
            mInteractiveLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist && mapObjects != null) {
                mapObjects.add(avlist);
            }
        } else if (mPassiveLayer != null) {
            mPassiveLayer.addRenderable(renderable);
        }

        mPlotLimiter.incPlotCounter(plotLimiterKey);
    }

    public void addToAllowList(Object item) {
        mPlotLimiter.addToAllowList(item);
    }

    public RenderableLayer getInteractiveLayer() {
        return mInteractiveLayer;
    }

    public PlotLimiter getPlotLimiter() {
        return mPlotLimiter;
    }

    public void plotAxis(BBase p, Position position, double length) {
        plotAxis(p, position, length, p.getAzimuth());
    }

    public void plotAxis(BBase p, Position position, double length, double altitude) {
        plotAxis(p, position, length, p.getAzimuth(), altitude);
    }

    public void plotAxis(BBase p, Position position, double length, Double azimuth) {
        plotAxis(p, position, length, azimuth, 0);
    }

    public void plotAxis(BBase p, Position position, double length, Double azimuth, double altitude) {
        if (azimuth == null) {
            return;
        }

        try {
            var z = altitude + 0.1;
            var bearing = MathHelper.convertCcwDegreeToCw(azimuth);
            var p2 = WWHelper.movePolar(position, bearing, length, z);
            position = WWHelper.positionFromPosition(position, z);
            p2 = WWHelper.positionFromPosition(p2, z);
            var arrowHeadSize = 0.15;

            //East - Positive X
            var pathE = new Path(position, p2);
            pathE.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathE, false, null, null);

            var x0 = WWHelper.movePolar(p2, bearing + 0, arrowHeadSize, z);
            var x1 = WWHelper.movePolar(p2, bearing + 120, arrowHeadSize, z);
            var x2 = WWHelper.movePolar(p2, bearing + 240, arrowHeadSize, z);
            var xPolygon = new Polygon(List.of(x0, x1, x2));
            xPolygon.setAltitudes(z + 0.0, z + 0.1);
            xPolygon.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(xPolygon, false, null, null);

            //West
            p2 = WWHelper.movePolar(position, bearing - 180, length, z);
            var pathW = new Path(position, p2);
            pathW.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathW, false, null, null);

            //North
            p2 = WWHelper.movePolar(position, bearing - 90, length, z);
            var pathN = new Path(position, p2);
            pathN.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathN, false, null, null);

            var y0 = WWHelper.movePolar(p2, bearing - 90 + 0, arrowHeadSize, z);
            var y1 = WWHelper.movePolar(p2, bearing - 90 + 120, arrowHeadSize, z);
            var y2 = WWHelper.movePolar(p2, bearing - 90 + 240, arrowHeadSize, z);
            var yPolygon = new Polygon(List.of(y0, y1, y2));
            yPolygon.setAltitudes(z + 0.0, z + 0.1);
            yPolygon.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(yPolygon, false, null, null);

            //South
            p2 = WWHelper.movePolar(position, bearing + 90, length, z);
            var pathS = new Path(position, p2);
            pathS.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathS, false, null, null);
        } catch (Exception e) {
            //System.err.println(e);
        }
    }

    public void plotPercentageAlarmIndicator(Position position, BAlarm alarm, RigidShape rigidShape, boolean rising) {
        if (alarm == null
                || alarm.ext().getRange0() == null
                || alarm.ext().getRange1() == null
                || alarm.ext().getRange0().getMaximum() == null
                || alarm.ext().getRange1().getMaximum() == null) {
            return;
        }

        try {
            Double limit0;
            Double limit1;
            if (rising) {
                limit0 = alarm.ext().getRange0().getMaximum();
                limit1 = alarm.ext().getRange1().getMaximum();
                if (Double.isInfinite(limit0) || Double.isInfinite(limit1)) {
                    limit0 = alarm.ext().getRange0().getMinimum();
                    limit1 = alarm.ext().getRange1().getMinimum();
                }
            } else {
                limit0 = alarm.ext().getRange0().getMinimum();
                limit1 = alarm.ext().getRange1().getMinimum();
                if (Double.isInfinite(limit0) || Double.isInfinite(limit1)) {
                    limit0 = alarm.ext().getRange0().getMaximum();
                    limit1 = alarm.ext().getRange1().getMaximum();
                }
            }

            var percent = limit0 / limit1;
            if (Double.isNaN(percent) || Double.isInfinite(percent)) {
                percent = 0.0;
            }

            var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percent);
            rigidShape.setCenterPosition(pos);
            var attrs = new BasicShapeAttributes(mAttributeManager.getAlarmInteriorAttributes(1));
            attrs.setInteriorOpacity(0.4);
            rigidShape.setAttributes(attrs);
            addRenderable(rigidShape, false, null, null);
        } catch (Exception e) {
            //System.err.println(e);
        }
    }

    public void plotPercentageRod(Position position, Integer percent) {
        if (percent == null) {
            percent = 0;
        }

        var maxTopAltitudePercentage = Math.max(percent, 100) / 100.0;
        var stepPercentage = 0.25;
        var topAltitudePercentage = stepPercentage;
        var odd = true;

        do {
            var loPos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * (topAltitudePercentage - stepPercentage));
            var hiPos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * topAltitudePercentage);
            topAltitudePercentage = topAltitudePercentage + stepPercentage;
            var groundPath = new Path(loPos, hiPos);
            groundPath.setAttributes(odd ? mAttributeManager.getComponentGroundPathOddAttributes() : mAttributeManager.getComponentGroundPathEvenAttributes());
            odd = !odd;
            addRenderable(groundPath, false, null, null);
        } while (topAltitudePercentage < maxTopAltitudePercentage + stepPercentage);

        var pos100 = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE);
        var cylinder = new Cylinder(pos100, 0.25, PERCENTAGE_SIZE * 2);
        cylinder.setAttributes(mAttributeManager.getAlarmLimit());
        addRenderable(cylinder, false, null, null);
    }

    public void postPlot() {

    }

    public void reset() {
        resetPlotLimiter();
    }

    public void resetPlotLimiter() {
        mPlotLimiter.reset();
    }

    protected boolean isPlotLimitReached(U p, Object key, Position position, boolean emptyList, ArrayList<AVListImpl> mapObjects) {
        if (mPlotLimiter.isLimitReached(key, p)) {
            addRenderable(mPlotLimiter.createPlotLimitIndicator(position, emptyList), true, null, mapObjects);
            return true;
        } else {
            return false;
        }
    }
}
