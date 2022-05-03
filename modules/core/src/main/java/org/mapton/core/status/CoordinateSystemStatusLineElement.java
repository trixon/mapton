/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.core.status;

import java.awt.Component;
import java.awt.event.ActionListener;
import javafx.collections.ListChangeListener;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.border.EmptyBorder;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCrsManager;
import static org.mapton.api.MEngine.KEY_STATUS_COORDINATE;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = StatusLineElementProvider.class, position = 599)
public class CoordinateSystemStatusLineElement implements StatusLineElementProvider {

    private ActionListener mActionListener;
    private JComboBox<MCooTrans> mComboBox;
    private MCooTrans mCooTrans;
    private final MCrsManager mManager = MCrsManager.getInstance();
    private final MOptions mOptions = MOptions.getInstance();

    public CoordinateSystemStatusLineElement() {
    }

    @Override
    public Component getStatusLineElement() {
        if (mComboBox == null) {
            init();
            initListeners();
            updateProviders();
        }

        return mComboBox;
    }

    private void init() {
        mComboBox = new JComboBox<>();
        mComboBox.setBorder(new EmptyBorder(0, 0, 0, 0));
    }

    private void initListeners() {
        mActionListener = actionEvent -> {
            mCooTrans = (MCooTrans) mComboBox.getModel().getSelectedItem();
            mOptions.setMapCooTrans(mCooTrans.getName());
            updateMousePositionData();
        };

        mManager.itemsProperty().get().addListener((ListChangeListener.Change<? extends MCooTrans> c) -> {
            updateProviders();
        });
    }

    private void updateMousePositionData() {
        Mapton.getGlobalState().put(KEY_STATUS_COORDINATE, Mapton.getGlobalState().get(KEY_STATUS_COORDINATE));

    }

    private void updateProviders() {
        SwingHelper.runLater(() -> {
            mComboBox.removeActionListener(mActionListener);
            mComboBox.removeAllItems();
            var cooTranses = mManager.getItems().sorted((o1, o2) -> o1.getName().compareToIgnoreCase(o2.getName()));
            mComboBox.setModel(new DefaultComboBoxModel<>(cooTranses.toArray(new MCooTrans[0])));

            for (var cooTrans : cooTranses) {
                Mapton.logLoading("Coordinate Transformation", cooTrans.getName());
            }

            if (!cooTranses.isEmpty()) {
                var cooTrans = MCooTrans.getCooTrans(mOptions.getMapCooTransName());

                if (cooTrans == null) {
                    cooTrans = mComboBox.getModel().getElementAt(0);
                }

                mComboBox.getModel().setSelectedItem(cooTrans);
                mCooTrans = (MCooTrans) mComboBox.getModel().getSelectedItem();
                updateMousePositionData();
            }

            mComboBox.addActionListener(mActionListener);
        });
    }
}
