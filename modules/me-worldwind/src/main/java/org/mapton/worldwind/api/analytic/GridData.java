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

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.DoubleStream;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;

/**
 *
 * @author Patrik Karlström
 */
public class GridData {

    private ArrayList<Double>[][] mCellValues;
    private int mHeight;
    private MLatLonBox mLatLonBox;
    private double mMax = Double.MIN_VALUE;
    private double mMin = Double.MAX_VALUE;
    private ArrayList<GridValue> mValues;
    private int mWidth;
//    private Table<Integer, Integer, ArrayList<Double>> mCellTable = HashBasedTable.create();

    public GridData() {
    }

    public GridData(int width, int height, ArrayList<GridValue> values) {
        //TODO Replace width & height with some calculated resulotion variant...?
        ArrayList<MLatLon> latLons = new ArrayList<>();
        values.forEach((gridValue) -> {
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
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                mCellValues[i][j] = new ArrayList<>();
            }
        }

        setValues(values);
    }

    public Double getCellAverage(Point p) {
        return getCellDoubles(p).average().orElse(0);
    }

    public Double getCellMax(Point p) {
        return getCellDoubles(p).max().orElse(0);
    }

    public Double getCellMedian(Point p) {
        List<Double> list = getCellValues(p);
        DoubleStream sortedValues = list.stream().mapToDouble(Double::doubleValue).sorted();

        double median = list.size() % 2 == 0
                ? sortedValues.skip(list.size() / 2 - 1).limit(2).average().getAsDouble()
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
//        if (!mCellTable.contains(p.y, p.x)) {
//            mCellTable.put(p.y, p.x, new ArrayList<>());
//        }
        return mCellValues[p.x][p.y];
    }

    public ArrayList<Double> getCellValues(int col, int row) {
        return mCellValues[col][row];
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

        for (GridValue value : values) {
            Point p = getCellPoint(value.getLatLon());
            getCellValues(p).add(value.getValue());
        }
    }

    private DoubleStream getCellDoubles(Point p) {
        return getCellValues(p).stream().mapToDouble(Double::doubleValue);
    }

    private Point getCellPoint(MLatLon latLon) {
        double deltaLat = latLon.getLatitude() - mLatLonBox.getSouthWest().getLatitude();
        double deltaLon = latLon.getLongitude() - mLatLonBox.getSouthWest().getLongitude();

        int y = (int) (deltaLat / (mLatLonBox.getLatitudeSpan() / mHeight));
        int x = (int) (deltaLon / (mLatLonBox.getLongitudeSpan() / mWidth));

        return new Point(x, y);
    }
}
