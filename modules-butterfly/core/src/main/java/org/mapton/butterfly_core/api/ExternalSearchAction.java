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
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class ExternalSearchAction extends Action {

    public ExternalSearchAction(BaseManager manager) {
        super(Dict.BROWSE.toString());
        setEventHandler(actionEvent -> {
            String url;
            try {
                url = (String) manager.getExternalSysUrlCallable().call();
                if (url != null) {
                    SystemHelper.desktopBrowse(url);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        setGraphic(MaterialIcon._Social.PUBL.getImageView(Mapton.getIconSizeToolBarInt()));
        disabledProperty().bind(manager.disabledSearchProperty());
    }
}
