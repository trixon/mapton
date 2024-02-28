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
package org.mapton.butterfly_core.loader;

import org.controlsfx.control.action.Action;
import org.mapton.api.MCommandBoxItem;
import org.mapton.api.MCommandBoxItemSystem;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCommandBoxItem.class)
public class CloseButterflyCommandBoxItem extends MCommandBoxItemSystem {

    public CloseButterflyCommandBoxItem() {
    }

    @Override
    public String getParent() {
        return "Butterfly";
    }

    @Override
    public Action getAction() {
        return new Action("%s Butterfly (%s)".formatted(Dict.CLOSE.toString(), Dict.RESTART.toLower()), actionEvent -> {
            ButterflyOpener.getInstance().close();
        });
    }

}
