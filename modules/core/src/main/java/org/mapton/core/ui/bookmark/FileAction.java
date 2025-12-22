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
package org.mapton.core.ui.bookmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.Mapton;
import org.mapton.api.jackson.LocalDateTimeDeserializer;
import org.mapton.api.jackson.LocalDateTimeSerializer;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class FileAction {

    protected final ResourceBundle mBundle = NbBundle.getBundle(FileAction.class);
    protected Color mIconColor = Mapton.options().getIconColorForBackground();
    protected final ObjectMapper mJsonObjectMapper;
    protected final MBookmarkManager mManager = MBookmarkManager.getInstance();
    protected PopOver mPopOver;
    protected final String mTitle = Dict.BOOKMARKS.toString();

    public FileAction(PopOver popOver) {
        mPopOver = popOver;
        var simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        mJsonObjectMapper = new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .registerModules(new JavaTimeModule(), simpleModule);
    }

    public abstract Action getAction(Node owner);

    protected void hidePopOver() {
        if (mPopOver != null) {
            Platform.runLater(() -> {
                mPopOver.hide();
            });
        }
    }
}
