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
package org.mapton.butterfly_format.types.topo;

import javafx.geometry.Point3D;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoGradeObservation extends BXyzPointObservation {

    private final BTopoGrade mGrade;
    private final Point3D mPoint3d1;
    private final Point3D mPoint3d2;
    private Point3D mCoordinate1;
    private Point3D mCoordinate2;

    public BTopoGradeObservation(BTopoGrade grade, Point3D point3d1, Point3D point3d2) {
        mGrade = grade;
        mPoint3d1 = point3d1;
        mPoint3d2 = point3d2;
    }

    public Point3D getCoordinate1() {
        return mCoordinate1;
    }

    public Point3D getCoordinate2() {
        return mCoordinate2;
    }

    public Point3D getPoint3d1() {
        return mPoint3d1;
    }

    public Point3D getPoint3d2() {
        return mPoint3d2;
    }

    public void setCoordinate1(Point3D coordinate1) {
        mCoordinate1 = coordinate1;
    }

    public void setCoordinate2(Point3D coordinate2) {
        mCoordinate2 = coordinate2;
    }

}
