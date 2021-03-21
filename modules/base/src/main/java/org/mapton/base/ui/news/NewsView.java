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
package org.mapton.base.ui.news;

import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

public class NewsView extends HBox {

    private final AppNewsSection mStaticNewsSection = new AppNewsSection();
    private final DynamicNewsSection mDynamicNewsSection = new DynamicNewsSection();

    public NewsView() {
        getChildren().setAll(
                mDynamicNewsSection,
                new Separator(),
                mStaticNewsSection
        );
        HBox.setHgrow(mDynamicNewsSection, Priority.ALWAYS);
        HBox.setHgrow(mStaticNewsSection, Priority.ALWAYS);
    }
}
