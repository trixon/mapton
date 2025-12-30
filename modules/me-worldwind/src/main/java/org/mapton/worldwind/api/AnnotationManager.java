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

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.GlobeAnnotation;
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

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class AnnotationManager extends MBaseDataManager<GlobeAnnotation> {

    public static final String KEY_CAT = "annotation.category";
    private final AnnotationsOptions mOptions = AnnotationsOptions.getInstance();

    public static AnnotationManager getInstance() {
        return Holder.INSTANCE;
    }

    private AnnotationManager() {
        super(GlobeAnnotation.class);

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

    public void add(GlobeAnnotation annotation) {
        var validForAdd = !getAllItems().stream()
                .anyMatch(a -> a.getPosition().equals(annotation.getPosition()));

        if (validForAdd) {
            getAllItems().add(annotation);
            trimIfNeeded();
        }
    }

    public void add(MLatLon latLon, String text, String category) {
        add(WWHelper.positionFromLatLon(latLon), text, category);
    }

    public void add(Position position, String text, String category) {
        var annotation = new GlobeAnnotation(text, position);
        annotation.setValue(KEY_CAT, category);
        add(annotation);
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
                    .filter(item -> CollectionHelper.incInteger(map, (String) item.getValue(KEY_CAT)) > limit)
                    .toList();

            items.removeAll(itemsToRemove);
        }
    }

    private static class Holder {

        private static final AnnotationManager INSTANCE = new AnnotationManager();
    }
}
