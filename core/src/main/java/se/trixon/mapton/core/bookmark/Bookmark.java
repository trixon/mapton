/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.bookmark;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author Patrik Karlström
 */
public class Bookmark {

    private final StringProperty mCategory = new SimpleStringProperty();
    private final StringProperty mDescription = new SimpleStringProperty();
    private final BooleanProperty mDisplayMarker = new SimpleBooleanProperty();
    private final LongProperty mId = new SimpleLongProperty();
    private final DoubleProperty mLatitude = new SimpleDoubleProperty();
    private final DoubleProperty mLongitude = new SimpleDoubleProperty();
    private final StringProperty mName = new SimpleStringProperty();
    private final LongProperty mTimeAccessed = new SimpleLongProperty();
    private final LongProperty mTimeCreated = new SimpleLongProperty();
    private final LongProperty mTimeModified = new SimpleLongProperty();
    private final IntegerProperty mZoom = new SimpleIntegerProperty();

    public Bookmark() {
        mId.set(System.currentTimeMillis());
        mTimeCreated.set(getId());
    }

    public final StringProperty categoryProperty() {
        return mCategory;
    }

    public final StringProperty descriptionProperty() {
        return mDescription;
    }

    public BooleanProperty displayMarker() {
        return mDisplayMarker;
    }

    public final String getCategory() {
        return mCategory.get();
    }

    public final String getDescription() {
        return mDescription.get();
    }

    public final boolean getDisplayMarker() {
        return mDisplayMarker.get();
    }

    public final long getId() {
        return mId.get();
    }

    public final double getLatitude() {
        return mLatitude.get();
    }

    public final double getLongitude() {
        return mLongitude.get();
    }

    public final String getName() {
        return mName.get();
    }

    public final long getTimeAccessed() {
        return mTimeAccessed.get();
    }

    public final long getTimeCreated() {
        return mTimeCreated.get();
    }

    public final long getTimeModified() {
        return mTimeModified.get();
    }

    public final int getZoom() {
        return mZoom.get();
    }

    public IntegerProperty getZoomProperty() {
        return mZoom;
    }

    public final LongProperty idProperty() {
        return mId;
    }

    public DoubleProperty latitudeProperty() {
        return mLatitude;
    }

    public DoubleProperty longitudeProperty() {
        return mLongitude;
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public final void setCategory(String value) {
        mCategory.set(value);
    }

    public final void setDescription(String value) {
        mDescription.set(value);
    }

    public final void setDisplayMarker(boolean value) {
        mDisplayMarker.set(value);
    }

    public final void setId(long value) {
        mId.set(value);
    }

    public final void setLatitude(double value) {
        mLatitude.set(value);
    }

    public final void setLongitude(double value) {
        mLongitude.set(value);
    }

    public final void setName(String value) {
        mName.set(value);
    }

    public final void setTimeAccessed(long value) {
        mTimeAccessed.set(value);
    }

    public final void setTimeCreated(long value) {
        mTimeCreated.set(value);
    }

    public final void setTimeModified(long value) {
        mTimeModified.set(value);
    }

    public final void setZoom(int value) {
        mZoom.set(value);
    }

    public final LongProperty timeAccessedProperty() {
        return mTimeAccessed;
    }

    public final LongProperty timeCreatedProperty() {
        return mTimeCreated;
    }

    public final LongProperty timeModifiedProperty() {
        return mTimeModified;
    }

}
