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
package org.mapton.butterfly.bcc;

import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class CmdConfig {

    private String[] mClassArgs;
    private String[] mClasses = new String[]{};
    private String mDestFile;
    private char[] mPassword;
    private String mResourceDir;
    private String mWorkingDir;

    public static CmdConfig getInstance() {
        return Holder.INSTANCE;
    }

    private CmdConfig() {
    }

    public String[] getClassArgs() {
        return mClassArgs;
    }

    public String[] getClasses() {
        return mClasses;
    }

    public String getDestFile() {
        return mDestFile;
    }

    public char[] getPassword() {
        return mPassword;
    }

    public String getResourceDir() {
        return mResourceDir;
    }

    public String getWorkingDir() {
        return mWorkingDir;
    }

    public boolean isValid() {
        var valid = ObjectUtils.allNotNull(mDestFile);

        return valid;
    }

    public void setClasses(String[] classes) {
        mClasses = new String[classes.length];
        var classArgs = new String[classes.length];

        for (int i = 0; i < classes.length; i++) {
            var item = StringUtils.split(classes[i], "=");
            mClasses[i] = Objects.toString(item[0], "");
            if (item.length > 1) {
                classArgs[i] = Objects.toString(item[1], "");
            }
        }

        mClassArgs = classArgs;
    }

    public void setDestFile(String destFile) {
        mDestFile = destFile;
    }

    public void setPassword(char[] password) {
        mPassword = password;
    }

    public void setResourceDir(String resourceDir) {
        mResourceDir = resourceDir;
    }

    public void setWorkingDir(String workingDir) {
        mWorkingDir = workingDir;
    }

    private static class Holder {

        private static final CmdConfig INSTANCE = new CmdConfig();
    }
}
