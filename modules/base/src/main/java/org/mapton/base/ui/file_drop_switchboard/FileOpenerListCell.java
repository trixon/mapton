/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.base.ui.file_drop_switchboard;

import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import org.mapton.api.MCoordinateFileOpener;

/**
 *
 * @author Patrik Karlström
 */
class FileOpenerListCell extends ListCell<MCoordinateFileOpener> {

    public FileOpenerListCell() {
    }

    @Override
    protected void updateItem(MCoordinateFileOpener item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
        } else {
            setText(item.getName());
            setTooltip(new Tooltip(item.getDescription()));
        }
    }
}
