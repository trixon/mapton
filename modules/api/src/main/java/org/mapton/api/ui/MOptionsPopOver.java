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
package org.mapton.api.ui;

import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class MOptionsPopOver extends MPopOver {

    public MOptionsPopOver() {
        String title = Dict.OPTIONS.toString();
        setTitle(title);
        getAction().setText(title);

        //Due to the lookup we get here before Core.init, so set the icon later
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED, () -> {
            FxHelper.runLater(() -> {
                getAction().setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt()));
            });
        });
    }

}
