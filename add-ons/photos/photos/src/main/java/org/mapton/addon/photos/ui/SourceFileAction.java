/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.addon.photos.ui;

import javax.swing.filechooser.FileNameExtensionFilter;
import org.controlsfx.control.action.Action;
import org.mapton.addon.photos.api.MapoSourceManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
public abstract class SourceFileAction {

    protected final MapoSourceManager mManager = MapoSourceManager.getInstance();
    protected final String mTitle = Dict.SOURCES.toString();

    public SourceFileAction() {
        SimpleDialog.getExtensionFilters().put("mapo", new FileNameExtensionFilter("Mapton Mapollage (*.mapo)", "*.mapo"));
    }

    public abstract Action getAction();
}
