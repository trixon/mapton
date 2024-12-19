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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import java.awt.Color;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.mapton.api.MLatLon;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class JulgranRenderer extends BaseRenderer {

    private final MLatLon mLatLon = new MLatLon(57.707144, 11.966839);

    public JulgranRenderer(RenderableLayer layer) {
        super(layer);
        initJulgran();
        mLayer.setPickEnabled(false);
    }

    @Override
    public void run() {
    }

    private void initJulgran() {
        var now = LocalDateTime.now();
        var startDate = LocalDate.of(now.getYear(), 12, 14);
        var endDate = LocalDate.of(now.getYear() + 1, 1, 14);

        if (DateHelper.isBetween(startDate, endDate, now.toLocalDate())) {
            var trunkHeight = 150.0;
            var trunkRadius = 20.0;
            var coneHeight = 400.0;
            var coneRadius = 150.0;
            var ballRadius = 40.0;
            var ballHeight = trunkHeight + coneHeight + 0.25 * ballRadius;

            var position = Position.fromDegrees(mLatLon.getLatitude(), mLatLon.getLongitude());

            var trunk = new CappedCylinder(position, trunkRadius);
            trunk.setAltitudes(0, trunkHeight);
            mLayer.addRenderable(trunk);

            var cone = new Cone(WWHelper.positionFromPosition(position, trunkHeight + coneHeight * 0.5), coneHeight, coneRadius);
            mLayer.addRenderable(cone);

            var ball = new Ellipsoid(WWHelper.positionFromPosition(position, ballHeight), ballRadius, ballRadius, ballRadius);
            mLayer.addRenderable(ball);

            var trunkAttrs = new BasicShapeAttributes();
            trunkAttrs.setDrawInterior(true);
            trunkAttrs.setDrawOutline(false);
            trunkAttrs.setInteriorMaterial(new Material(Color.decode("#644117")));
            trunkAttrs.setEnableLighting(true);
            trunk.setAttributes(trunkAttrs);

            var coneAttrs = new BasicShapeAttributes();
            coneAttrs.setDrawInterior(true);
            coneAttrs.setDrawOutline(true);
            coneAttrs.setInteriorMaterial(new Material(Color.GREEN.darker()));
            coneAttrs.setEnableLighting(true);
            coneAttrs.setOutlineMaterial(Material.WHITE);
            coneAttrs.setOutlineWidth(4);
            cone.setAttributes(coneAttrs);

            var ballAttrs = new BasicShapeAttributes();
            ballAttrs.setDrawInterior(true);
            ballAttrs.setDrawOutline(false);
            ballAttrs.setInteriorMaterial(Material.YELLOW);
            ballAttrs.setEnableLighting(true);
            ball.setAttributes(ballAttrs);
        }
    }

}
