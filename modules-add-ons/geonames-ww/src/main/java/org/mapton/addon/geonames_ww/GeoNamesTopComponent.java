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

import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
import org.mapton.geonames.api.Country;
import org.mapton.geonames.api.CountryManager;
import org.mapton.geonames.api.GeonamesManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;

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
        mRoot = new BorderPane(mListView);
        mRoot.setTop(titleLabel);
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

        final IndexedCheckModel<Country> checkModel = mListView.getCheckModel();

        checkModel.getCheckedItems().addListener((ListChangeListener.Change<? extends Country> c) -> {
            Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, checkModel.getCheckedItems());
        });
    }
}
