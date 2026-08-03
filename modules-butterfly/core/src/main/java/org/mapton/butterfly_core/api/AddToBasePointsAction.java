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
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AddToBasePointsAction extends Action {

    public AddToBasePointsAction(BaseManager manager) {
        super(Dict.ADD.toString());
        setEventHandler(actionEvent -> {
            manager.getTimeFilteredItems().stream().map(a -> (BXyzPoint) a).forEach(e -> {
                var p = (BXyzPoint) e;
                System.out.println(p.getName());
            });
            System.out.println("^^^Add points to base points");
        });
        setGraphic(MaterialIcon._Content.ADD.getImageView(Mapton.getIconSizeToolBarInt()));
    }
}
