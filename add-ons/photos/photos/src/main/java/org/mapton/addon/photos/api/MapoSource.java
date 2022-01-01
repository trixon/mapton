/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.addon.photos.api;

import com.google.gson.annotations.SerializedName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class MapoSource {

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
    private transient final MapoSourceManager mManager = MapoSourceManager.getInstance();
    @SerializedName("name")
    private String mName;
    private transient PathMatcher mPathMatcher;
    @SerializedName("recursive")
    private boolean mRecursive = true;
    @SerializedName("thumbnail_border_color")
    private String mThumbnailBorderColor = "FFFF00";
    @SerializedName("thumbnail_border_size")
    private int mThumbnailBorderSize = 10;
    private transient File mThumbnailDir;
    @SerializedName("thumbnail_force_creation")
    private boolean mThumbnailForceCreation = false;
    @SerializedName("thumbnail_size")
    private int mThumbnailSize = 800;
    @SerializedName("visible")
    private boolean mVisible = true;

    public MapoSource() {
    }

    public void fitToBounds() {
        ArrayList<MLatLon> latLons = new ArrayList<>();

        for (MapoPhoto photo : getCollection().getPhotos()) {
            latLons.add(new MLatLon(photo.getLat(), photo.getLon()));
        }

        if (!latLons.isEmpty()) {
            MLatLonBox latLonBox = new MLatLonBox(latLons);
            Mapton.getEngine().fitToBounds(latLonBox);
        }
    }

    public MapoCollection getCollection() {
        return mCollection;
    }

    public File getCollectionFile() {
        return new File(mManager.getCacheDir(), String.format("%d.json", getId()));
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

    public String getThumbnailBorderColor() {
        return mThumbnailBorderColor;
    }

    public int getThumbnailBorderSize() {
        return mThumbnailBorderSize;
    }

    public File getThumbnailDir() {
        if (mThumbnailDir == null) {
            mThumbnailDir = new File(mManager.getCacheDir(), String.valueOf(getId()));
        }

        return mThumbnailDir;
    }

    public int getThumbnailSize() {
        return mThumbnailSize;
    }

    public boolean isFollowLinks() {
        return mFollowLinks;
    }

    public boolean isRecursive() {
        return mRecursive;
    }

    public boolean isThumbnailForceCreation() {
        return mThumbnailForceCreation;
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
        try {
            return Mapo.getGson().fromJson(FileUtils.readFileToString(getCollectionFile(), "utf-8"), MapoCollection.class);
        } catch (FileNotFoundException e) {
            return new MapoCollection();
        }
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

    public void setThumbnailBorderColor(String thumbnailBorderColor) {
        mThumbnailBorderColor = thumbnailBorderColor;
    }

    public void setThumbnailBorderSize(int thumbnailBorderSize) {
        mThumbnailBorderSize = thumbnailBorderSize;
    }

    public void setThumbnailForceCreation(boolean thumbnailForceCreation) {
        mThumbnailForceCreation = thumbnailForceCreation;
    }

    public void setThumbnailSize(int thumbnailSize) {
        mThumbnailSize = thumbnailSize;
    }

    public void setVisible(boolean visible) {
        mVisible = visible;
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", mName, getCollection().getPhotos().size());
    }

}
