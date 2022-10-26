/*
 * Copyright 2022 Patrik Karlström.
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

import gov.nasa.worldwind.util.BufferFactory;
import gov.nasa.worldwind.util.BufferWrapper;
import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.stream.DoubleStream;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GridData {

    private CellAggregate mCellAggregate;
    private ArrayList<Double>[][] mCellValues;
    private int mHeight;
    private MLatLonBox mLatLonBox;
    private double mMax = Double.MIN_VALUE;
    private double mMin = Double.MAX_VALUE;
    private ArrayList<GridValue> mValues;
    private int mWidth;

    public GridData() {
    }

    public GridData(int width, int height, ArrayList<GridValue> values, CellAggregate cellAggregate) {
        //TODO Replace width & height with some calculated resulotion variant...?
        mCellAggregate = cellAggregate;
        var latLons = new ArrayList<MLatLon>();

        values.forEach(gridValue -> {
            latLons.add(gridValue.getLatLon());
            if (gridValue.getValue() != null) {
                mMin = Math.min(mMin, gridValue.getValue());
                mMax = Math.max(mMax, gridValue.getValue());
            }
        });

        mLatLonBox = new MLatLonBox(latLons);
        mWidth = width;
        mHeight = height;

        mCellValues = new ArrayList[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                mCellValues[x][y] = new ArrayList<>();
            }
        }

        setValues(values);
    }

    public CellAggregate getCellAggregate() {
        return mCellAggregate;
    }

    public Double getCellAverage(Point p) {
        return getCellDoubles(p).average().orElse(0);
    }

    public int getCellCount(Point p) {
        return getCellValues(p).size();
    }

    public Double getCellMax(Point p) {
        return getCellDoubles(p).max().orElse(0);
    }

    public Double getCellMedian(Point p) {
        var list = getCellValues(p);
        if (list.isEmpty()) {
            return 0d;
        }
        var sortedValues = list.stream().mapToDouble(Double::doubleValue).sorted();

        double median = list.size() % 2 == 0
                ? sortedValues.skip(list.size() / 2 - 1L).limit(2).average().getAsDouble()
                : sortedValues.skip(list.size() / 2).findFirst().getAsDouble();

        return median;
    }

    public Double getCellMin(Point p) {
        return getCellDoubles(p).min().orElse(0);
    }

    public Double getCellSum(Point p) {
        return getCellDoubles(p).sum();
    }

    public ArrayList<Double> getCellValues(Point p) {
        return mCellValues[p.x][p.y];
    }

    public ArrayList<Double> getCellValues(int col, int row) {
        return mCellValues[col][row];
    }

    public double[] getGridAggregates() {
        return getGridAggregates(mCellAggregate);
    }

    public double[] getGridAggregates(CellAggregate cellAggregate) {
        var dimension = new Dimension(mWidth, mHeight);
        var values = new double[mWidth * mHeight];

        for (int x = 0; x < mWidth; x++) {
            for (int y = 0; y < mHeight; y++) {
                int valueIndex = MathHelper.pointToIndex(new Point(x, y), dimension);
                var cellPoint = new Point(x, mHeight - 1 - y);

                switch (cellAggregate) {
                    case AVG:
                        values[valueIndex] = getCellAverage(cellPoint);
                        break;

                    case COUNT:
                        values[valueIndex] = getCellCount(cellPoint);
                        break;

                    case MAX:
                        values[valueIndex] = getCellMax(cellPoint);
                        break;

                    case MEDIAN:
                        values[valueIndex] = getCellMedian(cellPoint);
                        break;

                    case MIN:
                        values[valueIndex] = getCellMin(cellPoint);
                        break;

                    case SUM:
                        values[valueIndex] = getCellSum(cellPoint);
                        break;

                    default:
                        throw new AssertionError();
                }
            }
        }

        return values;
    }

    public BufferWrapper getGridWrapperAverages() {
        var dimension = new Dimension(mWidth, mHeight);
        var values = new double[mWidth * mHeight];

        for (int x = 0; x < mWidth; x++) {
            for (int y = 0; y < mHeight; y++) {
                values[MathHelper.pointToIndex(new Point(x, y), dimension)] = getCellAverage(new Point(x, mHeight - 1 - y));
            }
        }

        var buffer = new BufferFactory.DoubleBufferFactory().newBuffer(values.length);
        buffer.putDouble(0, values, 0, values.length);

        return buffer;
    }

    public int getHeight() {
        return mHeight;
    }

    public MLatLonBox getLatLonBox() {
        return mLatLonBox;
    }

    public double getMax() {
        return mMax;
    }

    public double getMin() {
        return mMin;
    }

    public ArrayList<GridValue> getValues() {
        return mValues;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setCellAggregate(CellAggregate cellAggregate) {
        mCellAggregate = cellAggregate;
    }

    public void setLatLonBox(MLatLonBox latLonBox) {
        mLatLonBox = latLonBox;
    }

    public void setMax(double max) {
        mMax = max;
    }

    public void setMin(double min) {
        mMin = min;
    }

    public void setValues(ArrayList<GridValue> values) {
        mValues = values;

        for (var value : values) {
            var p = getCellPoint(value.getLatLon());
            getCellValues(p).add(value.getValue());
        }
    }

    private DoubleStream getCellDoubles(Point p) {
        return getCellValues(p).stream().mapToDouble(Double::doubleValue);
    }

    private Point getCellPoint(MLatLon latLon) {
        double deltaLat = latLon.getLatitude() - mLatLonBox.getSouthWest().getLatitude();
        double deltaLon = latLon.getLongitude() - mLatLonBox.getSouthWest().getLongitude();

        int xRaw = (int) (deltaLon / (mLatLonBox.getLongitudeSpan() / mWidth));
        int yRaw = (int) (deltaLat / (mLatLonBox.getLatitudeSpan() / mHeight));

        int xMax = Math.min(xRaw, mWidth - 1);
        int yMax = Math.min(yRaw, mHeight - 1);

        int x = Math.max(xMax, 0);
        int y = Math.max(yMax, 0);

        return new Point(x, y);
    }
}
