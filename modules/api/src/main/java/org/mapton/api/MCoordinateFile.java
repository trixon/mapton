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
package org.mapton.api;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import org.apache.commons.lang3.Strings;
import org.openide.util.Lookup;

/**
 * This class holds a coordinate transformation and a file.
 *
 * @author Patrik Karlström
 */
public class MCoordinateFile {

    private transient MCooTrans mCooTrans;
    @SerializedName("cooTrans")
    private String mCooTransString;
    @SerializedName("coordinateFileOpener")
    private String mCoordinateFileOpenerName;
    @SerializedName("file")
    private File mFile;
    @SerializedName("visible")
    private boolean mVisible = true;

    public MCoordinateFile() {
    }

    public MCooTrans getCooTrans() {
        if (mCooTrans == null) {
            if (mCooTransString == null) {
                mCooTrans = MOptions.getInstance().getMapCooTrans();
            } else {
                mCooTrans = MCooTrans.getCooTrans(mCooTransString);
            }
        }
        return mCooTrans;
    }

    public MCoordinateFileOpener getCoordinateFileOpener() {
        return Lookup.getDefault().lookupAll(MCoordinateFileOpener.class).stream()
                .filter(fo -> Strings.CI.equals(mCoordinateFileOpenerName, fo.getName()))
                .findAny().orElse(null);
    }

    public String getCoordinateFileOpenerName() {
        return mCoordinateFileOpenerName;
    }

    public File getFile() {
        return mFile;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setCooTrans(MCooTrans cooTrans) {
        mCooTransString = cooTrans.getName();
        mCooTrans = cooTrans;
    }

    public void setCoordinateFileOpenerName(String coordinateFileOpenerName) {
        mCoordinateFileOpenerName = coordinateFileOpenerName;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    @Override
    public String toString() {
        return mFile.getName();
    }

}
