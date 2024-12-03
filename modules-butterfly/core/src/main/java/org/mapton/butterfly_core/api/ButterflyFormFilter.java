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

import java.time.LocalDate;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.ui.forms.FormFilter;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ButterflyFormFilter<ManagerType extends MBaseDataManager> extends FormFilter {

    public ButterflyFormFilter(MBaseDataManager manager) {
        super(manager);
    }

    public boolean validateAlarmName(BXyzPoint p, IndexedCheckModel checkModel) {
        var ah = p.getAlarm1Id();
        var ap = p.getAlarm2Id();

        switch (p.getDimension()) {
            case _1d -> {
                return validateCheck(checkModel, ah);
            }
            case _2d -> {
                return validateCheck(checkModel, ap);
            }
            case _3d -> {
                return validateCheck(checkModel, ah) && validateCheck(checkModel, ap);
            }
        }

        return true;
    }

    public boolean validateAlarmName1(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm1Id());
    }

    public boolean validateAlarmName2(BXyzPoint p, IndexedCheckModel checkModel) {
        return validateCheck(checkModel, p.getAlarm2Id());
    }

    public boolean validateNextMeas(BXyzPoint p, IndexedCheckModel<String> checkModel, long remainingDays) {
        var frequency = p.getFrequency();
        var latest = p.getDateLatest() != null ? p.getDateLatest().toLocalDate() : LocalDate.MIN;
        var today = LocalDate.now();
        var nextMeas = latest.plusDays(frequency);
//        var remainingDays = ;

        if (checkModel.isEmpty()) {
            return true;
        } else if (checkModel.isChecked("∞") && frequency == 0) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("<0") && nextMeas.isBefore(today)) {
            return true;
        } else if (frequency > 0 && checkModel.isChecked("0") && remainingDays == 0) {
            return true;
        } else {
            return checkModel.getCheckedItems().stream()
                    .filter(s -> StringUtils.countMatches(s, "-") == 1)
                    .anyMatch(s -> {
                        int start = Integer.parseInt(StringUtils.substringBefore(s, "-"));
                        int end = Integer.parseInt(StringUtils.substringAfter(s, "-"));
                        return remainingDays >= start && remainingDays <= end;
                    });
        }
    }

}
