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

import com.jogamp.opengl.GL;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import java.awt.Color;
import java.nio.DoubleBuffer;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BaseGraphicRenderer;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import org.mapton.butterfly_rock_earthquake.QuakeAttributeManager;
import org.mapton.butterfly_rock_earthquake.QuakeManager;
import org.mapton.butterfly_rock_earthquake.QuakeOptions;
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
    private final Color mColorsOfDay[] = {
        Color.RED,
        Color.ORANGE,
        Color.YELLOW,
        Color.GREEN,
        Color.BLUE,
        Color.GRAY,
        Color.BLACK};
    private ArrayList<AVListImpl> mMapObjects;
    private final QuakeOptions mOptions = QuakeOptions.getInstance();

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

    private Color getColor(BRockEarthquake quake) {
        var elapsedDays = Duration.between(quake.getDateLatest(), LocalDateTime.now()).toMillis() / TimeUnit.DAYS.toMillis(1);

        switch (mOptions.getColorBy()) {
            case AGE:
                return mColorsOfDay[Math.min((int) elapsedDays, mColorsOfDay.length - 1)];
            case MAGNITUDE:
//                return ButterflyHelper.getColorAwt(ButterflyHelper.sGreenToRedColors, 10.0, quake.getMag());
                return ButterflyHelper.getColorAwt(ButterflyHelper.sGreenToRedColors, 5, quake.getMag() - 3);
            default:
                throw new AssertionError();
        }

    }

    private void plotCircle(BRockEarthquake quake, Position position) {
        var annotation = new EqAnnotation(position, mAttributeManager.getAnnotationAttributes());
        annotation.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        if (quake == QuakeManager.getInstance().getTimeFilteredItems().getFirst()) {
            setBlinker(annotation);
        }

        annotation.getAttributes().setTextColor(getColor(quake));
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
        attrs.setOutlineMaterial(new Material(getColor(quake)));
        groundPath.setAttributes(attrs);
        addRenderable(groundPath, true, GraphicItem.LINES, mMapObjects);

        if (quake == QuakeManager.getInstance().getTimeFilteredItems().getFirst()) {
            plotCircle(quake, position);
        }
    }

    private void setBlinker(EqAnnotation ea) {
        if (mBlinker != null) {
            mBlinker.stop();
        }

        if (ea == null) {
            return;
        }

        mBlinker = new Blinker(ea);
    }

    private class Blinker {

        private EqAnnotation annotation;
        private double initialScale, initialOpacity;
        private int steps = 10;
        private int step = 0;
        private int delay = 100;
        private Timer timer;

        private Blinker(EqAnnotation ea) {
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

    private class EqAnnotation extends GlobeAnnotation {

        public EqAnnotation(Position position, AnnotationAttributes defaults) {
            super("", position, defaults);
        }

        @Override
        protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale) {
            double finalScale = scale * this.computeScale(dc);

            var gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glTranslated(x, y, 0);
            gl.glScaled(finalScale, finalScale, 1);
        }

        // Override annotation drawing for a simple circle
        private DoubleBuffer shapeBuffer;

        @Override
        protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition) {
            // Draw colored circle around screen point - use annotation's text color
            if (dc.isPickingMode()) {
                this.bindPickableObject(dc, pickPosition);
            }

            this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);

            // Draw 32x32 shape from its bottom left corner
            int size = SwingHelper.getUIScaled(32);
            if (this.shapeBuffer == null) {
                this.shapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_ELLIPSE, size, size, 0, null);
            }
            var gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
            gl.glTranslated(-size / 2, -size / 2, 0);
            FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.shapeBuffer);
        }
    }

}
