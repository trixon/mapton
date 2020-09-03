/*
 * Copyright 2020 patrik.
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
package org.mapton.core_nb.actions;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javax.swing.AbstractAction;
import org.mapton.core_nb.ui.AppToolBarProvider;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import org.openide.util.actions.Presenter;
import se.trixon.almond.nbp.fx.FxPanel;

@ActionID(category = "Mapton", id = "org.mapton.core_nb.actions.SomeAction")
//@ActionRegistration(displayName = "#CTL_SomeAction")
@ActionRegistration(lazy = false, displayName = "not used")

@ActionReference(path = "Menu", position = 9009)
@Messages("CTL_SomeAction=Fxx")
public final class SomeAction extends AbstractAction implements Presenter.Toolbar {

    private FxPanel mFxPanel;

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println("xxx");
    }

    @Override
    public Component getToolbarPresenter() {
        if (mFxPanel == null) {
            init();
        }
//        return new JComboBox();
//        return mFxPanel;
        return AppToolBarProvider.getInstance().getToolBarPanel();
    }

    private void init() {
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
            }

            private Scene createScene() {
                BorderPane borderPane = new BorderPane();
                borderPane.setCenter(new Label("qwe"));
                return new Scene(borderPane);
            }

        };
        mFxPanel.initFx(null);
//        mFxPanel.setPreferredSize(null);

        mFxPanel.setPreferredSize(new Dimension(999, 1));
    }

}
