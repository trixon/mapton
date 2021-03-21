/*
 * Copyright 2021 Patrik Karlström.
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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.AirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import gov.nasa.worldwind.render.airspaces.Orbit;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.awt.Color;
import java.util.Arrays;
import org.apache.commons.lang3.RandomUtils;
import org.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
public class MardiGrasRenderer extends BaseRenderer {

    public MardiGrasRenderer(RenderableLayer layer) {
        super(layer);
        setLatLon(new MLatLon(28, 0));
    }

    @Override
    public void run() {
        final Material topBreadMaterial = new Material(Color.decode("#712616").darker());
        final Material bottomBreadMaterial = new Material(Color.decode("#712616"));
        final Material creamMaterial = new Material(Color.decode("#fffdd0"));
        final Material sugarMaterial = new Material(Color.decode("#f5f5f5"));

        AirspaceAttributes topBreadAttrs = new BasicAirspaceAttributes(topBreadMaterial, 0.95);
        AirspaceAttributes botttomBreadAttrs = new BasicAirspaceAttributes(bottomBreadMaterial, 0.95);
        AirspaceAttributes creamAttrs = new BasicAirspaceAttributes(creamMaterial, 0.95);
        AirspaceAttributes sugarAttrs = new BasicAirspaceAttributes(sugarMaterial, 1.0);

        CappedCylinder topCylinder = new CappedCylinder(topBreadAttrs);
        topCylinder.setCenter(LatLon.fromDegrees(90.0, 0.0));
        topCylinder.setRadii(0.0, 700 * 10000.0);
        final double lowAlt = 10 * 10000.0;
        final double highAlt = 50 * 10000.0;
        topCylinder.setAltitudes(lowAlt, highAlt);
        topCylinder.setTerrainConforming(false, false);
        topCylinder.setDragEnabled(false);

        CappedCylinder bottomCylinder = new CappedCylinder(botttomBreadAttrs);
        bottomCylinder.setCenter(LatLon.fromDegrees(-90.0, 0.0));
        bottomCylinder.setRadii(0.0, 11000 * 1000.0);
        bottomCylinder.setAltitudes(lowAlt, highAlt);
        bottomCylinder.setTerrainConforming(false, false);
        bottomCylinder.setDragEnabled(false);

        Orbit creamOrbit = new Orbit(creamAttrs);
        creamOrbit.setLocations(LatLon.fromDegrees(18, 180), LatLon.fromDegrees(18, 90));
        creamOrbit.setAltitudes(lowAlt, highAlt * 1.5);
        creamOrbit.setWidth(450 * 10000.0);
        creamOrbit.setOrbitType(Orbit.OrbitType.CENTER);
        creamOrbit.setTerrainConforming(false, false);
        creamOrbit.setDragEnabled(false);

        mLayer.addRenderable(topCylinder);
        mLayer.addRenderable(bottomCylinder);

        double baseLat = 18.0;
        double width = 12.0;

        int step = 60;
        for (int i = -180; i < 180; i = i + step) {
            Polygon creamPolygon = new Polygon(creamAttrs);
            double startLon = i;
            double stopLon = i + step;
            creamPolygon.setLocations(Arrays.asList(
                    LatLon.fromDegrees(baseLat - width, startLon),
                    LatLon.fromDegrees(baseLat - width, stopLon),
                    LatLon.fromDegrees(baseLat + width, stopLon),
                    LatLon.fromDegrees(baseLat + width, startLon)
            ));
            creamPolygon.setAltitudes(lowAlt, highAlt * 1.5);
            creamPolygon.setTerrainConforming(true, true);
            mLayer.addRenderable(creamPolygon);
        }

        for (int i = 0; i < 2000; i++) {
            double lat = RandomUtils.nextDouble(50, 90);
            double lon = RandomUtils.nextDouble(0, 360) - 180;

            CappedCylinder sugarCylinder = new CappedCylinder(sugarAttrs);
            sugarCylinder.setCenter(LatLon.fromDegrees(lat, lon));
            sugarCylinder.setRadii(0.0, 7 * 10000.0 * RandomUtils.nextDouble(0.3, 1.2));
            sugarCylinder.setAltitudes(highAlt, highAlt * RandomUtils.nextDouble(1, 1.01));
            sugarCylinder.setTerrainConforming(false, false);
            sugarCylinder.setDragEnabled(false);
            mLayer.addRenderable(sugarCylinder);
        }
    }
}
