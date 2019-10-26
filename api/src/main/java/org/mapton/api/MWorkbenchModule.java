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
package org.mapton.api;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import javafx.scene.control.Control;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.stage.Stage;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.GlobalState;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MWorkbenchModule extends WorkbenchModule {

    protected final Logger LOGGER = Logger.getLogger(getClass().getName());
    protected final GlobalState mGlobalState = Mapton.getGlobalState();
    protected final MOptions2 mOptions2 = MOptions2.getInstance();
    protected final Preferences mPreferences;
    private ObservableMap<KeyCombination, Runnable> mAccelerators;
    private ResourceBundle mBundle;
    private final HashSet<KeyCodeCombination> mKeyCodeCombinations = new HashSet<>();
    private Scene mScene;
    private Stage mStage;

    public MWorkbenchModule(String name, Image icon) {
        super(name, icon);
        mPreferences = NbPreferences.forModule(getClass()).node(getClass().getCanonicalName());

        initListeners();
    }

    public ObservableMap<KeyCombination, Runnable> getAccelerators() {
        if (mAccelerators == null) {
            mAccelerators = getScene().getAccelerators();
        }

        return mAccelerators;
    }

    public ResourceBundle getBundle() {
        if (mBundle == null) {
            mBundle = NbBundle.getBundle(getClass());
        }

        return mBundle;
    }

    public String getBundleString(String key) {
        return getBundle().getString(key);
    }

    public HashSet<KeyCodeCombination> getKeyCodeCombinations() {
        return mKeyCodeCombinations;
    }

    public Scene getScene() {
        if (mScene == null) {
            mScene = getWorkbench().getScene();
        }

        return mScene;
    }

    public Stage getStage() {
        if (mStage == null) {
            mStage = (Stage) getScene().getWindow();
        }

        return mStage;
    }

    public void postInit() {
        setNightMode(mOptions2.general().isNightMode());
    }

    public void setNightMode(boolean state) {
    }

    public void setTooltip(Control control, String string) {
        control.setTooltip(new Tooltip(string));
    }

    public void setTooltip(Control control, String string, KeyCodeCombination keyCodeCombination) {
        control.setTooltip(new Tooltip(String.format("%s (%s)", string, keyCodeCombination.getDisplayText())));
    }

    private void initListeners() {

        mOptions2.general().nightModeProperty().addListener((observable, oldValue, newValue) -> setNightMode(newValue));
    }
}
