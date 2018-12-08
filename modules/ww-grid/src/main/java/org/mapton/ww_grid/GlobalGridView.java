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
package org.mapton.ww_grid;

import java.awt.Dimension;
import java.util.ResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javax.swing.SwingUtilities;
import static org.mapton.ww_grid.Options.*;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GlobalGridView extends VBox {

    private Button mButton;

    private final Options mOptions = Options.getInstance();
    private CheckBox mPlotCheckBox;

    public GlobalGridView() {
        createUI();
        initStates();
        initListeners();
    }

    private void createUI() {
        Font defaultFont = Font.getDefault();
        ResourceBundle bundle = NbBundle.getBundle(GridTopComponent.class);
        mPlotCheckBox = new CheckBox(bundle.getString("global"));
        mPlotCheckBox.setFont(new Font(defaultFont.getSize() * 1.2));
        mButton = new Button(Dict.OPTIONS.toString());

        mButton.disableProperty().bind(mPlotCheckBox.selectedProperty().not());

        getChildren().addAll(
                mPlotCheckBox,
                mButton
        );

        setSpacing(8);
    }

    private void initListeners() {
        mButton.setOnAction((event) -> {
            SwingUtilities.invokeLater(() -> {
                GlobalGridPanel globalGridPanel = new GlobalGridPanel();
                globalGridPanel.initFx(() -> {
                });

                globalGridPanel.setPreferredSize(new Dimension(280, 300));

                String[] buttons = new String[]{Dict.CLOSE.toString()};
                NotifyDescriptor d = new NotifyDescriptor(
                        globalGridPanel,
                        Dict.CUSTOMIZE.toString(),
                        NotifyDescriptor.OK_CANCEL_OPTION,
                        NotifyDescriptor.PLAIN_MESSAGE,
                        buttons,
                        buttons[0]);

                DialogDisplayer.getDefault().notify(d);
            });
        });

        mPlotCheckBox.setOnAction((event) -> {
            mOptions.set(KEY_GLOBAL_PLOT, mPlotCheckBox.isSelected());
        });

    }

    private void initStates() {
        mPlotCheckBox.setSelected(mOptions.is(KEY_GLOBAL_PLOT));
    }

}
