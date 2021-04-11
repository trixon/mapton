/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.ListChangeListener;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MCoordinateFileManager;
import org.mapton.core.api.MaptonNb;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public abstract class CoordinateFileRendererWW {

    public static final ConcurrentHashMap<String, ArrayList<Renderable>> DIGEST_RENDERABLE_MAP = new ConcurrentHashMap<>();
    protected MCooTrans mCooTrans;
    @Deprecated
    protected MCoordinateFile mCoordinateFile;
    protected final MCoordinateFileManager mCoordinateFileManager = MCoordinateFileManager.getInstance();
    protected final HashMap<MCoordinateFile, Layer> mCoordinateFileToLayer = new HashMap<>();
    protected Layer mParentLayer;
    private final DigestUtils mDigestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);
    private LayerBundle mLayerBundle;

    public CoordinateFileRendererWW() {
        initListeners();
    }

    public String getDigest() {
        try {
            return mDigestUtils.digestAsHex(mCoordinateFile.getFile());
        } catch (IOException ex) {
            return "-";
        }
    }

    public LayerBundle getLayerBundle() {
        return mLayerBundle;
    }

    public abstract void init(LayerBundle layerBundle);

    public abstract void render();

    public void setLayerBundle(LayerBundle layerBundle) {
        mLayerBundle = layerBundle;
        mParentLayer = mLayerBundle.getParentLayer();
    }

    protected void messageStart(MCoordinateFile coordinateFile) {
        MaptonNb.progressStart(String.format(Dict.OPENING_S.toString(), coordinateFile.getFile().getName()));
    }

    protected void messageStop(MCoordinateFile coordinateFile) {
        MaptonNb.progressStop(String.format(Dict.OPENING_S.toString(), coordinateFile.getFile().getName()));
    }

    @Deprecated
    protected void render(RenderableLayer layer) {
    }

    @Deprecated
    protected void render(Runnable r) {
        new Thread(() -> {
            r.run();
            LayerBundleManager.getInstance().redraw();
        }).start();
    }

    private void initListeners() {
        var delayedResetRunner = new DelayedResetRunner(100, () -> {
            if (mParentLayer != null && mParentLayer.isEnabled()) {
                render();
            }
        });

        mCoordinateFileManager.getItems().addListener((ListChangeListener.Change<? extends MCoordinateFile> c) -> {
            delayedResetRunner.reset();
        });

        mCoordinateFileManager.updatedProperty().addListener((observable, oldValue, newValue) -> {
            delayedResetRunner.reset();
        });
    }
}
