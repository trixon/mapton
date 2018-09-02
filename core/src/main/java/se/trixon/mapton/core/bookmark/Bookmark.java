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

import java.sql.Timestamp;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
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
    private final ObjectProperty<Timestamp> mTimeAccessed = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> mTimeCreated = new SimpleObjectProperty<>();
    private final ObjectProperty<Timestamp> mTimeModified = new SimpleObjectProperty<>();
    private final DoubleProperty mZoom = new SimpleDoubleProperty();

    public Bookmark() {
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

    public final Timestamp getTimeAccessed() {
        return mTimeAccessed.get();
    }

    public final Timestamp getTimeCreated() {
        return mTimeCreated.get();
    }

    public final Timestamp getTimeModified() {
        return mTimeModified.get();
    }

    public final double getZoom() {
        return mZoom.get();
    }

    public DoubleProperty getZoomProperty() {
        return mZoom;
    }

    public final LongProperty idProperty() {
        return mId;
    }

    public final boolean isDisplayMarker() {
        return mDisplayMarker.get();
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

    public final void setTimeAccessed(Timestamp value) {
        mTimeAccessed.set(value);
    }

    public final void setTimeCreated(Timestamp value) {
        mTimeCreated.set(value);
    }

    public final void setTimeModified(Timestamp value) {
        mTimeModified.set(value);
    }

    public final void setZoom(double value) {
        mZoom.set(value);
    }

    public final ObjectProperty<Timestamp> timeAccessedProperty() {
        return mTimeAccessed;
    }

    public final ObjectProperty<Timestamp> timeCreatedProperty() {
        return mTimeCreated;
    }

    public final ObjectProperty<Timestamp> timeModifiedProperty() {
        return mTimeModified;
    }

}
