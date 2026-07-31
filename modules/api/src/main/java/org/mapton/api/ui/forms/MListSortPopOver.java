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
package org.mapton.api.ui.forms;

import javafx.scene.Node;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MPopOver;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MListSortPopOver extends MPopOver {

    public static final String TITLE = "Sortera & begränsa";

    public MListSortPopOver() {
        super(TITLE, false);
        setTitle(TITLE);
        setArrowLocation(ArrowLocation.TOP_LEFT);
        getAction().setGraphic(MaterialIcon._Content.SORT.getImageView(getIconSizeToolBarInt()));
    }

    public void setNode(Node node) {
        setContentNode(node);
    }
}
