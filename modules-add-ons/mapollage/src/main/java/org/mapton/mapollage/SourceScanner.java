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
package org.mapton.mapollage;

import java.io.IOException;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.mapollage.api.MapoCollection;
import org.mapton.mapollage.api.MapoPhoto;
import org.mapton.mapollage.api.MapoSource;
import org.mapton.mapollage.api.MapoSourceManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class SourceScanner {

    private final MapoSourceManager mManager = MapoSourceManager.getInstance();
//    private ArrayList<MapoSource> mDisabledSources = new ArrayList<>();
//    private ArrayList<MapoSource> mScannedSources = new ArrayList<>();

    public SourceScanner() {
        System.out.println("new SourceScanner");

        new Thread(() -> {
            for (MapoSource source : mManager.getItems()) {
                System.out.println(ToStringBuilder.reflectionToString(source, ToStringStyle.JSON_STYLE));
                if (source.isVisible()) {
                    try {
                        scan(source);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
//                } else {
//                    mDisabledSources.add(source);
                }
            }

            try {
                mManager.loadCollections();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }

    private void scan(MapoSource source) throws IOException {
        MapoCollection collection = new MapoCollection();
        collection.setId(source.getId());
        collection.setName(source.getName());

        collection.getPhotos().add(new MapoPhoto());//test
        source.save(collection);
    }
}
