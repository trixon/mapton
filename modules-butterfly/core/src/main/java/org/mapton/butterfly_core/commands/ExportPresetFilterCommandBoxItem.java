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
package org.mapton.butterfly_core.commands;

import net.lingala.zip4j.exception.ZipException;
import org.controlsfx.control.action.Action;
import org.mapton.api.MCommandBoxItem;
import org.mapton.core.api.ui.MPresetPopOver;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCommandBoxItem.class)
public class ExportPresetFilterCommandBoxItem extends BaseExportPresetCommandBoxItem {

    public ExportPresetFilterCommandBoxItem() {
    }

    @Override
    public Action getAction() {
        return new Action("Filter", actionEvent -> {
            try {
                export(MPresetPopOver.PARENT_NODE_FILTER);
            } catch (ZipException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

}
