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
package org.mapton.api.ui.forms;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.LinkedHashMap;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.tools.Borders;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MBaseFilterSection {

    public static double GAP_H = FxHelper.getUIScaled(9.0);
    public static double GAP_V = FxHelper.getUIScaled(4.0);
    public double columnGap = FxHelper.getUIScaled(16);
    public final double mBorderInnerPadding = FxHelper.getUIScaled(8.0);
    public final double mTopBorderInnerPadding = FxHelper.getUIScaled(16.0);
    public double rowGap = FxHelper.getUIScaled(12);
    public double spinnerWidth = FxHelper.getUIScaled(70.0);
    public double titleGap = FxHelper.getUIScaled(3);
    private final CheckedTab mCheckedTab;
    private double mMaxWidth = FxHelper.getUIScaled(500);
    private SessionManager mSessionManager;

    public MBaseFilterSection(String title) {
        mCheckedTab = new CheckedTab(title, null);
    }

    public void clear() {
        try {
            mCheckedTab.getTabCheckBox().setSelected(true);
        } catch (NullPointerException e) {
        }
    }

    public void createInfoContent(LinkedHashMap<String, String> map) {
    }

    public Node getContent() {
        return mCheckedTab.getContent();
    }

    public double getMaxWidth() {
        return mMaxWidth;
    }

    public SessionManager getSessionManager() {
        return mSessionManager;
    }

    public CheckedTab getTab() {
        return mCheckedTab;
    }

    public boolean inRange(double value, DoubleProperty minProperty, DoubleProperty maxProperty) {
        return value >= minProperty.get() && value <= maxProperty.get();
    }

    public void initSession(SessionManager sessionManager) {

    }

    public boolean isSelected() {
        return selectedProperty().get();
    }

    public String makeInfo(ObservableList<String> list) {
        return String.join(",", list);
    }

    public String makeInfo(String s, String empty) {
        return StringUtils.equalsIgnoreCase(s, empty) ? "" : s;
    }

    public String makeInfoInteger(ObservableList<Integer> list) {
        return String.join(",", list.stream().map(o -> Integer.toString(o)).toList());
    }

    public abstract void onShownFirstTime();

    public abstract void reset(PropertiesConfiguration filterConfig);

    public BooleanProperty selectedProperty() {
        return mCheckedTab.getTabCheckBox().selectedProperty();
    }

    public void setContent(Node node) {
        if (node instanceof Region r) {
            r.setPadding(FxHelper.getUIScaledInsets(16));
        }

        mCheckedTab.setContent(node);
        mCheckedTab.getTabCheckBox().setSelected(true);
    }

    public void setMaxWidth(double maxWidth) {
        mMaxWidth = maxWidth;
    }

    public void setSessionManager(SessionManager sessionManager) {
        mSessionManager = sessionManager;
    }

    public boolean validateCheck(IndexedCheckModel checkModel, Object o) {
        return checkModel.isEmpty() || checkModel.isChecked(o);
    }

    public Node wrapInTitleBorder(String title, Node node) {
        return Borders.wrap(node)
                .etchedBorder()
                .title(title)
                .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();
    }
}
