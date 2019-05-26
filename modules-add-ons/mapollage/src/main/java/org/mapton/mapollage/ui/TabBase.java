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
package org.mapton.mapollage.ui;

import java.util.ResourceBundle;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.controlsfx.validation.ValidationSupport;
import org.mapton.mapollage.Options;
import org.mapton.mapollage.api.Mapo;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TabBase extends Tab {

    public static final int ICON_SIZE = 22;
    protected static ValidationSupport sValidationSupport;
    protected final ResourceBundle mBundle = SystemHelper.getBundle(TabBase.class, "Bundle");
    protected Mapo mMapo;
    protected final Options mOptions = Options.getInstance();
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Color mIconColor = Color.BLACK;
    private final ScrollPane mScrollPane = new ScrollPane();

    public static void setValidationSupport(ValidationSupport validationSupport) {
        TabBase.sValidationSupport = validationSupport;
    }

    public TabBase() {
        mScrollPane.setFitToHeight(true);
        mScrollPane.setFitToWidth(true);
    }

    public void setScrollPaneContent(Region node) {
        mScrollPane.setContent(node);
        setContent(mScrollPane);
    }

}
