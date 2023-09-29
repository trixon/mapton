/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.api.ui;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mapton.api.MDict;
import org.mapton.api.MPolygonFilterManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MFilterPopOver extends MPopOver {

    private final Button mAllButton = new Button(Dict.DEFAULT.toString());
    private final VBox mBox;
    private final HBox mButtonBox;
    private final Button mClearButton = new Button(Dict.CLEAR.toString());
    private final Button mCopyNamesButton = new Button(Dict.COPY_NAMES.toString());
    private final DelayedResetRunner mDelayedResetRunner;
    private final Button mPasteNameButton = new Button("%s %s".formatted(Dict.PASTE.toString(), Dict.NAME.toLower()));
    private final CheckBox mPolygonFilterCheckBox = new CheckBox(MDict.USE_GEO_FILTER.toString());
    private final MPolygonFilterManager mPolygonFilterManager = MPolygonFilterManager.getInstance();
    private SessionManager mSessionManager;

    public MFilterPopOver() {
        String title = Dict.FILTER.toString();
        setTitle(title);
        getAction().setText(title);
        getAction().setGraphic(MaterialIcon._Content.FILTER_LIST.getImageView(getIconSizeToolBarInt()));

        mAllButton.setOnAction(event -> {
            reset();
        });
        mClearButton.setOnAction(event -> {
            clear();
        });

        mAllButton.setPrefWidth(WIDTH);
        mClearButton.setPrefWidth(WIDTH);
        mButtonBox = new HBox(GAP, mAllButton, mClearButton);
        mButtonBox.setAlignment(Pos.CENTER);

        mBox = new VBox(FxHelper.getUIScaled(8),
                mButtonBox,
                mPolygonFilterCheckBox
        );

        mDelayedResetRunner = new DelayedResetRunner(500, () -> {
            onPolygonFilterChange();
        });

        mPolygonFilterManager.addListener(() -> {
            mDelayedResetRunner.reset();
        });

        mPolygonFilterCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            mDelayedResetRunner.reset();
        });
    }

    public void activateCopyNames(EventHandler<ActionEvent> eventHandler) {
        mCopyNamesButton.setOnAction(eventHandler);
    }

    public void activatePasteName(EventHandler<ActionEvent> eventHandler) {
        mPasteNameButton.setOnAction(eventHandler);
    }

    public abstract void clear();

    public void copyNames(List<String> names) {
        SystemHelper.copyToClipboard(String.join("\n", names) + "\n");
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    public Node getButtonBox() {
        return mBox;
    }

    public Button getCopyNamesButton() {
        return mCopyNamesButton;
    }

    public Button getPasteNameButton() {
        return mPasteNameButton;
    }

    public CheckBox getPolygonFilterCheckBox() {
        return mPolygonFilterCheckBox;
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(NbPreferences.forModule(getClass()));
        }

        return mSessionManager;
    }

    public boolean isPolygonFilters() {
        return mPolygonFilterCheckBox.isSelected();
    }

    public abstract void onPolygonFilterChange();

    public abstract void reset();

}
