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
package org.mapton.core.ui.grid;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javax.swing.SwingUtilities;
import org.mapton.api.MDict;
import org.mapton.api.MLocalGrid;
import org.mapton.api.MLocalGridManager;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridEditor {

    private final MLocalGridManager mManager = MLocalGridManager.getInstance();

    public LocalGridEditor() {
    }

    public void edit(final MLocalGrid aLocalGrid) {
        SwingUtilities.invokeLater(() -> {
            MLocalGrid newLocalGrid = aLocalGrid;
            boolean add = aLocalGrid == null;
            if (add) {
                newLocalGrid = new MLocalGrid();
            }

            final MLocalGrid localGrid = newLocalGrid;
            LocalGridPanel localGridPanel = new LocalGridPanel();
            DialogDescriptor d = new DialogDescriptor(localGridPanel, MDict.GRID.toString());
            localGridPanel.setNotifyDescriptor(d);
            localGridPanel.initFx(() -> {
                localGridPanel.load(localGrid);
            });

            localGridPanel.setPreferredSize(SwingHelper.getUIScaledDim(600, 380));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    localGridPanel.save(localGrid);
                    if (add) {
                        mManager.getItems().add(localGrid);
                    }

                    FXCollections.sort(mManager.getItems(), (MLocalGrid o1, MLocalGrid o2) -> o1.getName().compareTo(o2.getName()));
                });
            }
        });
    }

    public void remove(MLocalGrid localGrid) {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), localGrid.getName()),
                    String.format(Dict.Dialog.TITLE_REMOVE_S.toString(), MDict.GRID.toString().toLowerCase()) + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE.toString());

            if (Dict.REMOVE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll(localGrid);
                });
            }
        });
    }

}
