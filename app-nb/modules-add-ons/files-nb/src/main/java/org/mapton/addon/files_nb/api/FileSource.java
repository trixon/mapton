/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.files_nb.api;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.util.ArrayList;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class FileSource {

    @SerializedName("file")
    private File mFile;
    @SerializedName("recursive")
    private boolean mRecursive = true;
    @SerializedName("visible")
    private boolean mVisible = true;

    public FileSource() {
    }

    public FileSource(File file) {
        mFile = file;
    }

    public void fitToBounds() {
        ArrayList<MLatLon> latLons = new ArrayList<>();

        //TODO
        if (!latLons.isEmpty()) {
            MLatLonBox latLonBox = new MLatLonBox(latLons);
            Mapton.getEngine().fitToBounds(latLonBox);
        }
    }

    public File getFile() {
        return mFile;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setFile(File file) {
        mFile = file;
    }

    public void setRecursive(boolean recursive) {
        mRecursive = recursive;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    @Override
    public String toString() {
        return mFile.getName();
    }

}
