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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.shared.ColorBy;
import static org.mapton.butterfly_topo.shared.ColorBy.FREQUENCY;
import static org.mapton.butterfly_topo.shared.ColorBy.MEAS_NEED;
import static org.mapton.butterfly_topo.shared.ColorBy.STYLE;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoAttributeManager {

    private BasicShapeAttributes[] mBearingAttributes;
    private ColorBy mColorBy;
    private BasicShapeAttributes[][] mComponentCircle1dAttributes;
    private BasicShapeAttributes mComponentGroundPathAttributes;
    private BasicShapeAttributes[][] mComponentTrace1dAttributes;
    private BasicShapeAttributes[] mComponentVector12dAttributes;
    private BasicShapeAttributes[] mComponentVector3dAttributes;
    private BasicShapeAttributes[] mComponentVectorCurrentAttributes;
    private BasicShapeAttributes mComponentZeroAttributes;
    private BasicShapeAttributes mIndicatorConnectorAttributes;
    private BasicShapeAttributes[] mIndicatorNeedAttributes;
    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private PointPlacemarkAttributes[] mPinAttributes;
    private BasicShapeAttributes mSkipPlotAttribute;
    private BasicShapeAttributes[] mSymbolAttributes;
    private TopoConfig mTopoConfig;
    private BasicShapeAttributes mTraceAttribute;
    private BasicShapeAttributes[] mVectorAlarmAttributes;

    public static TopoAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private TopoAttributeManager() {
        initAttributes();
    }

    public BasicShapeAttributes getBearingAttribute(boolean first) {
        if (mBearingAttributes == null) {
            mBearingAttributes = new BasicShapeAttributes[2];
            for (int i = 0; i < 2; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(true);
                attrs.setOutlineWidth(4);
                attrs.setOutlineMaterial(Material.MAGENTA);
                attrs.setOutlineWidth(i == 0 ? 2.0 : 4.0);
                attrs.setOutlineOpacity(i == 0 ? 1.0 : 0.1);
                mBearingAttributes[i] = attrs;
            }
        }

        return mBearingAttributes[first ? 0 : 1];
    }

    public ColorBy getColorBy() {
        return mColorBy;
    }

    public BasicShapeAttributes getComponentCircle1dAttributes(BTopoControlPoint p, int alarmLevel, boolean rise, boolean maximus) {
        if (mComponentCircle1dAttributes == null) {
            mComponentCircle1dAttributes = new BasicShapeAttributes[5][2];

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    Material material;
                    if (i < 4) {
                        material = ButterflyHelper.getAlarmMaterial(i - 1);
                    } else {
                        material = new Material(Color.decode("#800080"));
                    }
                    attrs.setInteriorMaterial(material);
                    attrs.setEnableLighting(true);
                    attrs.setInteriorOpacity(0.85);

                    if (j == 1) {
                        attrs.setDrawOutline(true);
                        if (i < 4) {
                            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
                        } else {
                            attrs.setOutlineMaterial(Material.YELLOW);
                        }
                    }

                    mComponentCircle1dAttributes[i][j] = attrs;
                }
            }
        }

        int offset = 1;
        if (maximus) {
            offset++;
        }
        var i = alarmLevel + offset;
        var j = rise ? 1 : 0;

        return mComponentCircle1dAttributes[i][j];
    }

    public BasicShapeAttributes getComponentGroundPathAttributes() {
        if (mComponentGroundPathAttributes == null) {
            mComponentGroundPathAttributes = new BasicShapeAttributes();
            mComponentGroundPathAttributes.setDrawOutline(true);
            mComponentGroundPathAttributes.setOutlineMaterial(Material.LIGHT_GRAY);
            mComponentGroundPathAttributes.setEnableLighting(false);
            mComponentGroundPathAttributes.setOutlineWidth(1);
        }

        return mComponentGroundPathAttributes;
    }

    public BasicShapeAttributes getComponentTrace1dAttributes(int alarmLevel, boolean rise, boolean maximus) {
        if (mComponentTrace1dAttributes == null) {
            mComponentTrace1dAttributes = new BasicShapeAttributes[5][2];

            for (int i = 0; i < 5; i++) {
                for (int j = 0; j < 2; j++) {
                    var attrs = new BasicShapeAttributes();
                    attrs.setDrawOutline(false);
                    Material material;
                    if (i < 4) {
                        material = ButterflyHelper.getAlarmMaterial(i - 1);
                    } else {
                        material = new Material(Color.decode("#800080"));
                    }
                    attrs.setInteriorMaterial(material);
                    attrs.setEnableLighting(true);

                    if (j == 1) {
                        attrs.setDrawOutline(true);
                        if (i < 4) {
                            attrs.setOutlineMaterial(Material.LIGHT_GRAY);
                        } else {
                            attrs.setOutlineMaterial(Material.YELLOW);
                        }
                    }

                    mComponentTrace1dAttributes[i][j] = attrs;
                }
            }
        }

        int offset = 1;
        if (maximus) {
            offset++;
        }
        var i = alarmLevel + offset;
        var j = rise ? 1 : 0;

        return mComponentTrace1dAttributes[i][j];
    }

    public BasicShapeAttributes getComponentMeasurementsAttributes(BTopoControlPoint p) {
        var attrs = new BasicShapeAttributes();
        attrs.setDrawOutline(false);
        attrs.setEnableLighting(true);
        Material material;
        switch (p.getDimension()) {
            case _1d ->
                material = Material.CYAN;
            case _2d ->
                material = Material.PINK;
            case _3d ->
                material = Material.MAGENTA;
            default ->
                throw new AssertionError();
        }
        attrs.setInteriorMaterial(material);
        return attrs;
    }

    public BasicShapeAttributes getComponentVector1dAttributes(BTopoControlPoint p) {
        return mComponentVector12dAttributes[TopoHelper.getAlarmLevelHeight(p) + 1];
    }

    public BasicShapeAttributes getComponentVector2dAttributes(BTopoControlPoint p) {
        return mComponentVector12dAttributes[TopoHelper.getAlarmLevelPlane(p) + 1];
    }

    public BasicShapeAttributes getComponentVector3dAttributes(BTopoControlPoint p) {
        if (mComponentVector3dAttributes == null) {
            mComponentVector3dAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(true);
                attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(false);
                attrs.setOutlineWidth(4);
                mComponentVector3dAttributes[i] = attrs;
            }
        }

        return mComponentVector3dAttributes[TopoHelper.getAlarmLevel(p) + 1];
    }

    public BasicShapeAttributes getComponentVectorAlarmAttributes(int level) {
        if (mVectorAlarmAttributes == null) {
            mVectorAlarmAttributes = new BasicShapeAttributes[3];

            for (int i = 0; i < 3; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(false);
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                attrs.setInteriorOpacity(0.5);
                mVectorAlarmAttributes[i] = attrs;
            }
        }

        return mVectorAlarmAttributes[level + 1];
    }

    public BasicShapeAttributes getComponentVectorCurrentAttributes(BTopoControlPoint p) {
        if (mComponentVectorCurrentAttributes == null) {
            mComponentVectorCurrentAttributes = new BasicShapeAttributes[4];

            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setDrawOutline(false);
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                attrs.setInteriorOpacity(0.75);
                mComponentVectorCurrentAttributes[i] = attrs;
            }
        }

        return mComponentVectorCurrentAttributes[TopoHelper.getAlarmLevel(p) + 1];
    }

    public BasicShapeAttributes getComponentZeroAttributes() {
        if (mComponentZeroAttributes == null) {
            mComponentZeroAttributes = new BasicShapeAttributes();
            mComponentZeroAttributes.setDrawOutline(false);
            mComponentZeroAttributes.setInteriorMaterial(Material.LIGHT_GRAY);
            mComponentZeroAttributes.setEnableLighting(true);
        }

        return mComponentZeroAttributes;
    }

    public BasicShapeAttributes getIndicatorConnectorAttribute() {
        if (mIndicatorConnectorAttributes == null) {
            mIndicatorConnectorAttributes = new BasicShapeAttributes();
            mIndicatorConnectorAttributes.setOutlineMaterial(Material.DARK_GRAY);
            mIndicatorConnectorAttributes.setOutlineWidth(2.0);
        }

        return mIndicatorConnectorAttributes;
    }

    public BasicShapeAttributes[] getIndicatorNeedAttributes() {
        if (mIndicatorNeedAttributes == null) {
            var indicatorNeed = new BasicShapeAttributes();
            var indicatorNeed0 = new BasicShapeAttributes(indicatorNeed);
            var indicatorNeed1 = new BasicShapeAttributes(indicatorNeed);
            var indicatorNeed2 = new BasicShapeAttributes(indicatorNeed);
            indicatorNeed0.setInteriorMaterial(Material.GREEN);
            indicatorNeed1.setInteriorMaterial(Material.ORANGE);
            indicatorNeed2.setInteriorMaterial(Material.RED);

            mIndicatorNeedAttributes = new BasicShapeAttributes[]{
                indicatorNeed0,
                indicatorNeed1,
                indicatorNeed2
            };

        }
        return mIndicatorNeedAttributes;
    }

    public PointPlacemarkAttributes getLabelPlacemarkAttributes() {
        if (mLabelPlacemarkAttributes == null) {
            mLabelPlacemarkAttributes = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            mLabelPlacemarkAttributes.setLabelScale(1.6);
            mLabelPlacemarkAttributes.setImageColor(GraphicsHelper.colorAddAlpha(Color.RED, 0));
            mLabelPlacemarkAttributes.setScale(0.75);
            mLabelPlacemarkAttributes.setImageAddress("images/pushpins/plain-white.png");
        }

        return mLabelPlacemarkAttributes;
    }

    public PointPlacemarkAttributes getPinAttributes(BTopoControlPoint p) {
        if (mPinAttributes == null) {
            mPinAttributes = new PointPlacemarkAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
                attrs.setScale(0.75);
                attrs.setImageAddress("images/pushpins/plain-white.png");
                attrs.setImageColor(ButterflyHelper.getAlarmColorAwt(i - 1));

                mPinAttributes[i] = attrs;
            }
        }

        var attrs = mPinAttributes[TopoHelper.getAlarmLevel(p) + 1];

        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageColor(getColor(p));
        }

        return attrs;
    }

    public BasicShapeAttributes getSkipPlotAttribute() {
        if (mSkipPlotAttribute == null) {
            mSkipPlotAttribute = new BasicShapeAttributes();
            mSkipPlotAttribute.setInteriorMaterial(new Material(Color.decode("#800080")));
            mSkipPlotAttribute.setEnableLighting(true);
            mSkipPlotAttribute.setDrawOutline(false);
        }

        return mSkipPlotAttribute;
    }

    public BasicShapeAttributes getSymbolAttributes(BTopoControlPoint p) {
        if (mSymbolAttributes == null) {
            mSymbolAttributes = new BasicShapeAttributes[4];
            for (int i = 0; i < 4; i++) {
                var attrs = new BasicShapeAttributes();
                attrs.setInteriorMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
                attrs.setEnableLighting(true);
                attrs.setDrawOutline(false);
                mSymbolAttributes[i] = attrs;
            }
        }

        var attrs = mSymbolAttributes[TopoHelper.getAlarmLevel(p) + 1];
        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
            attrs = new BasicShapeAttributes(attrs);
            attrs.setInteriorMaterial(new Material(getColor(p)));
        }

        return attrs;
    }

    public BasicShapeAttributes getTraceAttribute() {
        if (mTraceAttribute == null) {
            mTraceAttribute = new BasicShapeAttributes();
            mTraceAttribute.setDrawOutline(true);
            mTraceAttribute.setOutlineMaterial(Material.RED);
            mTraceAttribute.setEnableLighting(false);
            mTraceAttribute.setOutlineWidth(0.6);
        }

        return mTraceAttribute;
    }

    public void setColorBy(ColorBy colorBy) {
        mColorBy = colorBy;
    }

    private Color getColor(BTopoControlPoint p) {
        switch (mColorBy) {
            case STYLE -> {
                if (mTopoConfig == null) {
                    //TODO Make proper fix
                    mTopoConfig = new TopoConfig();
                }
                return mTopoConfig.getColor(p);
            }
            case FREQUENCY -> {
                return getColorForFrequency(p);
            }
            case MEAS_NEED -> {
                return getColorForMeasNeed(p);
            }
            case ORIGIN -> {
                return getColorForOrigin(p);
            }
            case SPEED -> {
                return getColorForSpeed(p);
            }
            default ->
                throw new AssertionError();
        }
    }

    private Color getColorForFrequency(BTopoControlPoint p) {
        Color color;

        var f = p.getFrequency();
        if (f == null || f == 0) {
            color = Color.WHITE;
        } else if (f == 1) {
            color = Color.RED;
        } else if (f <= 7) {
            color = Color.ORANGE;
        } else if (f <= 28) {
            color = Color.YELLOW;
        } else if (f <= 365) {
            color = Color.GREEN;
        } else {
            color = Color.BLACK;
        }

        return color;
    }

    private Color getColorForMeasNeed(BTopoControlPoint p) {
        var frequency = p.getFrequency();
        var latest = p.getDateLatest() != null ? p.getDateLatest().toLocalDate() : LocalDate.MIN;
        var today = LocalDate.now();
        var nextMeas = latest.plusDays(frequency);
        var remainingDays = p.ext().getMeasurementUntilNext(ChronoUnit.DAYS);

        Color color;

        if (frequency == null || frequency == 0) {
            color = Color.WHITE;
        } else if (remainingDays < 0) {
            color = Color.RED;
        } else if (remainingDays <= 7) {
            color = Color.ORANGE;
        } else if (remainingDays <= 28) {
            color = Color.YELLOW;
        } else if (remainingDays <= 365) {
            color = Color.GREEN;
        } else {
            color = Color.BLACK;
        }

        return color;
    }

    private Color getColorForOrigin(BTopoControlPoint p) {
        var colors = new Color[]{
            Color.WHITE,
            Color.CYAN,
            Color.GREEN,
            Color.YELLOW,
            Color.MAGENTA,
            Color.PINK,
            Color.ORANGE,
            Color.RED,
            Color.DARK_GRAY,
            Color.GRAY,
            Color.LIGHT_GRAY,
            Color.BLACK,
            Color.BLUE
        };

        ArrayList<String> origins = TopoManager.getInstance().getValue("origins");
        var index = Math.max(0, origins.indexOf(p.getOrigin()));
        index = Math.min(colors.length - 1, index);

        return colors[index];
    }

    private Color getColorForSpeed(BTopoControlPoint p) {
        return TopoHelper.getSpeedColor(p);
    }

    private void initAttributes() {
        mComponentVector12dAttributes = new BasicShapeAttributes[4];

        for (int i = 0; i < 4; i++) {
            var attrs = new BasicShapeAttributes();
            attrs.setDrawOutline(true);
            attrs.setOutlineWidth(4);
            attrs.setOutlineMaterial(ButterflyHelper.getAlarmMaterial(i - 1));
            attrs.setOutlineOpacity(0.4);
            mComponentVector12dAttributes[i] = attrs;
        }
    }

    private static class Holder {

        private static final TopoAttributeManager INSTANCE = new TopoAttributeManager();
    }
}
