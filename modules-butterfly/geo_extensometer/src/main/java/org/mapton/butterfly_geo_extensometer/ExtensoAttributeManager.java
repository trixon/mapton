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
package org.mapton.butterfly_geo_extensometer;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import java.awt.Color;
import org.mapton.butterfly_core.api.BaseAttributeManager;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoAttributeManager extends BaseAttributeManager {

    private static final Material[] mAlarmMaterials = new Material[]{
        Material.GREEN,
        Material.YELLOW,
        Material.ORANGE,
        Material.RED,
        new Material(Color.decode("#800080")),
        Material.BLUE
    };
    private BasicShapeAttributes[][] mComponentTrace1dAttributes;

    private BasicShapeAttributes mGroundConnectorAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;
    private BasicShapeAttributes[] mStationConnectorAttributes;
    private final Color[] mStationConnectorColors = new Color[]{Color.CYAN, Color.MAGENTA, Color.YELLOW, Color.BLACK};
    private BasicShapeAttributes mStationConnectorEllipsoidAttributes;
    private BasicShapeAttributes[] mVectorAlarmAttributes;

    public static ExtensoAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExtensoAttributeManager() {
    }

    public BasicShapeAttributes getComponentAlarmAttributes(int level) {
        if (mVectorAlarmAttributes == null) {
            mVectorAlarmAttributes = new BasicShapeAttributes[mAlarmMaterials.length];

            for (int i = 0; i < mAlarmMaterials.length; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(false);
                attrs.setInteriorMaterial(mAlarmMaterials[i]);
                attrs.setEnableLighting(true);
//                attrs.setInteriorOpacity(0.5);
                mVectorAlarmAttributes[i] = attrs;
            }
        }

        if (level == -1) {
            level = 5;
        }

        return mVectorAlarmAttributes[level];
    }

    public BasicShapeAttributes getComponentTraceAttributes(int level, boolean rise, boolean maximus) {
        if (mComponentTrace1dAttributes == null) {
            mComponentTrace1dAttributes = new BasicShapeAttributes[mAlarmMaterials.length][2];

            for (int i = 0; i < mAlarmMaterials.length; i++) {
                for (int j = 0; j < 2; j++) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    attrs.setInteriorMaterial(mAlarmMaterials[i]);
                    attrs.setEnableLighting(true);

                    if (j == 1) {
                        attrs.setDrawOutline(true);
                        if (i == 4) {
                            attrs.setOutlineMaterial(Material.YELLOW);
                        } else {
                            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
                        }
                    }

                    mComponentTrace1dAttributes[i][j] = attrs;
                }
            }
        }

        if (level == -1) {
            level = 5;
        }

        if (maximus) {
            level = 4;
        }

        var i = level;
        var j = rise ? 1 : 0;

        return mComponentTrace1dAttributes[i][j];
    }

    public BasicShapeAttributes getGroundConnectorAttributes() {
        if (mGroundConnectorAttributes == null) {
            mGroundConnectorAttributes = new BasicShapeAttributes();
            mGroundConnectorAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mGroundConnectorAttributes.setOutlineWidth(1.0);
        }

        return mGroundConnectorAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(int index) {
        index = Math.min(index, mStationConnectorColors.length - 1);

        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes[mStationConnectorColors.length];
            for (int i = 0; i < mPinAttributes.length; i++) {
                var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
                attrs.setScale(0.75);
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(mStationConnectorColors[i]);

                mPinAttributes[i] = attrs;
            }
        }

        if (index == -1) {//station
            var attrs = new PointPlacemarkAttributes(mPinAttributes[0]);
            attrs.setImageColor(Color.RED);
            return attrs;
        } else {
            return mPinAttributes[index];
        }
    }

    public BasicAirspaceAttributes getSliceAttributes(double quota) {
        var attrs = new BasicAirspaceAttributes();
        attrs.setEnableLighting(true);
        attrs.setDrawOutline(false);

        String rgb;
        if (quota == 0) {
            rgb = "ff0000";
        } else if (quota <= 0.1) {
            rgb = "FF7777";
        } else if (quota <= 0.2) {
            rgb = "fe4400";
        } else if (quota <= 0.3) {
            rgb = "f86600";
        } else if (quota <= 0.4) {
            rgb = "ee8200";
        } else if (quota <= 0.5) {
            rgb = "df9b00";
        } else if (quota <= 0.6) {
            rgb = "cdb200";
        } else if (quota <= 0.7) {
            rgb = "b6c700";
        } else if (quota <= 0.8) {
            rgb = "98db00";
        } else if (quota <= 0.9) {
            rgb = "6fed00";
        } else {
            rgb = "00ff00";
        }
        attrs.setInteriorMaterial(new Material(Color.decode("#" + rgb)));

        return attrs;
    }

    public ShapeAttributes getStationConnectorAttribute(int index) {
        index = Math.min(index, mStationConnectorColors.length - 1);

        if (mStationConnectorAttributes == null) {
            mStationConnectorAttributes = new BasicShapeAttributes[mStationConnectorColors.length];
            for (int i = 0; i < mStationConnectorAttributes.length; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setOutlineMaterial(new Material(mStationConnectorColors[i]));
                attrs.setOutlineWidth(2.0);
                mStationConnectorAttributes[i] = attrs;
            }
        }

        return mStationConnectorAttributes[index];
    }

    public BasicShapeAttributes getStationConnectorEllipsoidAttributes() {
        if (mStationConnectorEllipsoidAttributes == null) {
            mStationConnectorEllipsoidAttributes = new BasicShapeAttributes();
            mStationConnectorEllipsoidAttributes.setDrawOutline(false);
            mStationConnectorEllipsoidAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
            mStationConnectorEllipsoidAttributes.setEnableLighting(true);
        }

        return mStationConnectorEllipsoidAttributes;
    }

    public BasicShapeAttributes getStatusAttributes(double quota) {
        var attrs = new BasicShapeAttributes();
        attrs.setEnableLighting(true);
        attrs.setDrawOutline(false);

        String rgb;
        if (quota == 0) {
            rgb = "ff0000";
        } else if (quota <= 0.1) {
            rgb = "FF7777";
        } else if (quota <= 0.2) {
            rgb = "fe4400";
        } else if (quota <= 0.3) {
            rgb = "f86600";
        } else if (quota <= 0.4) {
            rgb = "ee8200";
        } else if (quota <= 0.5) {
            rgb = "df9b00";
        } else if (quota <= 0.6) {
            rgb = "cdb200";
        } else if (quota <= 0.7) {
            rgb = "b6c700";
        } else if (quota <= 0.8) {
            rgb = "98db00";
        } else if (quota <= 0.9) {
            rgb = "6fed00";
        } else {
            rgb = "00ff00";
        }
        attrs.setInteriorMaterial(new Material(Color.decode("#" + rgb)));

        return attrs;
    }

    private static class Holder {

        private static final ExtensoAttributeManager INSTANCE = new ExtensoAttributeManager();
    }
}
