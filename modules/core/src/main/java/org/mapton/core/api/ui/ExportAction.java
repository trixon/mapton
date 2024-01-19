/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.core.api.ui;

import org.controlsfx.control.action.Action;
import org.mapton.api.Mapton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ExportAction extends Action {

    private final ExportPanel mExportPanel;
    private boolean mFirstRun = true;

    public ExportAction(Object lookupKey) {
        super(Dict.EXPORT.toString());

        mExportPanel = new ExportPanel(lookupKey);
        setEventHandler(actionEvent -> displayExportDialog());
        setGraphic(MaterialIcon._Content.SAVE.getImageView(Mapton.getIconSizeToolBarInt()));
        setDisabled(mExportPanel.getExportProviders().isEmpty());
    }

    private void displayExportDialog() {
        if (mFirstRun) {
            mExportPanel.initFx(() -> mExportPanel.load());
            mFirstRun = false;
        }

        var d = new DialogDescriptor(mExportPanel, getText());
        mExportPanel.setNotifyDescriptor(d);
        mExportPanel.setPreferredSize(SwingHelper.getUIScaledDim(700, 400));

        SwingHelper.runLaterDelayed(1, () -> {
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                mExportPanel.export();
            }
        });
    }
}
