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
package org.mapton.core.ui.commandbox;

import org.controlsfx.control.action.Action;
import org.openide.modules.Places;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import org.mapton.api.MCommandBoxItem;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCommandBoxItem.class)
public class OpenMaptonConfigDirCommandBoxItem extends BaseOpenFileCommandBoxItem {

    public OpenMaptonConfigDirCommandBoxItem() {
    }

    @Override
    public Action getAction() {
        return new Action("Mapton Config", actionEvent -> {
//            SystemHelper.desktopOpen(new File(System.getProperty("netbeans.user")));
            SystemHelper.desktopOpen(Places.getUserDirectory());

        });
    }

}
