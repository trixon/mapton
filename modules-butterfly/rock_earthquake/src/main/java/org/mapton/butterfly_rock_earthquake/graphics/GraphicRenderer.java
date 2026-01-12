/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_rock_earthquake.graphics;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import javax.swing.Timer;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import org.mapton.butterfly_rock_earthquake.QuakeAttributeManager;
import org.mapton.butterfly_rock_earthquake.QuakeHelper;
import org.mapton.butterfly_rock_earthquake.QuakeManager;
import org.mapton.worldwind.api.RoundAnnotation;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends BaseGraphicRenderer<GraphicItem, BRockEarthquake> {

    protected static final PlotLimiter sPlotLimiter = new PlotLimiter();
    private final QuakeAttributeManager mAttributeManager = QuakeAttributeManager.getInstance();
    private Blinker mBlinker;
    private final IndexedCheckModel<GraphicItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer, sPlotLimiter);

        mCheckModel = checkModel;
    }

    @Override
    public void plot(BRockEarthquake quake, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicItem.CIRCLES)) {
            try {
                plotCircle(quake, position);
            } catch (Exception e) {
                System.out.println("%s\n%s".formatted(quake.getName(), e.getMessage()));
            }
        }

        if (mCheckModel.isChecked(GraphicItem.LINES)) {
            try {
                plotLine(quake, position);
            } catch (Exception e) {
                System.out.println("%s\n%s".formatted(quake.getName(), e.getMessage()));
            }
        }
    }

    @Override
    public void reset() {
        super.reset();
    }

    private void plotCircle(BRockEarthquake quake, Position position) {
        var annotation = new RoundAnnotation(position, SwingHelper.getUIScaled(32), mAttributeManager.getAnnotationAttributes());
        annotation.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        if (quake == QuakeManager.getInstance().getTimeFilteredItems().getFirst()) {
            setBlinker(annotation);
        }

        annotation.getAttributes().setTextColor(QuakeHelper.getColor(quake));
        annotation.getAttributes().setScale(Math.abs(quake.getMag()) / 10);
        addRenderable(annotation, true, GraphicItem.CIRCLES, mMapObjects);
    }

    private void plotLine(BRockEarthquake quake, Position position) {
        var startPosition = WWHelper.positionFromPosition(position, 0.0);
        var value = Math.sqrt(Math.pow(10, Math.abs(quake.getMag())));
        var height = 10_000 + value * 1000;
        //var height = quake.getMag() * 200_000;
        var endPosition = WWHelper.positionFromPosition(position, height);

        var groundPath = new Path(startPosition, endPosition);
        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentGroundPathAttributes());
        attrs.setOutlineMaterial(new Material(QuakeHelper.getColor(quake)));
        groundPath.setAttributes(attrs);
        addRenderable(groundPath, true, GraphicItem.LINES, mMapObjects);

        if (quake == QuakeManager.getInstance().getTimeFilteredItems().getFirst()) {
            plotCircle(quake, position);
        }
    }

    private void setBlinker(RoundAnnotation ea) {
        if (mBlinker != null) {
            mBlinker.stop();
        }

        if (ea == null) {
            return;
        }

        mBlinker = new Blinker(ea);
    }

    private class Blinker {

        private RoundAnnotation annotation;
        private double initialScale, initialOpacity;
        private int steps = 10;
        private int step = 0;
        private int delay = 100;
        private Timer timer;

        private Blinker(RoundAnnotation ea) {
            this.annotation = ea;
            this.initialScale = this.annotation.getAttributes().getScale() * .5;
            this.initialOpacity = this.annotation.getAttributes().getOpacity();
            this.timer = new Timer(delay, actionEvent -> {
                annotation.getAttributes().setScale(initialScale * (1f + 7f * ((float) step / (float) steps)));
                annotation.getAttributes().setOpacity(initialOpacity * (1f - ((float) step / (float) steps)));
                step = step == steps ? 0 : step + 1;
            });
            start();
        }

        private void stop() {
            timer.stop();
            step = 0;
            this.annotation.getAttributes().setScale(initialScale);
            this.annotation.getAttributes().setOpacity(initialOpacity);
        }

        private void start() {
            timer.start();
        }
    }
}
