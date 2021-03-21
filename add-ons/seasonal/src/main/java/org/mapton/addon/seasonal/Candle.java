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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.ShapeAttributes;
import gov.nasa.worldwind.render.airspaces.CappedCylinder;
import java.awt.Color;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.RandomUtils;
import org.mapton.api.MLatLon;

/**
 *
 * @author Patrik Karlström
 */
public class Candle {

    private ShapeAttributes mBaseAttributes;
    private CappedCylinder mBaseCylinder;
    private ShapeAttributes mConeAttributes;
    private final double mHeight;
    private final MLatLon mLatLon;
    private boolean mLit;
    private final ArrayList<Renderable> mRenderables = new ArrayList<>();
    private final LocalDateTime mStartDateTime;
    private Cone mTopCone;
    private CappedCylinder mWickCylinder;
    private ShapeAttributes mWickLitAttributes;
    private ShapeAttributes mWickUnlitAttributes;
    private final double mWidth;

    public Candle(MLatLon latLon, LocalDateTime startDateTime, double height, double width) {
        mLatLon = latLon;
        mStartDateTime = startDateTime;
        mHeight = height;
        mWidth = width;

        initAttributes();
    }

    public ArrayList<Renderable> getRenderables() {
        var now = LocalDateTime.now();
        mLit = now.isAfter(mStartDateTime);

        var minutes = ChronoUnit.MINUTES.between(mStartDateTime, now);
        double periodMinutes = 60 * 24 * 26;

        var height = mHeight;
        if (minutes > 0) {
            double quota = minutes / periodMinutes;
            height = mHeight - mHeight * .9 * quota;
        }

        mRenderables.clear();
        if (height > 0) {
            createObjects(height);

            mRenderables.add(mBaseCylinder);
            mRenderables.add(mWickCylinder);

            if (mLit) {
                mRenderables.add(mTopCone);
            }
        }

        return mRenderables;
    }

    private void createObjects(double height) {
        var position = Position.fromDegrees(mLatLon.getLatitude(), mLatLon.getLongitude());

        mBaseCylinder = new CappedCylinder(position, mWidth);
        mBaseCylinder.setAltitudes(0, height);
        mBaseCylinder.setAttributes(mBaseAttributes);

        mWickCylinder = new CappedCylinder(position, mWidth * .1);
        final var wickHeight = 8;
        mWickCylinder.setAltitudes(height, height + wickHeight);

        if (mLit) {
            mWickCylinder.setAttributes(mWickLitAttributes);
        } else {
            mWickCylinder.setAttributes(mWickUnlitAttributes);
        }

        var offset = 0.000008;
        final var coneHeight = RandomUtils.nextInt(23, 28);
        final var wickPosition = Position.fromDegrees(
                mLatLon.getLatitude() + RandomUtils.nextDouble(0, offset) * (RandomUtils.nextBoolean() ? 1 : -1),
                mLatLon.getLongitude() + RandomUtils.nextDouble(0, offset) * (RandomUtils.nextBoolean() ? 1 : -1),
                height + wickHeight * .7 + coneHeight * .5
        );

        mTopCone = new Cone(wickPosition, coneHeight, mWidth * .6);
        mTopCone.setAttributes(mConeAttributes);
    }

    private void initAttributes() {
        mBaseAttributes = new BasicShapeAttributes();
        mBaseAttributes.setInteriorMaterial(new Material(Color.red.darker()));
        mBaseAttributes.setDrawOutline(false);
        mBaseAttributes.setEnableLighting(true);

        mConeAttributes = new BasicShapeAttributes(mBaseAttributes);
        mConeAttributes.setDrawOutline(false);
        mConeAttributes.setInteriorMaterial(Material.YELLOW);
        mConeAttributes.setInteriorOpacity(0.6);

        mWickUnlitAttributes = new BasicShapeAttributes(mBaseAttributes);
        mWickUnlitAttributes.setInteriorMaterial(Material.WHITE);
        mWickLitAttributes = new BasicShapeAttributes(mWickUnlitAttributes);
        mWickLitAttributes.setInteriorMaterial(Material.BLACK);
    }
}
