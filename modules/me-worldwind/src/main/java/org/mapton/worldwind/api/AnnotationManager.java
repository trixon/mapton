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
import org.mapton.api.MAnnotation;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class AnnotationManager extends MBaseDataManager<GlobeAnnotation> {

    public static AnnotationManager getInstance() {
        return Holder.INSTANCE;
    }

    private AnnotationManager() {
        super(GlobeAnnotation.class);

        Mapton.getGlobalState().addListener(gsce -> {
            add(gsce.<MAnnotation>getValue());
        }, "annotation");
    }

    public void add(MAnnotation annotation) {
        add(annotation.getLatLon(), annotation.getText(), annotation.getCategory());
    }

    public void add(GlobeAnnotation annotation) {
        getAllItems().add(annotation);
    }

    public void add(MLatLon latLon, String text, String category) {
        add(WWHelper.positionFromLatLon(latLon), text, category);
    }

    public void add(Position position, String text, String category) {
        var annotation = new GlobeAnnotation(text, position);
        annotation.setValue("category", category);
        add(annotation);
    }

    @Override
    protected void applyTemporalFilter() {
    }

    @Override
    protected void load(ArrayList<GlobeAnnotation> items) {
    }

    private static class Holder {

        private static final AnnotationManager INSTANCE = new AnnotationManager();
    }
}
