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
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.SurfaceImage;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import java.awt.Color;
import org.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
public class HalloweenRenderer extends BaseRenderer {

    public HalloweenRenderer(RenderableLayer layer) {
        super(layer);
        setLatLon(new MLatLon(-10, 0));
    }

    @Override
    public void run() {
        var pulpMaterial = new Material(Color.decode("#f4d314"));
        var pulpAttrs = new BasicAirspaceAttributes(pulpMaterial, 0.75);

        final double pulpLowAlt = 10 * 10000.0;
        final double pulpHighAlt = pulpLowAlt + 40 * 10000.0;

        var pulpTopCylinder = new CappedCylinder(pulpAttrs);
        pulpTopCylinder.setCenter(LatLon.fromDegrees(90.0, 0.0));
        pulpTopCylinder.setRadii(0.0, 1002 * 10000.0);
        pulpTopCylinder.setAltitudes(pulpLowAlt, pulpHighAlt);
        pulpTopCylinder.setTerrainConforming(false, false);
        pulpTopCylinder.setDragEnabled(false);

        mLayer.addRenderable(pulpTopCylinder);

        var pulpBottomCylinder = new CappedCylinder(pulpAttrs);
        pulpBottomCylinder.setCenter(LatLon.fromDegrees(-90.0, 0.0));
        pulpBottomCylinder.setRadii(0.0, 1002 * 10000.0);
        pulpBottomCylinder.setAltitudes(pulpLowAlt, pulpHighAlt);
        pulpBottomCylinder.setTerrainConforming(false, false);
        pulpBottomCylinder.setDragEnabled(false);

        mLayer.addRenderable(pulpBottomCylinder);

        var skinMaterial = new Material(Color.decode("#e26300"));
        var skinAttrs = new BasicAirspaceAttributes(skinMaterial, 0.75);

        final double skinLowAlt = pulpHighAlt;
        final double skinHighAlt = skinLowAlt + 40 * 10000.0;

        var skinTopCylinder = new CappedCylinder(skinAttrs);
        skinTopCylinder.setCenter(LatLon.fromDegrees(90.0, 0.0));
        skinTopCylinder.setCenter(LatLon.fromDegrees(0.0, 180.0));
        skinTopCylinder.setRadii(0.0, 1700 * 10000.0);
        skinTopCylinder.setAltitudes(skinLowAlt, skinHighAlt);
        skinTopCylinder.setTerrainConforming(false, false);
        skinTopCylinder.setDragEnabled(false);

        mLayer.addRenderable(skinTopCylinder);
        var surfaceImage = new SurfaceImage("pumpkin.png", Sector.FULL_SPHERE);
//surfaceImage.set
        mLayer.addRenderable(surfaceImage);
        //Mapton.notification(MKey.NOTIFICATION_FX_WARNING, "Trick or treat!", "Give me some Jazz", (Action) null);
    }

}
