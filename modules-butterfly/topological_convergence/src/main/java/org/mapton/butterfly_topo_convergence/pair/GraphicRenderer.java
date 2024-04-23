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
package org.mapton.butterfly_topo_convergence.pair;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_topo_convergence.ConvergenceAttributeManager;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final ConvergenceAttributeManager mAttributeManager = ConvergenceAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mGroundConnectorLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final Material[] mMaterials;
    private final RenderableLayer mNodeLayer;
    private double mOffset;
    private final HashSet<String> mPlottedNodes = new HashSet<>();
    private final RenderableLayer mSurfaceLayer;

    public GraphicRenderer(RenderableLayer nodeLayer, RenderableLayer groundConnectorLayer, RenderableLayer surfaceLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mNodeLayer = nodeLayer;
        mGroundConnectorLayer = groundConnectorLayer;
        mSurfaceLayer = surfaceLayer;
        mCheckModel = checkModel;
        mMaterials = new Material[]{
            //https://colordesigner.io/gradient-generator/?mode=hsl#00FF00-FF0000
            new Material(Color.decode("#00ff00")),
            new Material(Color.decode("#22ff00")),
            new Material(Color.decode("#44ff00")),
            new Material(Color.decode("#66ff00")),
            new Material(Color.decode("#88ff00")),
            new Material(Color.decode("#aaff00")),
            new Material(Color.decode("#ccff00")),
            new Material(Color.decode("#eeff00")),
            new Material(Color.decode("#ffee00")),
            new Material(Color.decode("#ffcc00")),
            new Material(Color.decode("#ffaa00")),
            new Material(Color.decode("#ff8800")),
            new Material(Color.decode("#ff6600")),
            new Material(Color.decode("#ff4400")),
            new Material(Color.decode("#ff2200")),
            new Material(Color.decode("#ff0000"))
        };
    }

    public void addRenderable(RenderableLayer layer, Renderable renderable) {
        layer.addRenderable(renderable);
        if (layer == mNodeLayer) {
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    public void plot(BTopoConvergencePair pair, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        mOffset = pair.getConvergenceGroup().ext2().getControlPoints().stream()
                .map(p -> p.getZeroZ())
                .mapToDouble(Double::doubleValue).min().orElse(0);
        if (mOffset < 0) {
            mOffset = mOffset * -1.0;
        }
        mOffset += 2;

        if (mCheckModel.isChecked(GraphicRendererItem.LINES)) {
            plotLines(pair, position, mapObjects);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.NODE)) {
            plotNodes(pair, mapObjects);
        }
    }

    public void reset() {
        mPlottedNodes.clear();
    }

    private Position getPosition(BTopoControlPoint controlPoint) {
        var altitude = controlPoint.getZeroZ() + mOffset;
        return Position.fromDegrees(controlPoint.getLat(), controlPoint.getLon(), altitude);
    }

    private void plotLines(BTopoConvergencePair pair, Position position, ArrayList<AVListImpl> mapObjects) {
        var pos1 = getPosition(pair.getP1());
        var pos2 = getPosition(pair.getP2());

        var path = new Path(pos1, pos2);
        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentGroundPathAttributes());
        var delta = pair.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();
        var max = 0.010;
        var level = Math.min(mMaterials.length - 1, (Math.abs(delta) / max) * (mMaterials.length - 1));
        attrs.setOutlineMaterial(mMaterials[(int) level]);

        if (delta >= 0) {
            attrs.setOutlineStippleFactor(3);
        }

        path.setAttributes(attrs);
        addRenderable(mGroundConnectorLayer, path);
        mapObjects.add(path);
    }

    private void plotNode(BTopoControlPoint controlPoint) {
        var name = controlPoint.getName();
        if (!mPlottedNodes.contains(name)) {
            var radius = 0.6;
            var pyramid = new Pyramid(getPosition(controlPoint), radius, radius);
            pyramid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(mNodeLayer, pyramid);

            mPlottedNodes.add(name);
        }
    }

    private void plotNodes(BTopoConvergencePair pair, ArrayList<AVListImpl> mapObjects) {
        plotNode(pair.getP1());
        plotNode(pair.getP2());
    }

}
