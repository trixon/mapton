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
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Color;
import java.util.ArrayList;
import org.mapton.api.MLatLonBox;
import org.mapton.worldwind.api.worldwind.AnalyticSurface;
import org.mapton.worldwind.api.worldwind.AnalyticSurfaceAttributes;

/**
 *
 * @author Patrik Karlström
 */
public class AnalyticGrid {

    private final double HUE_BLUE = 240d / 360d;
    private final double HUE_RED = 0d / 360d;
    private final AnalyticSurfaceAttributes mAttributes = new AnalyticSurfaceAttributes();
    private boolean mDynamicMinMax;
    private GridData mGridData;
    private final int mHeight;
    private final RenderableLayer mLayer;
    private Renderable mLegend;
    private double mMax;
    private double mMin;
    private final AnalyticSurface mSurface = new AnalyticSurface();
    private final int mWidth;

    public AnalyticGrid(RenderableLayer layer, GridData gridData, double altitude) {
        this(layer, gridData, altitude, gridData.getMin(), gridData.getMax());
        mDynamicMinMax = true;
    }

    public AnalyticGrid(RenderableLayer layer, GridData gridData, double altitude, double min, double max) {
        mLayer = layer;

        mWidth = gridData.getWidth();
        mHeight = gridData.getHeight();

        mMin = min;
        mMax = max;

        MLatLonBox latLonBox = gridData.getLatLonBox();
        mSurface.setSector(Sector.fromDegrees(
                latLonBox.getSouthWest().getLatitude(),
                latLonBox.getNorthEast().getLatitude(),
                latLonBox.getSouthWest().getLongitude(),
                latLonBox.getNorthEast().getLongitude()
        ));

        mSurface.setAltitude(altitude);
        mSurface.setDimensions(mWidth, mHeight);
        mSurface.setClientLayer(mLayer);

        mAttributes.setShadowOpacity(0.5);
        mSurface.setSurfaceAttributes(mAttributes);

        setGridData(gridData);
    }

    public AnalyticSurfaceAttributes getAttributes() {
        return mAttributes;
    }

    public AnalyticSurface getSurface() {
        return mSurface;
    }

    public void setGridData(GridData gridData) {
        mGridData = gridData;

        if (mDynamicMinMax) {
            mMin = gridData.getMin();
            mMax = gridData.getMax();
        }

        ArrayList<AnalyticSurface.GridPointAttributes> gridPointAttributeses = new ArrayList<>();
        double[] values = gridData.getGridAggregates();

        for (int i = 0; i < values.length; i++) {
            gridPointAttributeses.add(createColorGradientAttributes(values[i], mMin, mMax, HUE_RED, HUE_BLUE));
        }

        mSurface.setValues(gridPointAttributeses);

        if (mLayer != null) {
            mLayer.firePropertyChange(AVKey.LAYER, null, mLayer);
        }
    }

    public void setLegendVisible(boolean visible) {
        if (visible) {
            mLayer.addRenderable(mLegend);
        } else {
            mLayer.removeRenderable(mLegend);
        }
    }

    private AnalyticSurface.GridPointAttributes createColorGradientAttributes(final double value, double minValue, double maxValue, double minHue, double maxHue) {
        double hueFactor = WWMath.computeInterpolationFactor(value, minValue, maxValue);
        Color color = Color.getHSBColor((float) WWMath.mixSmooth(hueFactor, minHue, maxHue), 1f, 1f);
        double opacity = value == 0 ? 0.1 : 1.0;

        Color rgbaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * opacity));

        return AnalyticSurface.createGridPointAttributes(value, rgbaColor);
    }
}
