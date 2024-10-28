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
package org.mapton.butterfly_structural.strain;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final StrainAttributeManager mAttributeManager = StrainAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        sInteractiveLayer = layer;
        sCheckModel = checkModel;
    }

    public void plot(BStructuralStrainGaugePoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;
        plotDirection(p, position);
    }

    public void reset() {
    }

    private Double calcBearing(BStructuralStrainGaugePoint p) {
        if (p.getDirectionX() == null) {
            return null;
        }
        var bearing = MathHelper.convert(p.getDirectionX());
        var o0 = p.ext().getObservationFilteredFirst();
        var o1 = p.ext().getObservationFilteredLast();
        if (ObjectUtils.anyNull(o0, o1)) {
            System.out.println("calc failed " + p.getName());
            return null;

        }

        if (ObjectUtils.anyNull(o0.getMeasuredX(), o1.getMeasuredX(), o0.getMeasuredY(), o1.getMeasuredY())) {
            return null;
        }

        var v0 = Math.atan(o0.getMeasuredY() / o0.getMeasuredX());
        var v1 = Math.atan(o1.getMeasuredY() / o1.getMeasuredX());
        var delta = Math.toDegrees(v1 - v0);
        var r = bearing + delta;

        if (r > 360) {
//            r -= 360;
        } else if (r < 0) {
//            r += 360;
        }

        return r;
    }

    private void plotDirection(BStructuralStrainGaugePoint p, Position position) {
        if (!sCheckModel.isChecked(GraphicRendererItem.DIRECTION)) {
            return;
        }

        var bearing = calcBearing(p);
        if (bearing == null) {
            return;
        }

        try {

            var length = Math.abs(100.0 * p.ext().deltaZero().getDeltaZ());
            length = Math.max(length, 5.0);
//            length = 15.0;
            var p2 = WWHelper.movePolar(position, bearing, length);
            var z = 0.2;
            position = WWHelper.positionFromPosition(position, z);
            p2 = WWHelper.positionFromPosition(p2, z);
            var path = new Path(position, p2);
            path.setAttributes(mAttributeManager.getStrainAttribute());

            addRenderable(path, true);
        } catch (Exception e) {
            System.err.println(e);
        }

    }

}
