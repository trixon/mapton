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
package org.mapton.mapollage;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.SubRegistration(
        location = "Modules",
        displayName = "#AdvancedOption_DisplayName_Mapollage",
        keywords = "#AdvancedOption_Keywords_Mapollage",
        keywordsCategory = "Modules/Mapollage"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Mapollage=Mapollage", "AdvancedOption_Keywords_Mapollage=Mapollage"})
public final class OptionsController extends OptionsPanelController {

    private boolean mChanged;
    private OptionsPanel mPanel;
    private final PropertyChangeSupport mPcs = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mPcs.addPropertyChangeListener(l);
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            getPane().store();
            mChanged = false;
        });
    }

    @Override
    public void cancel() {
        // need not do anything special, if no changes have been persisted yet
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getPane();
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    @Override
    public boolean isChanged() {
        return mChanged;
    }

    @Override
    public boolean isValid() {
        return getPane().valid();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mPcs.removePropertyChangeListener(l);
    }

    @Override
    public void update() {
        getPane().load();
        mChanged = false;
    }

    void mChanged() {
        if (!mChanged) {
            mChanged = true;
            mPcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }

        mPcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private OptionsPanel getPane() {
        if (mPanel == null) {
            mPanel = new OptionsPanel(this);
        }

        return mPanel;
    }
}
