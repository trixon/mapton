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
package se.trixon.mapton.core.testing;

import javax.swing.SwingUtilities;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.core.api.MenuItemProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MenuItemProvider.class)
public class Fx1MenuItem extends MenuItemProvider {

    public Fx1MenuItem() {
        setText("Fx1TopComp");
        setOnAction((actionEvent) -> {
            SwingUtilities.invokeLater(() -> {
                mWindowManager.findTopComponent("Fx1TopComponent").open();
                mWindowManager.findTopComponent("Fx1TopComponent").requestActive();
            });
        });
    }

}
