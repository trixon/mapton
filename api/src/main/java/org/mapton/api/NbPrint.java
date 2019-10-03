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

import java.text.SimpleDateFormat;

public class NbPrint {
//aaa

    public NbPrint(String title) {
    }

    public void err(String x) {
    }

    public void err(Object x) {
        if (x == null) {
            err("NULL");
        } else {
            err(String.valueOf(x));
        }
    }

    public synchronized boolean isUseTimestamps() {
        return false;
    }

    public void out(String x) {
    }

    public void out(Object x) {
        if (x == null) {
            NbPrint.this.out("NULL");
        } else {
            NbPrint.this.out(String.valueOf(x));
        }
    }

    public synchronized void select() {
    }

    public void setDateFormat(SimpleDateFormat dateFormat) {
    }

    public void setUseTimestamps(boolean useTimestamps) {
    }
}
