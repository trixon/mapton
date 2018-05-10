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
package se.trixon.mapton.core;

import javafx.scene.Scene;
import se.trixon.almond.nbp.fx.FxPanel;

/**
 *
 * @author Patrik Karlström
 */
public class AppStatusPanel extends FxPanel {

    private static AppStatusPanel sAppStatusPanel;

    private AppStatusView mAppStatusView = new AppStatusView();

    public static AppStatusPanel getInstance() {
        return sAppStatusPanel;
    }

    public AppStatusPanel() {
        sAppStatusPanel = this;
        setPreferredSize(null);
        initFx(null);
    }

    public AppStatusView getView() {
        return mAppStatusView;
    }

    public void setStatusText(String text) {
        mAppStatusView.setText(text);
    }

    @Override
    protected void fxConstructor() {
        setScene(createScene());
    }

    private Scene createScene() {
        return new Scene(mAppStatusView);
    }
}
