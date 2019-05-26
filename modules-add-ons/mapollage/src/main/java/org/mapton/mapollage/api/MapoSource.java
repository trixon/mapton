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
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSource {

    private transient File mCacheDir;
    private transient MapoCollection mCollection = new MapoCollection();

    @SerializedName("descriptionString")
    private String mDescriptionString;
    @SerializedName("source")
    private File mDir = SystemUtils.getUserHome();
    @SerializedName("exclude_pattern")
    private String mExcludePattern = "";
    @SerializedName("file_pattern")
    private String mFilePattern = "{*.jpg,*.JPG}";
    @SerializedName("follow_links")
    private boolean mFollowLinks = true;
    @SerializedName("id")
    private Long mId;
    @SerializedName("name")
    private String mName;
    private transient PathMatcher mPathMatcher;
    @SerializedName("recursive")
    private boolean mRecursive = true;
    @SerializedName("visible")
    private boolean mVisible = true;

    public MapoSource() {
    }

    public void fitToBounds() {
//        MLatLonBox latLonBox = new MLatLonBox(southWest, northEast);
//        Mapton.getEngine().fitToBounds(latLonBox);
    }

    public File getCacheDir() {
        if (mCacheDir == null) {
            mCacheDir = new File(Mapton.getCacheDir(), "mapollage");
        }

        return mCacheDir;
    }

    public MapoCollection getCollection() {
        return mCollection;
    }

    public String getDescriptionString() {
        return mDescriptionString;
    }

    public File getDir() {
        return mDir;
    }

    public String getExcludePattern() {
        return mExcludePattern;
    }

    public String getFilePattern() {
        return mFilePattern;
    }

    public Long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public PathMatcher getPathMatcher() {
        return mPathMatcher;
    }

    public boolean isFollowLinks() {
        return mFollowLinks;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    public boolean isValid() {
        try {
            mPathMatcher = FileSystems.getDefault().getPathMatcher("glob:" + mFilePattern);
        } catch (Exception e) {
            //addValidationError("invalid file pattern: " + mFilePattern);
        }

        return true;
    }

    public boolean isVisible() {
        return mVisible;
    }

    public MapoCollection loadCollection() throws IOException {
        return Mapo.getGson().fromJson(FileUtils.readFileToString(getCollectionFile(), "utf-8"), MapoCollection.class);
    }

    public void save(MapoCollection collection) throws IOException {
        FileUtils.write(getCollectionFile(), Mapo.getGson().toJson(collection), "utf-8");
    }

    public void setCollection(MapoCollection collection) {
        mCollection = collection;
    }

    public void setDescriptionString(String descriptionString) {
        mDescriptionString = descriptionString;
    }

    public void setDir(File dir) {
        mDir = dir;
    }

    public void setExcludePattern(String excludePattern) {
        mExcludePattern = excludePattern;
    }

    public void setFilePattern(String filePattern) {
        mFilePattern = filePattern;
    }

    public void setFollowLinks(boolean followLinks) {
        mFollowLinks = followLinks;
    }

    public void setId(Long id) {
        mId = id;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPathMatcher(PathMatcher pathMatcher) {
        mPathMatcher = pathMatcher;
    }

    public void setRecursive(boolean recursive) {
        mRecursive = recursive;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    @Override
    public String toString() {
        return mName;
    }

    private File getCollectionFile() {
        return new File(getCacheDir(), String.format("%d.json", getId()));
    }
}
