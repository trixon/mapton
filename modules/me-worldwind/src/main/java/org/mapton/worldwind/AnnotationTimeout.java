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
public enum AnnotationTimeout {
    NO_TIMEOUT(NbBundle.getMessage(AnnotationTimeout.class, "annotationNoTimeout"), 0),
    SEC_03("3 s", 3),
    SEC_05("5 s", 5),
    SEC_10("10 s", 10),
    SEC_15("15 s", 15);
    private final String mTitle;
    private final int mDelay;

    private AnnotationTimeout(String title, int delay) {
        mTitle = title;
        mDelay = delay * 1000;
    }

    public int getDelay() {
        return mDelay;
    }

    @Override
    public String toString() {
        return mTitle;
    }
}
