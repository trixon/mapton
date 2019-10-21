/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.api.bookmark;

import java.util.ResourceBundle;
import javafx.stage.FileChooser.ExtensionFilter;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmarkManager;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BookmarkAction {

    protected final ResourceBundle mBundle = NbBundle.getBundle(BookmarkAction.class);
    protected ExtensionFilter mExtAll = new ExtensionFilter(Dict.ALL_FILES.toString(), "*");
    protected ExtensionFilter mExtCsv = new ExtensionFilter("Comma-separated value (*.csv)", "*.csv");
    protected ExtensionFilter mExtGeo = new ExtensionFilter("SBG Geo (*.geo)", "*.geo");
    protected ExtensionFilter mExtJson = new ExtensionFilter("JSON (*.json)", "*.json");
    protected ExtensionFilter mExtKml = new ExtensionFilter("Keyhole Markup Language (*.kml)", "*.kml");
    protected final MBookmarkManager mManager = MBookmarkManager.getInstance();
    protected final String mTitle = Dict.BOOKMARKS.toString();

    public abstract Action getAction();
}
