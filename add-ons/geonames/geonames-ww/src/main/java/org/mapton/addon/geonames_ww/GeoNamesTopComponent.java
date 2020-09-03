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
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.RandomUtils;
import org.controlsfx.control.CheckListView;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core_nb.api.MTopComponent;
import org.mapton.geonames.api.Country;
import org.mapton.geonames.api.CountryManager;
import org.mapton.geonames.api.GeonamesManager;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

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
@TopComponent.Registration(mode = "mapTools", openAtStartup = false)
@TopComponent.OpenActionRegistration(
        displayName = "#CTL_GeoNamesAction",
        preferredID = "GeoNamesTopComponent"
)
@ActionID(category = "Mapton", id = "org.mapton.addon.geonames.GeoNamesAction")
@ActionReferences({
    @ActionReference(path = "Menu/Tools/Add-on", position = 0)
})
@NbBundle.Messages({
    "CTL_GeoNamesAction=Population (GeoNames)"
})
public final class GeoNamesTopComponent extends MTopComponent {

    private IndexedCheckModel<Country> mCheckModel;
    private ListChangeListener<Country> mListChangeListener;
    private CheckListView<Country> mListView;
    private BorderPane mRoot;

    public GeoNamesTopComponent() {
        setName(Bundle.CTL_GeoNamesAction());
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

        Action randomAction = new Action(Dict.RANDOM.toString(), event -> {
            mListView.getCheckModel().getCheckedItems().removeListener(mListChangeListener);
            int randomSpan = 10;
            int offset = RandomUtils.nextInt(0, randomSpan);
            for (int i = offset; i < mListView.getItems().size(); i = i + randomSpan) {
                mListView.getCheckModel().check(i);
            }

            Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, mCheckModel.getCheckedItems());
            mCheckModel.getCheckedItems().addListener(mListChangeListener);
        });
        randomAction.setGraphic(MaterialIcon._Places.CASINO.getImageView(getIconSizeToolBarInt()));

        Action clearAction = new Action(Dict.CLEAR.toString(), event -> {
            mListView.getCheckModel().getCheckedItems().removeListener(mListChangeListener);
            mListView.getCheckModel().clearChecks();
            Mapton.getGlobalState().put(GeoN.KEY_LIST_SELECTION, mCheckModel.getCheckedItems());
            mCheckModel.getCheckedItems().addListener(mListChangeListener);
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        List<Action> actions = Arrays.asList(
                randomAction,
                ActionUtils.ACTION_SPAN,
                clearAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);

        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        toolBar.getItems().stream().filter((item) -> (item instanceof ButtonBase))
                .map((item) -> (ButtonBase) item).forEachOrdered((buttonBase) -> {
            FxHelper.undecorateButton(buttonBase);
        });

        FxHelper.slimToolBar(toolBar);

        Label titleLabel = Mapton.createTitle(Bundle.CTL_GeoNamesAction());

        mListView = new CheckListView<>();
        mListView.getItems().setAll(CountryManager.getInstance().getCountryList());
        mCheckModel = mListView.getCheckModel();

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
