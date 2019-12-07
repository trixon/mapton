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
package org.mapton.addon.geonames_ww;

import java.util.Arrays;
import java.util.Collection;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.RandomUtils;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.api.MTopComponent;
import org.mapton.geonames.api.Country;
import org.mapton.geonames.api.CountryManager;
import org.mapton.geonames.api.GeonamesManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.addon.geonames//GeoNames//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "GeoNamesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public final class GeoNamesTopComponent extends MTopComponent implements MMapMagnet {

    private IndexedCheckModel<Country> mCheckModel;
    private ListChangeListener<Country> mListChangeListener;
    private CheckListView<Country> mListView;
    private BorderPane mRoot;

    public GeoNamesTopComponent() {
        setName(GeoNamesTool.NAME);
        GeonamesManager.getInstance().init();
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initListeners();
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private Scene createScene() {
        Label titleLabel = Mapton.createTitle(GeoNamesTool.NAME);

        mListView = new CheckListView<>();
        mListView.getItems().setAll(CountryManager.getInstance().getCountryList());
        mCheckModel = mListView.getCheckModel();

        Collection<? extends Action> actions = Arrays.asList(
                new Action(Dict.RANDOM.toString(), (event) -> {
                    mListView.getCheckModel().getCheckedItems().removeListener(mListChangeListener);
                    int randomSpan = 10;
                    int offset = RandomUtils.nextInt(0, randomSpan);
                    for (int i = offset; i < mListView.getItems().size(); i = i + randomSpan) {
                        mListView.getCheckModel().check(i);
                    }

                    Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, mCheckModel.getCheckedItems());
                    mCheckModel.getCheckedItems().addListener(mListChangeListener);
                }),
                new Action(Dict.CLEAR_SELECTION.toString(), (event) -> {
                    mListView.getCheckModel().getCheckedItems().removeListener(mListChangeListener);
                    mListView.getCheckModel().clearChecks();
                    Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, mCheckModel.getCheckedItems());
                    mCheckModel.getCheckedItems().addListener(mListChangeListener);
                })
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.SHOW);

        BorderPane innerPane = new BorderPane(toolBar);
        mRoot = new BorderPane(mListView);
        innerPane.setTop(titleLabel);
        mRoot.setTop(innerPane);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        return new Scene(mRoot);
    }

    private Country getSelected() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends Country> c) -> {
            if (getSelected() != null) {
                Mapton.getEngine().fitToBounds(getSelected().getLatLonBox());
            }
        });

        mListChangeListener = (ListChangeListener.Change<? extends Country> c) -> {
            Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, mCheckModel.getCheckedItems());
        };

        mCheckModel.getCheckedItems().addListener(mListChangeListener);
    }
}
