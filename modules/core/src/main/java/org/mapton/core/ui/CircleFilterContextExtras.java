/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.core.ui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.mapton.api.MCircleFilterManager;
import org.mapton.api.MContextMenuItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PrefsHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@NbBundle.Messages({
    "CTL_CircleFilterMenu=&Circle filter"
})
@ServiceProvider(service = MContextMenuItem.class)
public class CircleFilterContextExtras extends MContextMenuItem {

    public CircleFilterContextExtras() {
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return event -> {
            SwingHelper.runLaterDelayed(10, () -> displayDialog());
        };
    }

    @Override
    public String getName() {
        return Actions.cutAmpersand(Bundle.CTL_CircleFilterMenu());
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

    @NbBundle.Messages("filterRadiusTitle=Enter filter radius")
    private void displayDialog() {
        var d = new NotifyDescriptor.InputLine(
                Dict.Geometry.RADIUS.toString(),
                Bundle.filterRadiusTitle());
        var p = NbPreferences.forModule(CircleFilterContextExtras.class);
        var key = "circleFilterRadius";
        d.setInputText("%.0f".formatted(p.getDouble(key, 10)));
        Double distance = Double.MAX_VALUE;
        if (DialogDisplayer.getDefault().notify(d) == DialogDescriptor.OK_OPTION) {
            try {
                var radius = Double.valueOf((String) d.getInputText());
                if (radius >= 0) {
                    p.putDouble(key, radius);
                    distance = radius;
                } else {
                    PrefsHelper.removeIfPresent(p, key);
                }
            } catch (NumberFormatException e) {
                //
            }
        } else {
            PrefsHelper.removeIfPresent(p, key);
        }

        MCircleFilterManager.getInstance().set(getLatitude(), getLongitude(), distance);
    }

}
