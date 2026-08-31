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

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.ui.forms.FormHelper;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import static org.mapton.butterfly_core.api.BFilterSectionMeas.MeasElement.*;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionMeas extends MBaseFilterSection {

    private static final int DEFAULT_NUM_OF_VALUE = 1;
    private final SessionCheckComboBox<String> mCodeSccb = new SessionCheckComboBox<>(true);
    private final CheckBox mLatestOperatorCheckbox = new CheckBox();
    private final CheckBox mNumOfCheckbox = new CheckBox();
    private final SessionIntegerSpinner mNumOfSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, DEFAULT_NUM_OF_VALUE);
    private final SessionCheckComboBox<String> mOperatorSccb = new SessionCheckComboBox<>();
    private final GridPane mRoot = new GridPane(GAP_H, GAP_V * 4);
    private final Node mSpecificNode;
    private final StackPane mSpecificPlacHolder = new StackPane();

    public BFilterSectionMeas() {
        this(null);
    }

    public BFilterSectionMeas(Region specificNode) {
        super(SDict.MEASUREMENTS.toString());
        if (specificNode == null) {
            mSpecificNode = mSpecificPlacHolder;
        } else {
            mSpecificNode = specificNode;
        }
        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        FxHelper.setSelected(false,
                mLatestOperatorCheckbox,
                mNumOfCheckbox
        );
        mNumOfSis.getValueFactory().setValue(DEFAULT_NUM_OF_VALUE);
        SessionCheckComboBox.clearChecks(
                mOperatorSccb,
                mCodeSccb
        );
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(getTab().getText().toUpperCase(Locale.ROOT), ".");
        map.put(getBundle().getString("measCodeCheckComboBoxTitle"),
                makeInfo(mCodeSccb.getCheckModel().getCheckedItems()));
        if (mNumOfCheckbox.isSelected()) {
            var value = mNumOfSis.getValue();
            map.put(getBundle().getString("numOfMeasCheckBoxText"),
                    FormHelper.negPosToLtGt(value));
        }
    }

    public void disable(MeasElement... elements) {
        var map = new HashMap<MeasElement, Node>();
        map.put(CODES, mCodeSccb);
        map.put(OPERATOR, mOperatorSccb);
        map.put(COUNT, mNumOfSis);

        for (var element : elements) {
            map.get(element).setDisable(true);
        }
    }

    public boolean filter(BXyzPoint p) {
        if (isSelected()) {
            var valid = true
                    && validateCode(p)
                    && validateOperators(p)
                    && validateCount(p);

            return valid;
        } else {
            return true;
        }
    }

    public ResourceBundle getBundle() {
        return NbBundle.getBundle(getClass());
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                mNumOfCheckbox.selectedProperty(),
                mLatestOperatorCheckbox.selectedProperty(),
                mNumOfSis.sessionValueProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

        List.of(
                mOperatorSccb.getCheckModel(),
                mCodeSccb.getCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("checkedCode"), mCodeSccb.checkedStringProperty());
        sessionManager.register(getKeyFilter("CheckedOperators"), mOperatorSccb.checkedStringProperty());
        sessionManager.register(getKeyFilter("latestOperator"), mLatestOperatorCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("numOf"), mNumOfCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("numOfValue"), mNumOfSis.sessionValueProperty());
    }

    public void load(ArrayList<? extends BXyzPoint> items) {
        mOperatorSccb.loadAndRestoreCheckItems(items.stream().flatMap(p -> p.extOrNull().getObservationsAllRaw().stream().map(o -> o.getOperator())));
        mCodeSccb.loadAndRestoreCheckItems();
        mNumOfSis.load();
        mNumOfSis.disableProperty().bind(mNumOfCheckbox.selectedProperty().not());
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mCodeSccb,
                mOperatorSccb
        );
        mCodeSccb.setTitle(getBundle().getString("measCodeCheckComboBoxTitle"));
        mOperatorSccb.setTitle(SDict.SURVEYORS.toString());
        mCodeSccb.getItems().setAll(List.of(
                getBundle().getString("measCodeZeroIs"),
                getBundle().getString("measCodeZero"),
                getBundle().getString("measCodeReplacement"),
                getBundle().getString("measCodeReplacementNot")
        ));
        mNumOfSis.getValueFactory().setConverter(new NegPosStringConverterInteger());
        mLatestOperatorCheckbox.setText(getBundle().getString("measLatesOperatorCheckBoxText"));
        mNumOfCheckbox.setText(getBundle().getString("numOfMeasCheckBoxText"));
        var spinners = new Spinner[]{
            mNumOfSis
        };
        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);

        var miscBox = new VBox(GAP_V, new VBox(titleGap, mNumOfCheckbox, mNumOfSis), new Separator(), mCodeSccb, new VBox(titleGap, mOperatorSccb, mLatestOperatorCheckbox));
        BindingHelper.bindWidthForChildrens(miscBox);
        BindingHelper.bindWidthForRegions(miscBox, mNumOfSis, mOperatorSccb);

        int row = 0;
        var wrappedSpecific = wrapInTitleBorder("Specifikt", mSpecificNode);
        FxHelper.autoSizeColumn(mRoot, 2);

        var specificLabel = new Label("Saknar innehåll");
        specificLabel.setDisable(true);
        mSpecificPlacHolder.getChildren().add(specificLabel);

        mRoot.addRow(row, wrapInTitleBorder("Generellt", miscBox), wrappedSpecific);
    }

    private boolean validateCode(BXyzPoint p) {
        var model = mCodeSccb.getCheckModel();
        var ext = p.extOrNull();
        if (model.isEmpty() || ext.getObservationsAllRaw().isEmpty()) {
            return true;
        }

        var firstIsZero = ext.getObservationRawFirstDate().equals(p.getDateZero());
        var chkIsFirstZero = model.isChecked(getBundle().getString("measCodeZeroIs"));
        var chkIsFirstZeroNot = model.isChecked(getBundle().getString("measCodeZero"));
        var chkHasReplacements = model.isChecked(getBundle().getString("measCodeReplacement"));
        var chkHasReplacementsNot = model.isChecked(getBundle().getString("measCodeReplacementNot"));

        var validFirstIsZero = chkIsFirstZero ? firstIsZero : true;
        var validFirstIsZeroNot = chkIsFirstZeroNot ? !firstIsZero : true;
        var validHasReplacements = chkHasReplacements ? ext.getNumOfReplacementsAfterZero() > 0 : true;
        var validHasReplacementsNot = chkHasReplacementsNot ? ext.getNumOfReplacementsAfterZero() == 0 : true;

        var valid = validFirstIsZero
                && validFirstIsZeroNot
                && validHasReplacements
                && validHasReplacementsNot;

        return valid;
    }

    private boolean validateCount(BXyzPoint p) {
        if (!mNumOfCheckbox.isSelected()) {
            return true;
        }

        var lim = mNumOfSis.getValue();
        var value = p.extOrNull().getObservationsAllRaw().size();

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim) && value != 0;
        } else if (lim > 0) {
            return value >= lim;
        }

        return true;
    }

    private boolean validateOperators(BXyzPoint p) {
        var model = mOperatorSccb.getCheckModel();
        var ext = p.extOrNull();
        if (model.isEmpty()) {
            return true;
        }

        if (mLatestOperatorCheckbox.isSelected()) {
            return model.getCheckedItems().contains(ext.getObservationsAllRaw().getLast().getOperator());
        } else {
            var pointOperators = ext.getObservationsAllRaw().stream().map(o -> o.getOperator()).collect(Collectors.toSet());

            for (var operator : model.getCheckedItems()) {
                if (pointOperators.contains(operator)) {
                    return true;
                }
            }

            return false;
        }
    }

    public enum MeasElement {
        COUNT,
        CODES,
        OPERATOR;
    }

}
