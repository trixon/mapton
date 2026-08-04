/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.util.HashSet;
import java.util.List;
import org.controlsfx.control.action.Action;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.base.XyzManager;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AddToBasePointsAction extends Action {

    public AddToBasePointsAction(BaseManager manager) {
        super("Lägg till i 'Baspunkter'");
        setEventHandler(actionEvent -> {
            var xyzManager = XyzManager.getInstance();
            if (manager != xyzManager) {
                var items = (List<BXyzPoint>) manager.getTimeFilteredItems();
                var targetPoints = new HashSet<>(xyzManager.getAllItems());
                var points = items.stream()
                        .map(p -> (BXyzPoint) p)
                        .filter(p -> !targetPoints.contains(p))
                        .toList();
                xyzManager.getAllItems().addAll(points);
            }
        });
        setGraphic(MaterialIcon._Content.ADD.getImageView(Mapton.getIconSizeToolBarInt()));
    }
}
