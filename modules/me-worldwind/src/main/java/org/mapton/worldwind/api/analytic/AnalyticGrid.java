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
package org.mapton.worldwind.api.analytic;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.util.WWMath;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import org.mapton.api.MLatLonBox;
import org.mapton.worldwind.api.worldwind.AnalyticSurface;
import org.mapton.worldwind.api.worldwind.AnalyticSurface.GridPointAttributes;
import org.mapton.worldwind.api.worldwind.AnalyticSurfaceAttributes;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AnalyticGrid {

    public static final double HUE_BLUE = 240d / 360d;
    public static final double HUE_RED = 0d / 360d;

    private final AnalyticSurfaceAttributes mAttributes = new AnalyticSurfaceAttributes();
    private Dimension mDimension;
    private boolean mDynamicMinMax;
    private GridData mGridData;
    private int mHeight;
    private final RenderableLayer mLayer;
    private Renderable mLegend;
    private double mMax;
    private double mMaxHue = HUE_RED;
    private double mMin;
    private double mMinHue = HUE_BLUE;
    private double mNullOpacity = 0.0;
    private final AnalyticSurface mSurface = new AnalyticSurface();
    private int mWidth;
    private double mZeroOpacity = 1.0;
    private int mZeroValueSearchRange = 4;

    public AnalyticGrid(RenderableLayer layer, double altitude) {
        this(layer, altitude, -1, -1);
        mDynamicMinMax = true;
    }

    public AnalyticGrid(RenderableLayer layer, double altitude, double min, double max) {
        mLayer = layer;

        mMin = min;
        mMax = max;

        mSurface.setAltitude(altitude);
        mSurface.setClientLayer(mLayer);

        mAttributes.setShadowOpacity(0.5);
        mAttributes.setDrawOutline(false);

        mSurface.setSurfaceAttributes(mAttributes);
    }

    public AnalyticSurfaceAttributes getAttributes() {
        return mAttributes;
    }

    public AnalyticSurface getSurface() {
        return mSurface;
    }

    public void setGridData(GridData gridData) {
        mGridData = gridData;
        mWidth = gridData.getWidth();
        mHeight = gridData.getHeight();

        MLatLonBox latLonBox = gridData.getLatLonBox();
        mSurface.setSector(Sector.fromDegrees(
                latLonBox.getSouthWest().getLatitude(),
                latLonBox.getNorthEast().getLatitude(),
                latLonBox.getSouthWest().getLongitude(),
                latLonBox.getNorthEast().getLongitude()
        ));

        mSurface.setDimensions(mWidth, mHeight);
        mDimension = new Dimension(mWidth, mHeight);

        if (mDynamicMinMax) {
            mMin = gridData.getMin();
            mMax = gridData.getMax();
        }

        ArrayList<AnalyticSurface.GridPointAttributes> gridPointAttributeses = new ArrayList<>();
        double[] values = gridData.getGridAggregates();

        for (int i = 0; i < values.length; i++) {
            gridPointAttributeses.add(createColorGradientAttributes(i, values, mMin, mMax));
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

    public void setMaxHue(double maxHue) {
        mMaxHue = maxHue;
    }

    public void setMinHue(double minHue) {
        mMinHue = minHue;
    }

    public void setNullOpacity(double nullOpacity) {
        mNullOpacity = nullOpacity;
    }

    public void setZeroOpacity(double zeroOpacity) {
        mZeroOpacity = zeroOpacity;
    }

    public void setZeroValueSearchRange(int zeroValueSearchRange) {
        mZeroValueSearchRange = zeroValueSearchRange;
    }

    private GridPointAttributes createColorGradientAttributes(final int pos, double[] values, double minValue, double maxValue) {
        double value = values[pos];
        double hueFactor = WWMath.computeInterpolationFactor(value, minValue, maxValue);
        double opacity = 1.0;
        Color color = Color.getHSBColor((float) WWMath.mixSmooth(hueFactor, mMinHue, mMaxHue), 1f, 1f);

        if (value == 0) {
            Point valuePoint = MathHelper.indexToPoint(pos, mDimension);
            opacity = mNullOpacity;
            someValueFound:
            for (int x = -mZeroValueSearchRange; x < mZeroValueSearchRange + 1; x++) {
                for (int y = -mZeroValueSearchRange; y < mZeroValueSearchRange + 1; y++) {
                    Point neighborPoint = new Point(valuePoint.x + x, valuePoint.y + y);
                    if (neighborPoint.x > -1 && neighborPoint.x < mWidth) {
                        int idx = MathHelper.pointToIndex(neighborPoint, mDimension);
                        try {
                            if (values[idx] != 0) {
                                opacity = mZeroOpacity;
                                break someValueFound;
                            }
                        } catch (ArrayIndexOutOfBoundsException e) {
                            //
                        }
                    }
                }
            }
        }

        Color rgbaColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int) (255 * opacity));

        return AnalyticSurface.createGridPointAttributes(value, rgbaColor);
    }
}
