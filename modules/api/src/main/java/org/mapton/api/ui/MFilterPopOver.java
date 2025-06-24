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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.mapton.api.MCircleFilterManager;
import org.mapton.api.MDict;
import org.mapton.api.MOptions;
import org.mapton.api.MPolygonFilterManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.FormFilter;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxActionCheck;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MFilterPopOver extends MPopOver {

    private final Button mAllButton = new Button(Dict.DEFAULT.toString());
    private final Map<String, Action> mAvailableActions = new HashMap<>();
    private final VBox mBox;
    private final HBox mButtonBox;
    private final MCircleFilterManager mCircleFilterManager = MCircleFilterManager.getInstance();
    private final Button mClearButton = new Button(Dict.CLEAR.toString());
    private final Button mCopyNamesButton = new Button(Dict.COPY_NAMES.toString());
    private final DelayedResetRunner mDelayedResetRunner;
    private FormFilter mFilter;
    private final TreeSet<String> mFilteredNames = new TreeSet<>();
    private final TreeSet<String> mMemorySet = new TreeSet<>();
    private final Button mPasteNameButton = new Button("%s %s".formatted(Dict.PASTE.toString(), Dict.NAME.toLower()));
    private FxActionCheck mPolygonFilterAction;
    private final CheckBox mPolygonFilterCheckBox = new CheckBox(MDict.USE_GEO_FILTER.toString());
    private final MPolygonFilterManager mPolygonFilterManager = MPolygonFilterManager.getInstance();
    private SessionManager mSessionManager;
    private ToolBar mToolBar;
    private final SimpleBooleanProperty mUsePolygonFilterProperty = new SimpleBooleanProperty();

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

        mCircleFilterManager.addListener(() -> {
            mDelayedResetRunner.reset();
        });

        mPolygonFilterManager.addListener(() -> {
            mDelayedResetRunner.reset();
        });

        mPolygonFilterCheckBox.selectedProperty().addListener((p, o, n) -> {
            mUsePolygonFilterProperty.set(mPolygonFilterCheckBox.isSelected());

            mDelayedResetRunner.reset();
        });

        initToolbar();
    }

    public void activateCopyNames(EventHandler<ActionEvent> eventHandler) {
        mCopyNamesButton.setOnAction(eventHandler);
    }

    public void activatePasteName(EventHandler<ActionEvent> eventHandler) {
        mPasteNameButton.setOnAction(eventHandler);
    }

    public void addToToolBar(String key, ActionTextBehavior actionTextBehavior) {
        var action = mAvailableActions.get(key);
        if (action != null) {
            var button = ActionUtils.createButton(action, actionTextBehavior);
            mToolBar.getItems().add(button);

            if (actionTextBehavior == ActionTextBehavior.SHOW) {
                var color = MOptions.getInstance().getIconColor();
                var fontSize = FxHelper.getScaledFontSize();
                var fontStyle = "-fx-font-size: %.0fpx; -fx-font-weight: %s;-fx-text-fill: %s;";

                button.setStyle(fontStyle.formatted(fontSize * 1.21, "normal", FxHelper.colorToString(color)));
            }

        }
    }

    public abstract void clear();

    public void copyNames(List<String> names) {
        SystemHelper.copyToClipboard(String.join("\n", names) + "\n");
    }

    public void filterPresetRestore(Preferences preferences) {

    }

    public void filterPresetStore(Preferences preferences) {

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

    @Deprecated(forRemoval = true)
    public CheckBox getPolygonFilterCheckBox() {
        return mPolygonFilterCheckBox;
    }

    public SessionManager getSessionManager() {
        if (mSessionManager == null) {
            mSessionManager = new SessionManager(NbPreferences.forModule(getClass()));
        }

        return mSessionManager;
    }

    public ToolBar getToolBar() {
        return mToolBar;
    }

    @Deprecated(forRemoval = true)
    public boolean isPolygonFilters() {
        return mUsePolygonFilterProperty.get();
    }

    public abstract void onPolygonFilterChange();

    public abstract void reset();

    public void setFilter(FormFilter filter) {
        mFilter = filter;
    }

    public void setNames(List list) {
        mFilteredNames.clear();
        mFilteredNames.addAll(list);
    }

    public void setUsePolygonFilter(boolean selected) {
        mPolygonFilterAction.setSelected(selected);
        mPolygonFilterCheckBox.setSelected(selected);
        mUsePolygonFilterProperty.set(selected);
    }

    public SimpleBooleanProperty usePolygonFilterProperty() {
        return mUsePolygonFilterProperty;
    }

    private void initToolbar() {
        var allAction = new Action(Dict.DEFAULT.toString(), actionEvent -> {
            reset();
        });
        allAction.setGraphic(MaterialIcon._Content.SELECT_ALL.getImageView(getIconSizeToolBarInt()));

        var clearAction = new Action(Dict.CLEAR.toString(), actionEvent -> {
            clear();
        });
        clearAction.setGraphic(MaterialIcon._Content.CLEAR.getImageView(getIconSizeToolBarInt()));

        mPolygonFilterAction = new FxActionCheck(MDict.USE_GEO_FILTER.toString(), actionEvent -> {
            mUsePolygonFilterProperty.set(mPolygonFilterAction.isSelected());
            mDelayedResetRunner.reset();
        });
        mPolygonFilterAction.setGraphic(MaterialIcon._Editor.FORMAT_SHAPES.getImageView(getIconSizeToolBarInt()));

        var copyNamesAction = new Action(Dict.COPY_NAMES.toString(), actionEvent -> {
            mCopyNamesButton.fire();
        });
        copyNamesAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeToolBarInt()));

        var s = "%s %s".formatted(Dict.PASTE.toString(), Dict.NAME.toLower());
        var pasteNameAction = new Action(s, actionEvent -> {
            mPasteNameButton.fire();
        });
        pasteNameAction.setGraphic(MaterialIcon._Content.CONTENT_PASTE.getImageView(getIconSizeToolBarInt()));

        var mcAction = new Action("MC", actionEvent -> {
            mMemorySet.clear();
        });
        var mrAction = new Action("MR", actionEvent -> {
            mFilter.freeTextProperty().set(String.join("\n", mMemorySet));
        });
        var mmAction = new Action("M-", actionEvent -> {
            mMemorySet.removeAll(mFilteredNames);
        });
        var mpAction = new Action("M+", actionEvent -> {
            mMemorySet.addAll(mFilteredNames);
        });

        var actions = List.of(
                allAction,
                clearAction,
                ActionUtils.ACTION_SEPARATOR,
                mPolygonFilterAction,
                ActionUtils.ACTION_SEPARATOR
        );

        mAvailableActions.put("copyNames", copyNamesAction);
        mAvailableActions.put("paste", pasteNameAction);
        mAvailableActions.put("mc", mcAction);
        mAvailableActions.put("mr", mrAction);
        mAvailableActions.put("mm", mmAction);
        mAvailableActions.put("mp", mpAction);

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.slimToolBar(mToolBar);
    }

}
