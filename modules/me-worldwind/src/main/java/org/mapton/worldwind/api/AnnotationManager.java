/*
 * Copyright 2025 Patrik Karlström <patrik@trixon.se>.
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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.GlobeAnnotation;
import java.awt.Color;
import java.awt.Font;
import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import org.mapton.api.MAnnotation;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MKey;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.mapton.worldwind.AnnotationLimitMode;
import org.mapton.worldwind.AnnotationsOptions;
import se.trixon.almond.util.CollectionHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class AnnotationManager extends MBaseDataManager<GlobeAnnotation> {

    public static final String KEY_CAT = "annotation.category";
    private final HashMap<Class, AnnotationAttributes> mCategoryClassToAttribute = new HashMap<>();
    private AnnotationAttributes mDefaultAttributes = new AnnotationAttributes();
    private final AnnotationsOptions mOptions = AnnotationsOptions.getInstance();

    public static AnnotationManager getInstance() {
        return Holder.INSTANCE;
    }

    private AnnotationManager() {
        super(GlobeAnnotation.class);

        mDefaultAttributes.setCornerRadius(SwingHelper.getUIScaled(8));
        mDefaultAttributes.setBackgroundColor(new Color(0f, 0f, 0f, .75f));
        mDefaultAttributes.setTextColor(Color.WHITE);
        mDefaultAttributes.setDistanceMinScale(.5);
        mDefaultAttributes.setDistanceMaxScale(2);
        mDefaultAttributes.setDistanceMinOpacity(.5);
        mDefaultAttributes.setLeaderGapWidth(SwingHelper.getUIScaled(16));
        mDefaultAttributes.setDrawOffset(new Point(SwingHelper.getUIScaled(30), SwingHelper.getUIScaled(60)));
        mDefaultAttributes.setInsets(SwingHelper.getUIScaledInsets(6, 12, 12, 12));
        mDefaultAttributes.setFont(Font.decode("Dialog-PLAIN-%d".formatted(SwingHelper.getUIScaled(14))));
        mDefaultAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        mDefaultAttributes.setSize(SwingHelper.getUIScaledDim(800, 0));

        Mapton.getGlobalState().addListener(gsce -> {
            var annotation = gsce.<MAnnotation>getValue();
            if (annotation == null) {
                getAllItems().clear();
            } else {
                add(annotation);
            }
        }, MKey.ANNOTATIONS);

        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            trimIfNeeded();
        });
    }

    public void add(MAnnotation annotation) {
        add(annotation.getLatLon(), annotation.getText(), annotation.getCategory());
    }

    /**
     * Here is the real add method
     *
     * @param annotation
     */
    public void add(GlobeAnnotation annotation) {
        var validForAdd = !getAllItems().stream()
                .anyMatch(a -> a.getPosition().equals(annotation.getPosition()));

        if (validForAdd) {
            annotation.setAlwaysOnTop(true);
            annotation.setAttributes(mCategoryClassToAttribute.getOrDefault(annotation.getValue(KEY_CAT), mDefaultAttributes));
            getAllItems().add(annotation);
            trimIfNeeded();

            var delay = mOptions.getTimeout().getDelay();
            if (delay > 0) {
                FxHelper.runLaterDelayed(delay, () -> {
                    try {
                        getAllItems().remove(annotation);
                    } catch (Exception e) {
                        //nvm it might be gone already
                    }
                });
            }
        }
    }

    public void add(MLatLon latLon, String text, Class category) {
        add(WWHelper.positionFromLatLon(latLon), text, category);
    }

    public void add(Position position, String text, Class category) {
        var annotation = new GlobeAnnotation(text, position);
        annotation.setValue(KEY_CAT, category);
        add(annotation);
    }

    public AnnotationAttributes createAttributes() {
        var attrs = new AnnotationAttributes();
        attrs.setDefaults(mDefaultAttributes);

        return attrs;
    }

    public AnnotationAttributes getDefaultAttributes() {
        return mDefaultAttributes;
    }

    public void putAttributes(Class category, AnnotationAttributes attributes) {
        mCategoryClassToAttribute.put(category, attributes);
    }

    public void setDefaultAttributes(AnnotationAttributes defaultAttributes) {
        mDefaultAttributes = defaultAttributes;
    }

    @Override
    protected void applyTemporalFilter() {
    }

    @Override
    protected void load(ArrayList<GlobeAnnotation> items) {
    }

    private void trimIfNeeded() {
        var items = getAllItems();
        var limit = mOptions.getLimit();

        if (mOptions.getLimitMode() == AnnotationLimitMode.TOTAL) {
            if (items.size() > limit) {
                items.remove(0, items.size() - limit);
            }
        } else {
            var map = new HashMap<String, Integer>();
            var itemsToRemove = items.reversed().stream()
                    .filter(item -> CollectionHelper.incInteger(map, item.getValue(KEY_CAT).toString()) > limit)
                    .toList();

            items.removeAll(itemsToRemove);
        }
    }

    private static class Holder {

        private static final AnnotationManager INSTANCE = new AnnotationManager();
    }
}
