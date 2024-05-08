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

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_topo_convergence.ConvergenceAttributeManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final ConvergenceAttributeManager mAttributeManager = ConvergenceAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mLineLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final Material[] mMaterials;
    private final RenderableLayer mNodeLayer;
    private final HashSet<String> mPlottedLabels = new HashSet<>();
    private final HashSet<String> mPlottedNodes = new HashSet<>();
    private final RenderableLayer mSurfaceLayer;

    public GraphicRenderer(RenderableLayer nodeLayer, RenderableLayer groundConnectorLayer, RenderableLayer surfaceLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mNodeLayer = nodeLayer;
        mLineLayer = groundConnectorLayer;
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
        if (layer == mLineLayer) {
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    public void plot(BTopoConvergencePair pair, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicRendererItem.LINES)) {
            plotLine(pair);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.NODE)) {
            plotNodes(pair);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.LABELS)) {
            plotLabels(pair);
        }
    }

    public void reset() {
        mPlottedNodes.clear();
        mPlottedLabels.clear();
    }

    private void plotLabel(BTopoConvergencePair pair, BTopoControlPoint controlPoint) {
        var name = controlPoint.getName();

        if (!mPlottedLabels.contains(name)) {
            var position = PairHelper.getPosition(controlPoint, pair.getOffset());
            var placemark = new PointPlacemark(position);
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText(StringUtils.remove(controlPoint.getName(), pair.getConvergenceGroup().getName()));
            addRenderable(mNodeLayer, placemark);
            mPlottedLabels.add(name);
        }
    }

    private void plotLabels(BTopoConvergencePair pair) {
        plotLabel(pair, pair.getP1());
        plotLabel(pair, pair.getP2());
    }

    private void plotLine(BTopoConvergencePair pair) {
        if (pair.getObservations().isEmpty()) {
            return;
        }

        var pos1 = PairHelper.getPosition(pair.getP1(), pair.getOffset());
        var pos2 = PairHelper.getPosition(pair.getP2(), pair.getOffset());

        var path = new Path(pos1, pos2);
        var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
        var delta = pair.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();
        int level = pair.getLevel(mMaterials.length);
        attrs.setOutlineMaterial(mMaterials[level]);
        if (delta >= 0) {
            attrs.setOutlineStippleFactor(3);
        }

        path.setAttributes(attrs);
        addRenderable(mLineLayer, path);
    }

    private void plotNode(BTopoControlPoint controlPoint, double offset) {
        var name = controlPoint.getName();
        if (!mPlottedNodes.contains(name)) {
            var radius = PairHelper.NODE_SIZE;
            var position = PairHelper.getPosition(controlPoint, offset);
            var pyramid = new Pyramid(position, radius, radius);
            pyramid.setAttributes(mAttributeManager.getNodeAttributes());
            addRenderable(mNodeLayer, pyramid);

            var groundPath = new Path(position, WWHelper.positionFromPosition(position, 0.0));
            groundPath.setAttributes(mAttributeManager.getGroundPathAttributes());
            addRenderable(mNodeLayer, groundPath);

            mPlottedNodes.add(name);

            var leftClickRunnable = (Runnable) () -> {
                Mapton.getGlobalState().put(ConvergencePairChartBuilder.class.getName() + "node", name);
            };
            pyramid.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
        }
    }

    private void plotNodes(BTopoConvergencePair pair) {
        plotNode(pair.getP1(), pair.getOffset());
        plotNode(pair.getP2(), pair.getOffset());
    }

}
