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
package org.mapton.addon.photos;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.GeoLocation;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.exif.GpsDirectory;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.TimeZone;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.mapton.addon.photos.api.MapoSource;
import se.trixon.almond.util.GraphicsHelper;
import se.trixon.almond.util.ImageScaler;
import se.trixon.almond.util.Scaler;

/**
 *
 * @author Patrik Karlström
 */
public class PhotoInfo {

    private String mChecksum;
    private ExifSubIFDDirectory mExifDirectory;
    private final File mFile;
    private final double mFormat = 1000000;
    private GeoLocation mGeoLocation;
    private GpsDirectory mGpsDirectory;
    private int mHeight;
    private final ImageScaler mImageScaler = ImageScaler.getInstance();
    private Metadata mMetadata;
    private int mOrientation;
    private Dimension mOriginalDimension = null;
    private int mWidth;

    public PhotoInfo(File file) throws ImageProcessingException, IOException {
        mFile = file;
        init();
    }

    public void createThumbnail(MapoSource source, File file) throws IOException {
        int thumbnailSize = source.getThumbnailSize();

        if (source.isThumbnailForceCreation() || !file.exists()) {
            var scaledImage = mImageScaler.getScaledImage(mFile, new Dimension(thumbnailSize, thumbnailSize));
            scaledImage = GraphicsHelper.rotate(scaledImage, mOrientation);

            mHeight = scaledImage.getHeight();
            mWidth = scaledImage.getWidth();

            int borderSize = source.getThumbnailBorderSize();
            var borderedImage = scaledImage;

            if (borderSize > 0) {
                int width = scaledImage.getWidth();
                int height = scaledImage.getHeight();

                int borderedImageWidth = width + borderSize * 2;
                int borderedImageHeight = height + borderSize * 2;

                borderedImage = new BufferedImage(borderedImageWidth, borderedImageHeight, BufferedImage.TYPE_3BYTE_BGR);

                var g2 = borderedImage.createGraphics();
                g2.setColor(Color.decode("#" + source.getThumbnailBorderColor()));
                g2.fillRect(0, 0, borderedImageWidth, borderedImageHeight);
                g2.drawImage(scaledImage, borderSize, borderSize, width + borderSize, height + borderSize, 0, 0, width, height, Color.YELLOW, null);
            }

            try {
                ImageIO.write(borderedImage, "jpg", file);
            } catch (IOException ex) {
                throw new IOException("E000 %s".formatted(file.getAbsolutePath()));
            }
        } else {
            var scaler = new Scaler(getOriginalDimension());
            scaler.setHeight(thumbnailSize);
            scaler.setWidth(thumbnailSize);

            mHeight = scaler.getDimension().height;
            mWidth = scaler.getDimension().width;
        }
    }

    public Double getAltitude() {
        if (mGpsDirectory != null) {
            return mGpsDirectory.getDoubleObject(GpsDirectory.TAG_ALTITUDE);
        } else {
            return null;
        }
    }

    public Double getBearing() {
        if (mGpsDirectory != null) {
            return mGpsDirectory.getDoubleObject(GpsDirectory.TAG_ORIENTATION);
        } else {
            return null;
        }
    }

    public String getChecksum() {
        return mChecksum;
    }

    public Date getDate() {
        Date date;

        if (mExifDirectory.containsTag(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)) {
            date = mExifDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL, TimeZone.getDefault());
        } else {
            long millis = 0;
            try {
                var basicFileAttributes = Files.readAttributes(mFile.toPath(), BasicFileAttributes.class);
                millis = basicFileAttributes.lastModifiedTime().toMillis();
            } catch (IOException ex) {
                millis = mFile.lastModified();
            } finally {
                date = new Date(millis);
            }
        }

        return date;
    }

    public ExifSubIFDDirectory getExifDirectory() {
        return mExifDirectory;
    }

    public GpsDirectory getGpsDirectory() {
        return mGpsDirectory;
    }

    public int getHeight() {
        return mHeight;
    }

    public double getLat() throws NullPointerException {
        int latInt = (int) (mGeoLocation.getLatitude() * mFormat);

        return latInt / mFormat;
    }

    public double getLon() {
        int lonInt = (int) (mGeoLocation.getLongitude() * mFormat);

        return lonInt / mFormat;
    }

    public Metadata getMetadata() {
        return mMetadata;
    }

    public int getOrientation() {
        return mOrientation;
    }

    public int getWidth() {
        return mWidth;
    }

    public boolean isZeroCoordinate() {
        return mGpsDirectory == null || mGpsDirectory.getGeoLocation() == null || mGpsDirectory.getGeoLocation().isZero();
    }

    private GeoLocation getGeoLocation() throws ImageProcessingException {
        return mGpsDirectory.getGeoLocation();
    }

    private Dimension getOriginalDimension() throws IOException {
        if (mOriginalDimension == null) {
            try {
                mOriginalDimension = GraphicsHelper.getImgageDimension(mFile);
                if (getOrientation() == 6 || getOrientation() == 8) {
                    int storedHeight = mOriginalDimension.height;
                    mOriginalDimension.height = mOriginalDimension.width;
                    mOriginalDimension.width = storedHeight;
                }

            } catch (IOException ex) {
                throw new IOException("E000 %s".formatted(mFile.getAbsolutePath()));
            }

            if (mOriginalDimension == null) {
                mOriginalDimension = new Dimension(200, 200);
            }
        }

        return mOriginalDimension;
    }

    private void init() throws ImageProcessingException, IOException, NullPointerException {
        mChecksum = "%08x".formatted(FileUtils.checksumCRC32(mFile));
        mMetadata = ImageMetadataReader.readMetadata(mFile);
        mExifDirectory = mMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
        mGpsDirectory = mMetadata.getFirstDirectoryOfType(GpsDirectory.class);

        if (mGpsDirectory != null) {
            mGeoLocation = getGeoLocation();

            try {
                var rotationDirectory = mMetadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
                mOrientation = rotationDirectory.getInt(ExifSubIFDDirectory.TAG_ORIENTATION);
            } catch (MetadataException | NullPointerException ex) {
                mOrientation = 1;
            }
        }
    }
}
