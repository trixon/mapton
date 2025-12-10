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
package org.mapton.butterfly_remote.insar;

import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import org.mapton.butterfly_core.api.BaseAttributeManager;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import static org.mapton.butterfly_remote.insar.ColorBy.DISPLACEMENT;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class InsarAttributeManager extends BaseAttributeManager {

    private ColorBy mColorBy;
    private BasicShapeAttributes mComponentEllipsoidAttributes;
    private BasicShapeAttributes mInsarAttribute;
    private BasicShapeAttributes mSurfaceAttributes;

    public static InsarAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private InsarAttributeManager() {
    }

    public javafx.scene.paint.Color getColorFx(BRemoteInsarPoint p) {
        return SwingHelper.colorToColor(getColor(p));
    }

    public String getValueByColorByWithHeader(BRemoteInsarPoint p) {
        var format = "%s: %+.1f %s";
        switch (mColorBy) {
            case ALARM, DISPLACEMENT -> {
                return format.formatted("Δ", p.ext().deltaZero().getDeltaZ() * 1000, "mm");
            }
            case VELOCITY -> {
                return format.formatted("Hastighet", p.getVelocity(), "mm/år");
            }
            case VELOCITY_3 -> {
                return format.formatted("Hastighet 3m", p.getVelocity3m(), "mm/år");
            }
            case VELOCITY_6 -> {
                return format.formatted("Hastighet 6m", p.getVelocity6m(), "mm/år");
            }
            case ACCELERATION -> {
                return format.formatted("Acceleration", p.getAcceleration(), "mm/år^2");
            }
            default ->
                throw new AssertionError();
        }

    }

    public String getValueByColorBy(BRemoteInsarPoint p) {
        var format = "%+.1f";
        switch (mColorBy) {
            case ALARM, DISPLACEMENT -> {
                return format.formatted(p.ext().deltaZero().getDeltaZ() * 1000);
            }
            case VELOCITY -> {
                return format.formatted(p.getVelocity());
            }
            case VELOCITY_3 -> {
                return format.formatted(p.getVelocity3m());
            }
            case VELOCITY_6 -> {
                return format.formatted(p.getVelocity6m());
            }
            case ACCELERATION -> {
                return format.formatted(p.getAcceleration());
            }
            default ->
                throw new AssertionError();
        }

    }

    public Color getColor(BRemoteInsarPoint p) {
        switch (mColorBy) {
            case ALARM -> {
                return InsarHelper.getAlarmColorAwt(p);
            }
            case DISPLACEMENT -> {
                return ButterflyHelper.getRangeColor(p.ext().deltaZero().getDeltaZ(), 0.01);
            }
            case VELOCITY -> {
                return ButterflyHelper.getRangeColor(p.getVelocity(), 10);
            }
            case VELOCITY_3 -> {
                return ButterflyHelper.getRangeColor(p.getVelocity3m(), 10);
            }
            case VELOCITY_6 -> {
                return ButterflyHelper.getRangeColor(p.getVelocity6m(), 10);
            }
            case ACCELERATION -> {
                return ButterflyHelper.getRangeColor(p.getAcceleration(), 10);
            }
            default ->
                throw new AssertionError();
        }
    }

    public ColorBy getColorBy() {
        return mColorBy;
    }

    public BasicShapeAttributes getComponentEllipsoidAttributes() {
        if (mComponentEllipsoidAttributes == null) {
            mComponentEllipsoidAttributes = new BasicShapeAttributes();
            mComponentEllipsoidAttributes.setDrawOutline(false);
            mComponentEllipsoidAttributes.setInteriorMaterial(Material.ORANGE);
            mComponentEllipsoidAttributes.setEnableLighting(true);
        }

        return mComponentEllipsoidAttributes;
    }

    public BasicShapeAttributes getInsarAttribute() {
        if (mInsarAttribute == null) {
            mInsarAttribute = new BasicShapeAttributes();
            mInsarAttribute.setDrawOutline(true);
            mInsarAttribute.setOutlineMaterial(Material.RED);
            mInsarAttribute.setOutlineWidth(4.0);
            mInsarAttribute.setOutlineOpacity(1.0);
        }

        return mInsarAttribute;
    }

    public PointPlacemarkAttributes getPinAttributes(BRemoteInsarPoint p) {
        var attrs = getPinAttributes(InsarHelper.getAlarmLevel(p));

        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
            attrs = new PointPlacemarkAttributes(attrs);
            attrs.setImageColor(getColor(p));
        }

        return attrs;
    }

    public BasicShapeAttributes getSurfaceAttributes() {
        if (mSurfaceAttributes == null) {
            mSurfaceAttributes = new BasicShapeAttributes();
            mSurfaceAttributes.setDrawOutline(false);
            mSurfaceAttributes.setDrawInterior(true);
            mSurfaceAttributes.setInteriorMaterial(Material.RED);
            mSurfaceAttributes.setEnableLighting(false);
        }

        return mSurfaceAttributes;
    }

    public BasicShapeAttributes getSymbolAttributes(BRemoteInsarPoint p) {
        var attrs = getAlarmInteriorAttributes(InsarHelper.getAlarmLevel(p));
        if (mColorBy != null && mColorBy != ColorBy.ALARM) {
            attrs = new BasicShapeAttributes(attrs);
            attrs.setInteriorMaterial(new Material(getColor(p)));
        }

        return attrs;
    }

    public void setColorBy(ColorBy colorBy) {
        mColorBy = colorBy;
    }

    private static class Holder {

        private static final InsarAttributeManager INSTANCE = new InsarAttributeManager();
    }
}
