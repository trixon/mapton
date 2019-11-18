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
package org.mapton.workbench.modules;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import javafx.scene.Node;
import org.mapton.api.MWorkbenchModule;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.LogListener;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class LogModule extends MWorkbenchModule implements LogListener {

    private LogPanel mLogPanel;

    public LogModule() {
        super(Dict.LOG.toString(), MaterialDesignIcon.MESSAGE);

        createUI();
    }

    @Override
    public Node activate() {
        return mLogPanel;
    }

    @Override
    public boolean destroy() {
        return true;
    }

    @Override
    public void println(String s) {
        mLogPanel.println(s);
    }

    private void createUI() {
        mLogPanel = new LogPanel();
        mLogPanel.setMonospaced();
    }
}
