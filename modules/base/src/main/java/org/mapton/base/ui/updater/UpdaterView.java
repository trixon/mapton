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
package org.mapton.base.ui.updater;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import org.mapton.api.MKey;
import org.mapton.api.MUpdater;
import org.mapton.api.Mapton;
import se.trixon.almond.util.LogListener;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterView implements LogListener {

    private final UpdaterListView mListView;
    private final LogPanel mLogPanel;
    private final BooleanProperty mRunningProperty = new SimpleBooleanProperty(false);
    private final BooleanProperty mSelectedProperty = new SimpleBooleanProperty(false);
    private final UpdaterMaskerPane mUpdaterMaskerPane;

    public UpdaterView() {
        mListView = new UpdaterListView();
        mUpdaterMaskerPane = new UpdaterMaskerPane();
        mRunningProperty.bind(mUpdaterMaskerPane.runningProperty());
        mSelectedProperty.bind(mListView.selectedProperty());
        mUpdaterMaskerPane.setContent(mListView);

        mLogPanel = new LogPanel();
        mLogPanel.setMonospaced();

        Mapton.getGlobalState().addListener(gsce -> {
            mLogPanel.println((String) gsce.getObject());
        }, MKey.UPDATER_LOGGER);
    }

    public void clear() {
        mLogPanel.clear();
    }

    public Node getListNode() {
        return mUpdaterMaskerPane.getNode();
    }

    public LogPanel getLogPanel() {
        return mLogPanel;
    }

    @Override
    public void println(String s) {
        mLogPanel.println(s);
    }

    public void refreshUpdaters() {
        mListView.refreshUpdaters();
    }

    public BooleanProperty runningProperty() {
        return mRunningProperty;
    }

    public BooleanProperty selectedProperty() {
        return mSelectedProperty;
    }

    public void update() {
        for (MUpdater updater : mListView.getItems()) {
            if (updater.isMarkedForUpdate()) {
                mUpdaterMaskerPane.update(mListView.getItems(), () -> {
                    refreshUpdaters();
                });
                break;
            }
        }
    }

}
