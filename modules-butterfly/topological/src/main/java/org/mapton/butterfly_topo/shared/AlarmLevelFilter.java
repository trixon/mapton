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
package org.mapton.butterfly_topo.shared;

import java.util.ResourceBundle;
import org.mapton.butterfly_format.types.BComponent;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public enum AlarmLevelFilter {
    HEIGHT_0(BComponent.HEIGHT, 0),
    HEIGHT_1(BComponent.HEIGHT, 1),
    HEIGHT_2(BComponent.HEIGHT, 2),
    HEIGHT_E(BComponent.HEIGHT, -1),
    PLANE_0(BComponent.PLANE, 0),
    PLANE_1(BComponent.PLANE, 1),
    PLANE_2(BComponent.PLANE, 2),
    PLANE_E(BComponent.PLANE, -1);
    private final BComponent mComponent;
    private final int mLevel;
    private final ResourceBundle mBundle = NbBundle.getBundle(AlarmLevelFilter.class);

    private AlarmLevelFilter(BComponent component, int level) {
        mComponent = component;
        mLevel = level;
    }

    public BComponent getComponent() {
        return mComponent;
    }

    public int getLevel() {
        return mLevel;
    }

    public String getName() {
        return mBundle.getString(name());
    }

    @Override
    public String toString() {
        return getName();
    }

}
