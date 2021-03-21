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
package org.mapton.core.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
        position = 0,
        categoryName = "#OptionsCategory_Name_Mapton",
        iconBase = "org/mapton/core/ui/options/mapton32.png",
        keywords = "#OptionsCategory_Keywords_Mapton",
        keywordsCategory = "Mapton"
)
@org.openide.util.NbBundle.Messages({"OptionsCategory_Name_Mapton=Mapton", "OptionsCategory_Keywords_Mapton=mapton"})
public final class MainOptionsPanelController extends OptionsPanelController {

    private boolean mChanged;
    private MainPanel mMainPanel;
    private final PropertyChangeSupport mPropertyChangeSupport = new PropertyChangeSupport(this);

    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.addPropertyChangeListener(l);
    }

    @Override
    public void applyChanges() {
        SwingUtilities.invokeLater(() -> {
            getMainPanel().store();
            mChanged = false;
        });
    }

    @Override
    public void cancel() {
        mMainPanel.cancel();
    }

    @Override
    public JComponent getComponent(Lookup masterLookup) {
        return getMainPanel();
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
        return getMainPanel().valid();
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        mPropertyChangeSupport.removePropertyChangeListener(l);
    }

    @Override
    public void update() {
        getMainPanel().load();
        mChanged = false;
    }

    void changed() {
        if (!mChanged) {
            mChanged = true;
            mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }

        mPropertyChangeSupport.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }

    private MainPanel getMainPanel() {
        if (mMainPanel == null) {
            mMainPanel = new MainPanel(this);
        }

        return mMainPanel;
    }

}
