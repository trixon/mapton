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
package org.mapton.mapollage.api;

import com.google.gson.annotations.SerializedName;
import java.time.LocalDate;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSettings {

    @SerializedName("high_date")
    private LocalDate mHighDate;
    @SerializedName("low_date")
    private LocalDate mLowDate;

    public LocalDate getHighDate() {
        return mHighDate;
    }

    public LocalDate getLowDate() {
        return mLowDate;
    }

    public void setHighDate(LocalDate highDate) {
        mHighDate = highDate;
    }

    public void setLowDate(LocalDate lowDate) {
        mLowDate = lowDate;
    }

}
