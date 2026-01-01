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
package org.mapton.worldwind;

import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public enum AnnotationLimitMode {
    TOTAL(NbBundle.getMessage(AnnotationLimitMode.class, "annotationModeTotal")),
    CATEGORY(NbBundle.getMessage(AnnotationLimitMode.class, "annotationModeCategory"));
    private final String mTitle;

    private AnnotationLimitMode(String title) {
        mTitle = title;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
