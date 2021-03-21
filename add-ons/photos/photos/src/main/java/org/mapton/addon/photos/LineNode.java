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
package org.mapton.addon.photos;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author Patrik Karlström
 */
public class LineNode {

    private static final SimpleDateFormat sNameDateFormat = new SimpleDateFormat("yyyyMMdd HHmmss");

    private Date mDate;
    private double mLat;
    private double mLon;

    public static String getName(ArrayList<LineNode> previousNodes, ArrayList<LineNode> nodes) {
        String name = String.format("%s_%s",
                sNameDateFormat.format(previousNodes.get(previousNodes.size() - 1).getDate()),
                sNameDateFormat.format(nodes.get(0).getDate()));

        return name;
    }

    public static String getName(ArrayList<LineNode> nodes) {
        String name = String.format("%s_%s",
                sNameDateFormat.format(nodes.get(0).getDate()),
                sNameDateFormat.format(nodes.get(nodes.size() - 1).getDate()));

        return name;
    }

    public LineNode(Date date, double lat, double lon) {
        mDate = date;
        mLat = lat;
        mLon = lon;
    }

    public Date getDate() {
        return mDate;
    }

    public double getLat() {
        return mLat;
    }

    public double getLon() {
        return mLon;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    public void setLat(double lat) {
        mLat = lat;
    }

    public void setLon(double lon) {
        mLon = lon;
    }
}
