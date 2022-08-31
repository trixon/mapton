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
package org.mapton.addon.photos;

import com.drew.imaging.ImageProcessingException;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.lang3.StringUtils;
import org.mapton.addon.photos.api.MapoCollection;
import org.mapton.addon.photos.api.MapoPhoto;
import org.mapton.addon.photos.api.MapoSource;
import org.mapton.addon.photos.api.MapoSourceManager;
import org.mapton.api.MNotificationIcons;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Exceptions;
import se.trixon.almond.nbp.NbPrint;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class SourceScanner {

    private MapoCollection mCurrentCollection;
    private MapoSource mCurrentSource;
    private final ArrayList<File> mFiles = new ArrayList<>();
    private boolean mInterrupted = false;
    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
    private final NbPrint mPrint = new NbPrint(Dict.PHOTOS.toString());

    public SourceScanner() {
        mPrint.select();
        mPrint.out("BEGIN SCAN COLLECTION");

        for (MapoSource source : mManager.getItems()) {
            if (source.isVisible()) {
                try {
                    process(source);
                    if (mInterrupted) {
                        break;
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

        if (mInterrupted) {
            mPrint.out("INTERRUPTED SCAN COLLECTION");
            NotificationDisplayer.getDefault().notify(
                    Dict.OPERATION_INTERRUPTED.toString(),
                    MNotificationIcons.getWarningIcon(),
                    Dict.PHOTOS.toString(),
                    null,
                    NotificationDisplayer.Priority.HIGH
            );
        } else {
            mManager.load();
            mPrint.out("END SCAN COLLECTION");
            NotificationDisplayer.getDefault().notify(
                    Dict.OPERATION_COMPLETED.toString(),
                    MNotificationIcons.getInformationIcon(),
                    Dict.PHOTOS.toString(),
                    null,
                    NotificationDisplayer.Priority.LOW
            );
        }
    }

    private void generateFileList(MapoSource source) throws IOException {
        mPrint.out(Dict.GENERATING_FILELIST.toString());
        PathMatcher pathMatcher = source.getPathMatcher();

        EnumSet<FileVisitOption> fileVisitOptions;
        if (source.isFollowLinks()) {
            fileVisitOptions = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
        } else {
            fileVisitOptions = EnumSet.noneOf(FileVisitOption.class);
        }

        File file = source.getDir();
        if (file.isDirectory()) {
            FileVisitor fileVisitor = new FileVisitor(pathMatcher);
            try {
                if (source.isRecursive()) {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, Integer.MAX_VALUE, fileVisitor);
                } else {
                    Files.walkFileTree(file.toPath(), fileVisitOptions, 1, fileVisitor);
                }

                if (mInterrupted) {
                    return;
                }
            } catch (IOException ex) {
                mPrint.err(ex.toString());
            }
        } else if (file.isFile() && pathMatcher.matches(file.toPath().getFileName())) {
            mFiles.add(file);
        }

        if (mFiles.isEmpty()) {
            mPrint.out("EMPTY FILE LIST");
        } else {
            Collections.sort(mFiles);
        }
    }

    private void process(File file) {
        mPrint.out(file);
        try {
            PhotoInfo photoInfo = new PhotoInfo(file);

            if (!photoInfo.isZeroCoordinate()) {
                MapoPhoto mapoPhoto = new MapoPhoto();
                mapoPhoto.setPath(file.getAbsolutePath());
                mapoPhoto.setLat(photoInfo.getLat());
                mapoPhoto.setLon(photoInfo.getLon());
                mapoPhoto.setDate(photoInfo.getDate());
                mapoPhoto.setAltitude(photoInfo.getAltitude());
                mapoPhoto.setBearing(photoInfo.getBearing());
                mapoPhoto.setChecksum(photoInfo.getChecksum());
                mapoPhoto.setOrientation(photoInfo.getOrientation());

                photoInfo.createThumbnail(mCurrentSource, new File(mCurrentSource.getThumbnailDir(), "%s.jpg".formatted(photoInfo.getChecksum())));
                mapoPhoto.setHeight(photoInfo.getHeight());
                mapoPhoto.setWidth(photoInfo.getWidth());

                mCurrentCollection.getPhotos().add(mapoPhoto);
            }
        } catch (ImageProcessingException | IOException ex) {
            mPrint.err(ex);
        }
    }

    private void process(MapoSource source) throws IOException {
        mPrint.out("%s: %s".formatted("BEGIN SCAN", source));

        mFiles.clear();
        mCurrentSource = source;
        mCurrentCollection = new MapoCollection();
        mCurrentCollection.setId(source.getId());
        mCurrentCollection.setName(source.getName());
        source.isValid();

        generateFileList(source);

        if (!mInterrupted && !mFiles.isEmpty()) {
            mPrint.out("BEGIN PROCESSING PHOTOS");
            FileUtils.forceMkdir(source.getThumbnailDir());

            for (File f : mFiles) {
                process(f);
                try {
                    TimeUnit.NANOSECONDS.sleep(1);
                } catch (InterruptedException ex) {
                    mInterrupted = true;
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            ArrayList<MapoPhoto> photos = mCurrentCollection.getPhotos();
            Collections.sort(photos, (MapoPhoto o1, MapoPhoto o2) -> o1.getDate().compareTo(o2.getDate()));

            try {
                mCurrentCollection.setDateMin(photos.get(0).getDate());
                mCurrentCollection.setDateMax(photos.get(photos.size() - 1).getDate());
            } catch (Exception e) {
                Date d = new Date();
                mCurrentCollection.setDateMin(d);
                mCurrentCollection.setDateMax(d);
            }

            mPrint.out("END PROCESSING PHOTOS");
        }

        source.save(mCurrentCollection);

        mPrint.out("%s: %s".formatted("SAVED", source.getCollectionFile().getAbsoluteFile()));
        mPrint.out("%s: %s".formatted("END SCAN", source));
    }

    public class FileVisitor extends SimpleFileVisitor<Path> {

        private final String[] mExcludePatterns;
        private final PathMatcher mPathMatcher;

        public FileVisitor(PathMatcher pathMatcher) {
            mPathMatcher = pathMatcher;
            mExcludePatterns = StringUtils.split(mCurrentSource.getExcludePattern(), "::");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            mPrint.out(dir);
            if (mExcludePatterns != null) {
                for (String excludePattern : mExcludePatterns) {
                    if (IOCase.SYSTEM.isCaseSensitive()) {
                        if (StringUtils.contains(dir.toString(), excludePattern)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    } else {
                        if (StringUtils.containsIgnoreCase(dir.toString(), excludePattern)) {
                            return FileVisitResult.SKIP_SUBTREE;
                        }
                    }
                }
            }

            String[] filePaths = dir.toFile().list();

            if (filePaths != null && filePaths.length > 0) {
                for (String fileName : filePaths) {
                    try {
                        TimeUnit.NANOSECONDS.sleep(1);
                    } catch (InterruptedException ex) {
                        mInterrupted = true;
                        Thread.currentThread().interrupt();
                        return FileVisitResult.TERMINATE;
                    }

                    File file = new File(dir.toFile(), fileName);
                    if (file.isFile() && mPathMatcher.matches(file.toPath().getFileName())) {
                        boolean exclude = false;
                        if (mExcludePatterns != null) {
                            for (String excludePattern : mExcludePatterns) {
                                if (StringUtils.contains(file.getAbsolutePath(), excludePattern)) {
                                    exclude = true;
                                    break;
                                }
                            }
                        }

                        if (!exclude) {
                            mFiles.add(file);
                        }
                    }
                }
            }

            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exception) {
            mPrint.err(exception.toString());

            return FileVisitResult.CONTINUE;
        }
    }
}
