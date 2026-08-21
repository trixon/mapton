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
package org.mapton.butterfly_core.api;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import org.controlsfx.control.action.Action;
import org.mapton.api.Mapton;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class WatchlistEditorAction extends Action {

    private final JTextArea mTextArea = new JTextArea(16, 40);
    private final JScrollPane mScrollPane = new JScrollPane(mTextArea);

    public WatchlistEditorAction(BaseManager manager) {
        super("Redigera bevakningslista");
        mTextArea.setLineWrap(true);
        mTextArea.setWrapStyleWord(true);

        setEventHandler(actionEvent -> {
            var p = NbPreferences.forModule(manager.getClass()).node("watchlist").node("definition");
            mTextArea.setText(p.get("content", "# List of point names and/or polygons"));
            var d = new NotifyDescriptor(
                    mScrollPane,
                    "Redigera bevakningslista",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.PLAIN_MESSAGE,
                    null,
                    null
            );

            SwingUtilities.invokeLater(() -> {
                if (DialogDisplayer.getDefault().notify(d) == NotifyDescriptor.OK_OPTION) {
                    p.put("content", mTextArea.getText());
                }
            });
        });
        setGraphic(MaterialIcon._Action.VISIBILITY.getImageView(Mapton.getIconSizeToolBarInt()));
    }
}
