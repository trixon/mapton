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
package org.mapton.butterfly_core.api;

import java.util.ArrayList;
import org.mapton.api.Mapton;
import org.mapton.api.report.MReport;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseReport extends MReport {

    public BaseReport() {
        mSplitNavSetting.setTitleColor(Mapton.getThemeBackgroundColor());
    }

    public void addBlankRow(ArrayList<ArrayList<String>> rows) {
        rows.add(new ArrayList<>());
    }

    public void addRow(ArrayList<ArrayList<String>> rows, Object... objects) {
        var columns = new ArrayList<String>();
        for (var object : objects) {
            if (object == null) {
                columns.add("");
                continue;
            }

            if (object instanceof Integer value) {
                columns.add(Integer.toString(value));
            } else {
                columns.add(object.toString());
            }
        }
        rows.add(columns);
    }

}
