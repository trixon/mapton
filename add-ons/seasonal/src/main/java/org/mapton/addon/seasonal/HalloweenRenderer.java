/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.geom.LatLon;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class HalloweenRenderer extends BaseRenderer {

    public HalloweenRenderer(RenderableLayer layer) {
        super(layer);
        setLatLon(new MLatLon(28, 0));
        setZoom(0.05);
    }

    @Override
    public void run() {
        var skinMaterial = new Material(Color.decode("#e26300"));
        var skinAttrs = new BasicAirspaceAttributes(skinMaterial, 0.9);
        var featureMaterial = Material.BLACK;
        var featureAttrs = new BasicAirspaceAttributes(featureMaterial, 0.8);
        var hornAttrs = new BasicAirspaceAttributes(skinMaterial, 1.0);

        final double skinLowAlt = 100 * 1000.0;
        final double skinHighAlt = skinLowAlt * 2.0;

        var skinCylinder = new CappedCylinder(skinAttrs);
        skinCylinder.setCenter(LatLon.fromDegrees(90.0, 0.0));
        skinCylinder.setCenter(LatLon.fromDegrees(0.0, 0.0));
        skinCylinder.setRadii(0.0, 2000 * 10000.0);
        skinCylinder.setAltitudes(skinLowAlt, skinHighAlt);
        skinCylinder.setTerrainConforming(false, false);
        skinCylinder.setDragEnabled(false);

        mLayer.addRenderable(skinCylinder);

        final double hornHeight = 3000000;
        final double baseRadius = 1000000;
        final double hornRadius = baseRadius * 1.0;

        Cone westCone = new Cone(Position.fromDegrees(57, -50, skinHighAlt + hornHeight / 2.5), hornHeight, hornRadius);
        westCone.setAttributes(hornAttrs);
        mLayer.addRenderable(westCone);

        Cone eastCone = new Cone(Position.fromDegrees(57, 50, skinHighAlt + hornHeight / 2.5), hornHeight, hornRadius);
        eastCone.setAttributes(hornAttrs);
        mLayer.addRenderable(eastCone);

//        final double eyeRadius = baseRadius * 0.8;
//        CappedCylinder westEyeCylinder = new CappedCylinder(hornAttrs);
//        westEyeCylinder.setCenter(LatLon.fromDegrees(47, -23));
//        westEyeCylinder.setRadii(0.0, eyeRadius);
//        westEyeCylinder.setAltitudes(skinHighAlt, skinHighAlt * 1.3);
//        westEyeCylinder.setTerrainConforming(false, false);
//        westEyeCylinder.setDragEnabled(false);
//        mLayer.addRenderable(westEyeCylinder);
        var westEyePolygon = new Polygon(featureAttrs);
        westEyePolygon.setAltitudes(skinHighAlt, skinHighAlt * 2.0);
        var westEyeList = new ArrayList<LatLon>();
        westEyeList.add(LatLon.fromDegrees(30, -30));
        westEyeList.add(LatLon.fromDegrees(30, -15));
        westEyeList.add(LatLon.fromDegrees(45, -15));
        westEyePolygon.setLocations(westEyeList);
        mLayer.addRenderable(westEyePolygon);

        var rightEyePolygon = new Polygon(featureAttrs);
        rightEyePolygon.setAltitudes(skinHighAlt, skinHighAlt * 2.0);
        var rightEyeList = new ArrayList<LatLon>();
        rightEyeList.add(LatLon.fromDegrees(30, 30));
        rightEyeList.add(LatLon.fromDegrees(30, 15));
        rightEyeList.add(LatLon.fromDegrees(45, 15));
        rightEyePolygon.setLocations(rightEyeList);
        mLayer.addRenderable(rightEyePolygon);

        var nosePolygon = new Polygon(featureAttrs);
        nosePolygon.setAltitudes(skinHighAlt, skinHighAlt * 2.0);
        var noseList = new ArrayList<LatLon>();
        noseList.add(LatLon.fromDegrees(10, -7.5));
        noseList.add(LatLon.fromDegrees(10, 7.5));
        noseList.add(LatLon.fromDegrees(25, 0));
        nosePolygon.setLocations(noseList);
        mLayer.addRenderable(nosePolygon);

        var mouthList = new ArrayList<LatLon>();
        double baseLat = -10;
        int lonSpan = 100;
        for (int i = -lonSpan / 2; i <= lonSpan / 2; i += 10) {
            double lon = i;
            boolean odd = (i / 10 & 1) != 0;
            double span = (odd ? 2 : 3) * RandomUtils.nextDouble(0.8, 1.2);
            double lat = baseLat + span;
            mouthList.add(LatLon.fromDegrees(lat, lon));
        }

        var mouthPolygon = new Polygon(featureAttrs);
        mouthPolygon.setAltitudes(skinHighAlt, skinHighAlt * 2.0);
        mouthPolygon.setLocations(mouthList);
        mLayer.addRenderable(mouthPolygon);

        Mapton.notification(MKey.NOTIFICATION_FX_WARNING, "Trick or treat!", "Give me some Jazz,\nthen Go Home!", (Action) null);
    }
}
