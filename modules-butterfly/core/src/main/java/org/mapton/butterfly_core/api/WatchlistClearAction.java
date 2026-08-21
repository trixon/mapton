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

import org.controlsfx.control.action.Action;
import org.mapton.api.Mapton;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class WatchlistClearAction extends Action {

    public WatchlistClearAction(BaseManager<? extends BBaseControlPoint> manager) {
        super("Kvittera bevakningslista");

        setEventHandler(actionEvent -> {
            var allItems = manager.getAllItems();
            allItems.forEach(p -> p.setValue("watchlistChanged", Boolean.FALSE));
            manager.getAllItems();
            BaseManager rawManager = (BaseManager) manager;

            if (!rawManager.getAllItems().isEmpty()) {
                int lastIndex = rawManager.getAllItems().size() - 1;
                var dummy = rawManager.getAllItems().get(lastIndex);
                rawManager.getAllItems().remove(lastIndex);
                rawManager.getAllItems().add(dummy);
            }
        });
        setGraphic(MaterialIcon._Action.VISIBILITY_OFF.getImageView(Mapton.getIconSizeToolBarInt()));
    }
}
