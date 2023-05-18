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
package org.mapton.transformation;

import java.util.Locale;
import javafx.scene.control.ListCell;
import javafx.scene.control.Tooltip;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 *
 * @author Patrik Karlström
 */
class CrsListCell extends ListCell<CoordinateReferenceSystem> {

    public CrsListCell() {
    }

    @Override
    protected void updateItem(CoordinateReferenceSystem item, boolean empty) {
        super.updateItem(item, empty);
        if (item == null || empty) {
            setText(null);
        } else {
            setText(item.getName().toString());
            try {
                var desc = item.getDomainOfValidity().getDescription().toString(Locale.getDefault());
                setTooltip(new Tooltip(desc));
            } catch (NullPointerException e) {
            }
        }
    }
}
