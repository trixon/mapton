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
package org.mapton.core_wb.modules.map.mode;

import org.openide.util.lookup.ServiceProvider;
import se.trixon.windowsystemfx.Mode;
import se.trixon.windowsystemfx.ModeLayout;
import se.trixon.windowsystemfx.WindowSystemComponent;

@WindowSystemComponent.Description(
        preferredId = RootMode.ROOT_ID,
        modeLayout = ModeLayout.SPLIT_HORIZONTAL,
        position = 0
)
/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = Mode.class)
public class RootMode extends Mode {

    public static final String ROOT_ID = "root";

    @WindowSystemComponent.Description(
            preferredId = LeftMode.ID,
            parentId = ROOT_ID,
            modeLayout = ModeLayout.SPLIT_VERTICAL,
            position = 1
    )
    @ServiceProvider(service = Mode.class)
    public static class LeftMode extends Mode {

        public static final String ID = "root_left";

    }

    @WindowSystemComponent.Description(
            preferredId = RightMode.ID,
            parentId = ROOT_ID,
            modeLayout = ModeLayout.SPLIT_VERTICAL,
            position = 2
    )
    @ServiceProvider(service = Mode.class)
    public static class RightMode extends Mode {

        public static final String ID = "root_right";

    }

}
