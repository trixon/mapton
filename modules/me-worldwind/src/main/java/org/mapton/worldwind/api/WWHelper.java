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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.render.PointPlacemarkAttributes;

/**
 *
 * @author Patrik Karlström
 */
public class WWHelper {

    public static final String KEY_FAST_OPEN = "mapton.layer.fast_open";
    public static final String KEY_HIDE_FROM_LAYER_MANAGER = "mapton.layer.hide";
    public static final String KEY_RUNNABLE_HOOVER = "mapton.runnable.hoover";
    public static final String KEY_HOOVER_TEXT = "mapton.hoover.text";
    public static final String KEY_RUNNABLE_LEFT_CLICK = "mapton.runnable.left_click";
    public static final String KEY_RUNNABLE_LEFT_DOUBLE_CLICK = "mapton.runnable.left_double_click";

    public static PointPlacemarkAttributes createHighlightAttributes(PointPlacemarkAttributes attrs, double scale) {
        PointPlacemarkAttributes highlightAttrs = new PointPlacemarkAttributes(attrs);
        highlightAttrs.setScale(attrs.getScale() * scale);
        highlightAttrs.setLabelScale(attrs.getLabelScale() * scale);

        return highlightAttrs;
    }
}
