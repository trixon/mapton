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

import javafx.geometry.Point2D;
import org.apache.commons.lang3.StringUtils;
import org.geotools.api.geometry.MismatchedDimensionException;
import org.geotools.api.geometry.Position;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.api.referencing.operation.MathTransform;
import org.geotools.api.referencing.operation.NoninvertibleTransformException;
import org.geotools.api.referencing.operation.TransformException;
import org.geotools.geometry.Position2D;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.locationtech.jts.geom.Geometry;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class GeoToolsCooTrans implements MCooTrans {

    private final String mCrsCode;
    private MathTransform mInverseMathTransform;
    private MathTransform mMathTransform;
    private MBounds mSourceBounds;
    private final CoordinateReferenceSystem mSourceCrs = DefaultGeographicCRS.WGS84;
    private MBounds mTargetBounds;
    private CoordinateReferenceSystem mTargetCrs;

    public GeoToolsCooTrans(String crsCode) {
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
        var position = getPosition(mMathTransform, latitude, longitude);

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

    public MathTransform getInverseMathTransform() {
        return mInverseMathTransform;
    }

    @Override
    public double getLatitude(double latitude, double longitude) {
        return getPosition(mMathTransform, latitude, longitude).getCoordinate()[0];
    }

    @Override
    public String getLatitudeString(double latitude, double longitude) {
        return "%.1f N".formatted(getLatitude(latitude, longitude));
    }

    @Override
    public double getLongitude(double latitude, double longitude) {
        return getPosition(mMathTransform, latitude, longitude).getCoordinate()[1];
    }

    @Override
    public String getLongitudeString(double latitude, double longitude) {
        return "%.1f E".formatted(getLongitude(latitude, longitude));
    }

    public MathTransform getMathTransform() {
        return mMathTransform;
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
            return "%s  %s".formatted(getLatitudeString(latitude, longitude), getLongitudeString(latitude, longitude));
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

    @Override
    public Geometry transform(Geometry geometry) throws MismatchedDimensionException, TransformException {
        return JTS.transform(geometry, mMathTransform);
    }

    private Position getPosition(MathTransform mathTransform, double latitude, double longitude) {
        try {
            return mathTransform.transform(new Position2D(longitude, latitude), null);
        } catch (MismatchedDimensionException | TransformException ex) {
            Exceptions.printStackTrace(ex);
        }

        return new Position2D();
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

        //TODO Swap x & Y or not? Fix this for all target crs
        mTargetBounds = new MBounds(
                targetEnvelope.getMinY(),
                targetEnvelope.getMinX(),
                targetEnvelope.getMaxY(),
                targetEnvelope.getMaxX()
        );
    }
}
