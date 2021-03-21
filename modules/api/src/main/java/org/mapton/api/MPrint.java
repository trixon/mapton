/*
 * Copyright 2021 Patrik Karlström.
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import se.trixon.almond.util.GlobalState;

public class MPrint {

    private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss.SSS: ");
    private final GlobalState mGlobalState = Mapton.getGlobalState();
    private final String mKey;
    private boolean mUseTimestamps = true;

    public MPrint(String key) {
        mKey = key;
    }

    public void err(String x) {
        mGlobalState.put(mKey, getDatedString("* " + x));
    }

    public void err(Object x) {
        if (x == null) {
            err("NULL");
        } else {
            err(String.valueOf(x));
        }
    }

    public boolean isUseTimestamps() {
        return mUseTimestamps;
    }

    public void out(String x) {
        mGlobalState.put(mKey, getDatedString(x));
    }

    public void out(Object x) {
        if (x == null) {
            out("NULL");
        } else {
            out(String.valueOf(x));
        }
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
        mDateFormat = dateFormat;
    }

    public void setUseTimestamps(boolean useTimestamps) {
        mUseTimestamps = useTimestamps;
    }

    private String getDatedString(String s) {
        if (mUseTimestamps) {
            return mDateFormat.format(Calendar.getInstance().getTime()) + s;
        } else {
            return s;
        }
    }
}
