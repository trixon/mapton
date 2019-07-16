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

import java.util.ArrayList;

/**
 *
 * @author Patrik Karlström
 */
public class GridData {

    private double mMax;
    private double mMin;
    private ArrayList<GridValue> mValues;

    public GridData() {
    }

    public GridData(double min, double max, ArrayList<GridValue> values) {
        mMin = min;
        mMax = max;
        mValues = values;
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

    public void setMax(double max) {
        mMax = max;
    }

    public void setMin(double min) {
        mMin = min;
    }

    public void setValues(ArrayList<GridValue> values) {
        mValues = values;
    }
}
