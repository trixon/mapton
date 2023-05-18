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
package org.mapton.worldwind.custom_image;

import gov.nasa.worldwind.layers.SurfaceImageLayer;
import java.io.File;
import java.io.IOException;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.Exceptions;
import se.trixon.almond.util.swing.FileHelper;

/**
 *
 * @author Patrik Karlström
 */
//@ServiceProvider(service = LayerBundle.class)
public class ImageLayerBundle extends LayerBundle {

    private SurfaceImageLayer mLayer;

    public ImageLayerBundle() {
        mLayer = new SurfaceImageLayer();
        mLayer.setOpacity(1);
        mLayer.setPickEnabled(false);
        mLayer.setName("Custom Images");
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        setPopulated(true);
//        refresh();
    }

    private void loadImages(File dir) {
        for (File file : dir.listFiles()) {
            try {
                mLayer.addImage(file.getAbsolutePath());
                System.out.println("loading " + file.getAbsolutePath());
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
    }

    private void refresh() {
        Thread thread = new Thread(() -> {
            File dir = new File(FileHelper.getDefaultDirectory(), "test/img");
            loadImages(dir);

            LayerBundleManager.getInstance().redraw();
        }, getClass().getCanonicalName());

        thread.setPriority(Thread.MIN_PRIORITY);
        thread.start();
    }
}
