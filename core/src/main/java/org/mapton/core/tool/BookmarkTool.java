/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.core.tool;

import javafx.stage.FileChooser;
import org.mapton.api.MTool;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BookmarkTool implements MTool {

    protected FileChooser.ExtensionFilter mExtAll = new FileChooser.ExtensionFilter(Dict.ALL_FILES.toString(), "*");
    protected FileChooser.ExtensionFilter mExtCsv = new FileChooser.ExtensionFilter("Comma-separated value (*.csv)", "*.csv");
    protected FileChooser.ExtensionFilter mExtGeo = new FileChooser.ExtensionFilter("SBG Geo (*.geo)", "*.geo");
    protected FileChooser.ExtensionFilter mExtJson = new FileChooser.ExtensionFilter("JSON (*.json)", "*.json");
    protected FileChooser.ExtensionFilter mExtKml = new FileChooser.ExtensionFilter("Keyhole Markup Language (*.kml)", "*.kml");
    protected final String mTitle = Dict.BOOKMARKS.toString();

}
