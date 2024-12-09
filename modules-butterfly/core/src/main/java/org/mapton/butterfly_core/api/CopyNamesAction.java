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

import java.util.List;
import java.util.concurrent.Callable;
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
public class CopyNamesAction extends Action {

    public CopyNamesAction(BaseManager manager) {
        super(Dict.COPY_NAMES.toString());
        setEventHandler(actionEvent -> {
            Callable<List<String>> callable = manager.getCopyNamesCallable();
            try {
                SystemHelper.copyToClipboard(String.join("\n", callable.call()) + "\n");
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        });
        setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(Mapton.getIconSizeToolBarInt()));
    }
}
