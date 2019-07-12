/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.worldwind.api.analytic;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Extent;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.BufferFactory;
import gov.nasa.worldwind.util.BufferWrapper;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.util.ArrayList;
import javax.swing.Timer;
import org.mapton.worldwind.api.worldwind.AnalyticSurface;
import org.mapton.worldwind.api.worldwind.AnalyticSurfaceAttributes;
import org.mapton.worldwind.api.worldwind.AnalyticSurfaceLegend;
import org.mapton.worldwind.api.worldwind.WWExampleUtil;

/**
 *
 * @author Patrik Karlström
 */
public class AnalyticGrid {

    private final int DEFAULT_RANDOM_ITERATIONS = 1000;
    private final double DEFAULT_RANDOM_SMOOTHING = 0.5d;
    private final double HUE_BLUE = 240d / 360d;
    private final double HUE_RED = 0d / 360d;

    private AnalyticSurface mAnalyticSurface;
    private AnalyticSurfaceAttributes mAnalyticSurfaceAttributes;

    public AnalyticGrid() {
    }

    public void createRandomAltitudeSurface(RenderableLayer layer) {
        wwCreateRandomAltitudeSurface(HUE_RED, HUE_BLUE, 400, 400, layer);
    }

    private Renderable wwCreateLegendRenderable(final AnalyticSurface surface, final double surfaceMinScreenSize, final AnalyticSurfaceLegend legend) {
        return (DrawContext dc) -> {
            Extent extent = surface.getExtent(dc);
            if (!extent.intersects(dc.getView().getFrustumInModelCoordinates())) {
                return;
            }

            if (WWMath.computeSizeInWindowCoordinates(dc, extent) < surfaceMinScreenSize) {
                return;
            }

            legend.render(dc);
        };
    }

    private Iterable<? extends AnalyticSurface.GridPointAttributes> wwCreateMixedColorGradientGridValues(double a, BufferWrapper firstBuffer, BufferWrapper secondBuffer, double minValue, double maxValue, double minHue, double maxHue) {
        ArrayList<AnalyticSurface.GridPointAttributes> attributesList = new ArrayList<>();

        long length = Math.min(firstBuffer.length(), secondBuffer.length());
        for (int i = 0; i < length; i++) {
            double value = WWMath.mixSmooth(a, firstBuffer.getDouble(i), secondBuffer.getDouble(i));
            attributesList.add(
                    AnalyticSurface.createColorGradientAttributes(value, minValue, maxValue, minHue, maxHue));
        }

        return attributesList;
    }

    private void wwCreateRandomAltitudeSurface(double minHue, double maxHue, int width, int height, RenderableLayer outLayer) {
        double minValue = -40;
        double maxValue = 40;

        AnalyticSurface surface = new AnalyticSurface();
        surface.setSector(Sector.fromDegrees(-90, 90, -180, 180));
        surface.setAltitude(40);
        surface.setDimensions(width, height);
        surface.setClientLayer(outLayer);
        outLayer.addRenderable(surface);

        BufferWrapper firstBuffer = wwRandomGridValues(width, height, minValue, maxValue);
        BufferWrapper secondBuffer = wwRandomGridValues(width, height, minValue * 2d, maxValue / 2d);
        wwMixValuesOverTime(2000L, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue, surface);

        AnalyticSurfaceAttributes attr = new AnalyticSurfaceAttributes();
        attr.setShadowOpacity(0.5);
        surface.setSurfaceAttributes(attr);

        final double altitude = surface.getAltitude();
        final double verticalScale = surface.getVerticalScale();

        Format legendLabelFormat = new DecimalFormat("# m") {
            public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
                double altitudeMeters = altitude + verticalScale * number;
//                double altitudeKm = altitudeMeters * WWMath.METERS_TO_KILOMETERS;
                double altitudeKm = altitudeMeters;
                return super.format(altitudeKm, result, fieldPosition);
            }
        };

        AnalyticSurfaceLegend legend = AnalyticSurfaceLegend.fromColorGradient(minValue, maxValue, minHue, maxHue,
                AnalyticSurfaceLegend.createDefaultColorGradientLabels(minValue, maxValue, legendLabelFormat),
                AnalyticSurfaceLegend.createDefaultTitle("Skala"));

        legend.setOpacity(0.8);
        legend.setScreenLocation(new Point(50, 200));
        outLayer.addRenderable(wwCreateLegendRenderable(surface, 300, legend));
    }

    private void wwMixValuesOverTime(
            final long timeToMix,
            final BufferWrapper firstBuffer, final BufferWrapper secondBuffer,
            final double minValue, final double maxValue, final double minHue, final double maxHue,
            final AnalyticSurface surface) {
        Timer timer = new Timer(20, new ActionListener() {
            protected long startTime = -1;

            public void actionPerformed(ActionEvent e) {
                if (this.startTime < 0) {
                    this.startTime = System.currentTimeMillis();
                }

                double t = (double) (e.getWhen() - this.startTime) / (double) timeToMix;
                int ti = (int) Math.floor(t);

                double a = t - ti;
                if ((ti % 2) == 0) {
                    a = 1d - a;
                }

                surface.setValues(wwCreateMixedColorGradientGridValues(a, firstBuffer, secondBuffer, minValue, maxValue, minHue, maxHue));

                if (surface.getClientLayer() != null) {
                    surface.getClientLayer().firePropertyChange(AVKey.LAYER, null, surface.getClientLayer());
                }
            }
        });
        timer.start();
    }

    private BufferWrapper wwRandomGridValues(int width, int height, double min, double max, int numIterations, double smoothness, BufferFactory factory) {
        double[] values = WWExampleUtil.createRandomGridValues(width, height, min, max, numIterations, smoothness);
        BufferWrapper wrapper = factory.newBuffer(values.length);

        wrapper.putDouble(0, values, 0, values.length);

        return wrapper;
    }

    private BufferWrapper wwRandomGridValues(int width, int height, double min, double max) {
        return wwRandomGridValues(width, height, min, max, DEFAULT_RANDOM_ITERATIONS, DEFAULT_RANDOM_SMOOTHING, new BufferFactory.DoubleBufferFactory());
    }
}
