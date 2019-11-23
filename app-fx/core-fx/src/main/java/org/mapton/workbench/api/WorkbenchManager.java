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
package org.mapton.workbench.api;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchModule;
import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.workbench.CustomTab;
import org.mapton.workbench.modules.MapModule;
import org.mapton.workbench.modules.UpdaterModule;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class WorkbenchManager {

    private boolean mAllowModulePopulation = false;
    private final ArrayList<MWorkbenchModule> mFixModules = new ArrayList<>();
    private final Workbench mWorkbench;

    public static WorkbenchManager getInstance() {
        return Holder.INSTANCE;
    }

    private WorkbenchManager() {
        mWorkbench = Workbench.builder()
                .tabFactory(CustomTab::new)
                .modulesPerPage(24)
                .build();

        mWorkbench.getStylesheets().add(getClass().getResource("baseTheme.css").toExternalForm());
        mWorkbench.getNavigationDrawer().setVisible(false);
        initListeners();

        Platform.runLater(() -> {
            setNightMode(Mapton.optionsGeneral().isNightMode());
        });
    }

    public void activateModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        int pageIndex = 0;//TODO get actual page index
        int moduleIndex = pageIndex * mWorkbench.getModulesPerPage() + moduleIndexOnPage - 1;
        try {
            mWorkbench.openModule(mWorkbench.getModules().get(moduleIndex));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    public void activateOpenModule(int moduleIndexOnPage) {
        if (moduleIndexOnPage == 0) {
            moduleIndexOnPage = 10;
        }

        try {
            mWorkbench.openModule(mWorkbench.getOpenModules().get(moduleIndexOnPage - 1));
        } catch (IndexOutOfBoundsException e) {
            //nvm
        }
    }

    public void closeActiveModule() {
        if (mWorkbench.getActiveModule() != null && !(mWorkbench.getActiveModule() instanceof MapModule)) {
            mWorkbench.closeModule(mWorkbench.getActiveModule());
        }
    }

    public ArrayList<MWorkbenchModule> getFixModules() {
        return mFixModules;
    }

    public Workbench getWorkbench() {
        return mWorkbench;
    }

    public void openModule(Class<UpdaterModule> clazz) {
        for (WorkbenchModule module : mWorkbench.getModules()) {
            if (clazz.isInstance(module)) {
                mWorkbench.openModule(module);
            }
        }
    }

    public void populateModules() {
        if (!mAllowModulePopulation) {
            return;
        }

        var lookupModules = new ArrayList<>(Lookup.getDefault().lookupAll(MWorkbenchModule.class));
        Collections.sort(lookupModules, (MWorkbenchModule o1, MWorkbenchModule o2) -> o1.getName().compareToIgnoreCase(o2.getName()));

        MWorkbenchModule reportsModule = null;

        for (MWorkbenchModule module : lookupModules) {
            if (module.getClass().getName().equalsIgnoreCase("org.mapton.reports.ReportsModule")) {
                reportsModule = module;
            }
        }

        ArrayList<MWorkbenchModule> fixModules = new ArrayList<>(mFixModules);

        if (reportsModule != null) {
            lookupModules.remove(reportsModule);
            fixModules.add(reportsModule);
        }

        fixModules.addAll(lookupModules);
        for (MWorkbenchModule module : fixModules) {
            mWorkbench.getModules().add(module);
        }
    }

    public void setAllowModulePopulation(boolean allowModulePopulation) {
        mAllowModulePopulation = allowModulePopulation;
    }

    public void toggleNavigationDrawer() {
        if (mWorkbench.getNavigationDrawer().isVisible()) {
            mWorkbench.hideNavigationDrawer();
        } else {
            mWorkbench.showNavigationDrawer();
        }
    }

    private void initListeners() {
        Mapton.optionsGeneral().nightModeProperty().addListener((observable, oldValue, newValue) -> setNightMode(newValue));

        Lookup.getDefault().lookupResult(MWorkbenchModule.class).addLookupListener((LookupEvent ev) -> {
            Platform.runLater(() -> {
                populateModules();
            });
        });
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                Notifications notifications = evt.getValue();
                notifications.owner(mWorkbench).position(Pos.TOP_RIGHT);
                if (Mapton.optionsGeneral().isNightMode()) {
                    notifications.darkStyle();
                }
                switch (evt.getKey()) {
                    case MKey.NOTIFICATION:
                        notifications.show();
                        break;

                    case MKey.NOTIFICATION_CONFIRM:
                        notifications.showConfirm();
                        break;

                    case MKey.NOTIFICATION_ERROR:
                        notifications.showError();
                        break;

                    case MKey.NOTIFICATION_INFORMATION:
                        notifications.showInformation();
                        break;

                    case MKey.NOTIFICATION_WARNING:
                        notifications.showWarning();
                        break;

                    default:
                        throw new AssertionError();
                }
            });
        }, MKey.NOTIFICATION, MKey.NOTIFICATION_CONFIRM, MKey.NOTIFICATION_ERROR, MKey.NOTIFICATION_INFORMATION, MKey.NOTIFICATION_WARNING);
    }

    private void setNightMode(boolean state) {
        String lightTheme = getClass().getResource("lightTheme.css").toExternalForm();
        String darkTheme = getClass().getResource("darkTheme.css").toExternalForm();
        String darculaTheme = FxHelper.class.getResource("darcula.css").toExternalForm();

        ObservableList<String> stylesheets = mWorkbench.getStylesheets();

        if (state) {
            stylesheets.remove(lightTheme);
            stylesheets.add(darkTheme);
            stylesheets.add(darculaTheme);
        } else {
            stylesheets.remove(darkTheme);
            stylesheets.remove(darculaTheme);
            stylesheets.add(lightTheme);
        }
    }

    private static class Holder {

        private static final WorkbenchManager INSTANCE = new WorkbenchManager();
    }
}
