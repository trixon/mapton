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
package org.mapton.maintenance.bookmarks2601;

import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.nbp.dialogs.NbOptionalDialog;
import se.trixon.almond.util.fx.FxHelper;

/**
 * This class will notify the user of a change in user config/cache paths i.e.
 * 2.1
 *
 * @author Patrik Karlström
 */
@OnStart
public class Bookmarks2601 implements Runnable {

    private final String mIntro;

    public Bookmarks2601() {
        mIntro = """
                 Heads up, your bookmarks are about to be converted.

                 The old sql format will be replaced with csv.

                 The old sql file will be kept.

                 """;
    }

    @Override
    public void run() {
        NbOptionalDialog.reset(Bookmarks2601.class.toString() + "_0");
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED, () -> {
            var key = "bookmarks2601oldFormat";
            var preferences = NbPreferences.forModule(Bookmarks2601.class);
            var sqlBookmarks = MBookmarkManagerSql.getInstance().dbLoad("*", true);

            if (!sqlBookmarks.isEmpty() && preferences.getBoolean(key, true)) {
                NbMessage.information("IMPORTANT INFORMATION", mIntro);
                var booksmarks = sqlBookmarks.stream()
                        .map(sql -> {
                            var csv = new MBookmark();
                            csv.setName(sql.getName());
                            csv.setCategory(sql.getCategory());
                            csv.setColor(sql.getColor());
                            csv.setDescription(sql.getDescription());
                            csv.setDisplayMarker(sql.isDisplayMarker());
                            csv.setLatitude(sql.getLatitude());
                            csv.setLongitude(sql.getLongitude());
                            csv.setTimeCreated(sql.getTimeCreated().toLocalDateTime());
                            csv.setTimeModified(sql.getTimeModified() != null ? sql.getTimeModified().toLocalDateTime() : null);
                            csv.setUrl(sql.getUrl());
                            csv.setZoom(sql.getZoom());
                            return csv;
                        })
                        .toList();

                FxHelper.runLater(() -> MBookmarkManager.getInstance().add(booksmarks));

                preferences.putBoolean(key, false);
            }
        });
    }
}
