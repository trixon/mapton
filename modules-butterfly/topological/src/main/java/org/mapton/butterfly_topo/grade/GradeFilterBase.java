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
package org.mapton.butterfly_topo.grade;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.topo.BTopoGrade;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GradeFilterBase extends FormFilter<GradeManagerBase> {

    DoubleProperty mDabbaHMaxProperty = new SimpleDoubleProperty();
    DoubleProperty mDabbaHMinProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mDabbaHSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mDabbaRMaxProperty = new SimpleDoubleProperty();
    DoubleProperty mDabbaRMinProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mDabbaRSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mDeltaHMaxProperty = new SimpleDoubleProperty();
    DoubleProperty mDeltaHMinProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mDeltaHSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mDeltaRMaxProperty = new SimpleDoubleProperty();
    DoubleProperty mDeltaRMinProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mDeltaRSelectedProperty = new SimpleBooleanProperty();
    SimpleBooleanProperty mGradeHorizontalSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mGradeHorizontalValueProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mGradeVerticalSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mGradeVerticalValueProperty = new SimpleDoubleProperty();
    private final SimpleDoubleProperty mMaxDoubleProperty = new SimpleDoubleProperty(Double.MAX_VALUE);

    public GradeFilterBase(GradeManagerBase manager) {
        super(manager);
    }

    public void initPropertyListeners() {
        mDeltaHSelectedProperty.addListener(mChangeListenerObject);
        mDeltaHMinProperty.addListener(mChangeListenerObject);
        mDeltaHMaxProperty.addListener(mChangeListenerObject);

        mDeltaRSelectedProperty.addListener(mChangeListenerObject);
        mDeltaRMinProperty.addListener(mChangeListenerObject);
        mDeltaRMaxProperty.addListener(mChangeListenerObject);

        mDabbaHSelectedProperty.addListener(mChangeListenerObject);
        mDabbaHMinProperty.addListener(mChangeListenerObject);
        mDabbaHMaxProperty.addListener(mChangeListenerObject);

        mDabbaRSelectedProperty.addListener(mChangeListenerObject);
        mDabbaRMinProperty.addListener(mChangeListenerObject);
        mDabbaRMaxProperty.addListener(mChangeListenerObject);

        mGradeHorizontalSelectedProperty.addListener(mChangeListenerObject);
        mGradeHorizontalValueProperty.addListener(mChangeListenerObject);

        mGradeVerticalSelectedProperty.addListener(mChangeListenerObject);
        mGradeVerticalValueProperty.addListener(mChangeListenerObject);
    }

    protected boolean validateDabbaH(BTopoGrade p) {
        if (mDabbaHSelectedProperty.get()) {
            return inRange(p.getDistanceHeight(), mDabbaHMinProperty, mDabbaHMaxProperty);
        } else {
            return true;
        }
    }

    protected boolean validateDabbaR(BTopoGrade p) {
        if (mDabbaRSelectedProperty.get()) {
            return inRange(p.getDistancePlane(), mDabbaRMinProperty, mDabbaRMaxProperty);
        } else {
            return true;
        }
    }

    protected boolean validateDeltaH(BTopoGrade p) {
        if (mDeltaHSelectedProperty.get()) {
            return inRange(p.getDistanceHeight(), mDeltaHMinProperty, mDeltaHMaxProperty);
        } else {
            return true;
        }
    }

    protected boolean validateDeltaR(BTopoGrade p) {
        if (mDeltaRSelectedProperty.get()) {
            return inRange(p.getDistancePlane(), mDeltaRMinProperty, mDeltaRMaxProperty);
        } else {
            return true;
        }
    }

    protected boolean validateGradeHorizontal(BTopoGrade p) {
        if (mGradeHorizontalSelectedProperty.get()) {
            return inRange(p.ext().getDiff().getZPerMille(), mGradeHorizontalValueProperty, mMaxDoubleProperty);
        } else {
            return true;
        }
    }

    protected boolean validateGradeVertical(BTopoGrade p) {
        if (mGradeVerticalSelectedProperty.get()) {
            return inRange(p.ext().getDiff().getRPerMille(), mGradeVerticalValueProperty, mMaxDoubleProperty);
        } else {
            return true;
        }
    }

    private boolean inRange(double value, DoubleProperty minProperty, DoubleProperty maxProperty) {
        return value >= minProperty.get() && value <= maxProperty.get();
    }

}
