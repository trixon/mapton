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
package org.mapton.butterfly_core.indicators;

import gov.nasa.worldwind.geom.Position;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.mapton.api.MContextMenuItem;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MContextMenuItem.class)
public class ScaleRulerAddContextExtras extends BaseContextMenuItem {

    public ScaleRulerAddContextExtras() {
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return actionEvent -> {
            mScaleRulerManager.add(Position.fromDegrees(getLatitude(), getLongitude()));
        };
    }

    @Override
    public String getName() {
        return mBundle.getString("scaleRulerAdd");
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

}
