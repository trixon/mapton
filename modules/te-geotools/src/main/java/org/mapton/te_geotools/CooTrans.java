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
package org.mapton.te_geotools;

import javafx.geometry.Point2D;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.mapton.api.MBounds;
import org.mapton.api.MCooTrans;
import org.opengis.geometry.DirectPosition;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.NoninvertibleTransformException;
import org.opengis.referencing.operation.TransformException;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class CooTrans implements MCooTrans {

    private final String mCrsCode;
    private MathTransform mInverseMathTransform;
    private MathTransform mMathTransform;
    private MBounds mSourceBounds;
    private final CoordinateReferenceSystem mSourceCrs = DefaultGeographicCRS.WGS84;
    private MBounds mTargetBounds;
    private CoordinateReferenceSystem mTargetCrs;

    public CooTrans(String crsCode) {
        mCrsCode = crsCode;

        try {
            init(crsCode);
        } catch (FactoryException | NoninvertibleTransformException ex) {
            Exceptions.printStackTrace(ex);
        } catch (TransformException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public Point2D fromWgs84(double latitude, double longitude) {
        var position = getPosition(mInverseMathTransform, latitude, longitude);

        return new Point2D(position.getCoordinate()[1], position.getCoordinate()[0]);
    }

    @Override
    public MBounds getBoundsProjected() {
        return mTargetBounds;
    }

    @Override
    public MBounds getBoundsWgs84() {
        return mSourceBounds;
    }

    public String getCrsCode() {
        return mCrsCode;
    }

    @Override
    public double getLatitude(double latitude, double longitude) {
        return getPosition(mMathTransform, latitude, longitude).getCoordinate()[0];
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        return String.format("%.1f N", getLatitude(latitude, longitude));
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return getPosition(mMathTransform, latitude, longitude).getCoordinate()[1];
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        return String.format("%.1f E", getLongitude(latitude, longitude));
    }

    @Override
    public String getName() {
        return StringUtils.removeStart(mTargetCrs.getName().toString(), "EPSG:");
    }

    public CoordinateReferenceSystem getProjectedCrs() {
        return mTargetCrs;
    }

    @Override
    public String getString(double latitude, double longitude) {
        if (isWithinWgs84Bounds(latitude, longitude)) {
            return String.format("%s  %s", getLatitudeString(latitude, longitude), getLongitudeString(latitude, longitude));
        } else {
            return Dict.OUT_OF_BOUNDS.toString();
        }
    }

    @Override
    public boolean isWithinProjectedBounds(double latitude, double longitude) {
        return mTargetBounds.contains(longitude, latitude);
    }

    @Override
    public boolean isWithinWgs84Bounds(double latitude, double longitude) {
        return mSourceBounds.contains(longitude, latitude);
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public Point2D toWgs84(double latitude, double longitude) {
        var position = getPosition(mInverseMathTransform, longitude, latitude);

        return new Point2D(position.getCoordinate()[0], position.getCoordinate()[1]);
    }

    private DirectPosition getPosition(MathTransform mMathTransform, double latitude, double longitude) {
        try {
            return mMathTransform.transform(new DirectPosition2D(longitude, latitude), null);
        } catch (MismatchedDimensionException | TransformException ex) {
            Exceptions.printStackTrace(ex);
        }

        return new DirectPosition2D();
    }

    private void init(String crsCode) throws FactoryException, NoninvertibleTransformException, TransformException {
        mTargetCrs = CRS.decode(crsCode);
        mMathTransform = CRS.findMathTransform(mSourceCrs, mTargetCrs, true);
        mInverseMathTransform = mMathTransform.inverse();

        //This is not a org.locationtech.jts.geom.Envelope
        var foreignEnvelope = CRS.getEnvelope(mTargetCrs);
        //So we have to create one
        var targetEnvelope = new org.locationtech.jts.geom.Envelope(
                foreignEnvelope.getLowerCorner().getCoordinate()[0],
                foreignEnvelope.getUpperCorner().getCoordinate()[0],
                foreignEnvelope.getLowerCorner().getCoordinate()[1],
                foreignEnvelope.getUpperCorner().getCoordinate()[1]
        );

        var sourceEnvelope = JTS.transform(targetEnvelope, mInverseMathTransform);

        mSourceBounds = new MBounds(
                sourceEnvelope.getMinX(),
                sourceEnvelope.getMinY(),
                sourceEnvelope.getMaxX(),
                sourceEnvelope.getMaxY()
        );

        mTargetBounds = new MBounds(
                targetEnvelope.getMinX(),
                targetEnvelope.getMinY(),
                targetEnvelope.getMaxX(),
                targetEnvelope.getMaxY()
        );
    }
}
