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
package org.mapton.butterfly_activities;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.awt.Color;
import java.util.HashMap;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.BAreaActivity.BAreaStatus;
import se.trixon.almond.util.GraphicsHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ActAttributeManager {

    private PointPlacemarkAttributes mLabelPlacemarkAttributes;
    private final HashMap<BAreaStatus, PointPlacemarkAttributes> mStatusToPointPlacemarkAttributes = new HashMap<>();
    private final HashMap<BAreaStatus, BasicShapeAttributes> mStatusToShapeAttributes = new HashMap<>();
    private final HashMap<BAreaStatus, BasicShapeAttributes> mStatusToShapeHighlightAttributes = new HashMap<>();

    public static ActAttributeManager getInstance() {
        return Holder.INSTANCE;
    }

    private ActAttributeManager() {
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

    public PointPlacemarkAttributes getPinAttributes(BAreaActivity a) {
        return mStatusToPointPlacemarkAttributes.computeIfAbsent(a.getStatus(), k -> {
            var attrs = new PointPlacemarkAttributes(new PointPlacemark(Position.ZERO).getDefaultAttributes());
            attrs.setScale(0.75);
            attrs.setImageAddress("images/pushpins/plain-white.png");
            switch (a.getStatus()) {
                case OTHER ->
                    attrs.setImageColor(Color.GRAY);
                case INFORMATION ->
                    attrs.setImageColor(Color.GREEN);
                case TRIGGER ->
                    attrs.setImageColor(Color.RED);
                default ->
                    attrs.setImageColor(Color.DARK_GRAY);
            }

            return attrs;
        });
    }

    public BasicShapeAttributes getSurfaceAttributes(BAreaActivity a) {
        return mStatusToShapeAttributes.computeIfAbsent(a.getStatus(), k -> {
            var attrs = new BasicShapeAttributes();
            attrs.setOutlineWidth(3.0);
            attrs.setDrawInterior(true);
            attrs.setDrawOutline(true);
            attrs.setInteriorOpacity(0.1);

            Material material;
            switch (a.getStatus()) {
                case OTHER ->
                    material = Material.GRAY;
                case INFORMATION ->
                    material = Material.GREEN;
                case TRIGGER ->
                    material = Material.RED;
                default ->
                    material = Material.DARK_GRAY;
            }

            attrs.setInteriorMaterial(material);
            attrs.setOutlineMaterial(material);

            return attrs;
        });
    }

    public BasicShapeAttributes getSurfaceHighlightAttributes(BAreaActivity a) {
        return mStatusToShapeHighlightAttributes.computeIfAbsent(a.getStatus(), k -> {
            var attrs = new BasicShapeAttributes(getSurfaceAttributes(a));
            attrs.setInteriorOpacity(0.20);
            attrs.setOutlineOpacity(0.20);

            return attrs;
        });

    }

    private static class Holder {

        private static final ActAttributeManager INSTANCE = new ActAttributeManager();
    }
}
