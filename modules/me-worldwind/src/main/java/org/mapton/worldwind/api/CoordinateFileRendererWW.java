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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javafx.collections.ListChangeListener;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCoordinateFile;
import org.mapton.api.MCoordinateFileManager;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MKey;
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
    protected final MCoordinateFileManager mCoordinateFileManager = MCoordinateFileManager.getInstance();
    protected Layer mParentLayer;
    private final HashMap<MCoordinateFile, Layer> mCoordinateFileToLayer = new HashMap<>();
    private final DigestUtils mDigestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);
    private LayerBundle mLayerBundle;
    private Set<String> mSupportedFileOpeners = new HashSet<>();

    public CoordinateFileRendererWW() {
        initListeners();
    }

    public void addLayer(MCoordinateFile coordinateFile, Layer layer) {
        getLayerBundle().getLayers().add(layer);
        getLayerBundle().addAllChildLayers(layer);
        mCoordinateFileToLayer.put(coordinateFile, layer);

        messageStop(coordinateFile);
    }

    //    public String getDigest() {
//        try {
//            return mDigestUtils.digestAsHex(mCoordinateFile.getFile());
//        } catch (IOException ex) {
//            return "-";
//        }
//    }
    public LayerBundle getLayerBundle() {
        return mLayerBundle;
    }

    public Set<String> getSupportedFileOpeners() {
        return mSupportedFileOpeners;
    }

    public abstract void init(LayerBundle layerBundle);

    public void removeLayer(MCoordinateFile coordinateFile) {
        var layer = mCoordinateFileToLayer.remove(coordinateFile);
        if (layer != null) {
            getLayerBundle().getLayers().remove(layer);
        }
    }

    public void setLayerBundle(LayerBundle layerBundle) {
        mLayerBundle = layerBundle;
        mParentLayer = mLayerBundle.getParentLayer();
        mParentLayer.addPropertyChangeListener(pce -> {
            if (pce.getPropertyName().equals("Enabled")) {
                if ((Boolean) pce.getNewValue()) {
                    render();
                }
            }
        });
    }

    protected void addSupportedFileOpeners(Class<? extends MCoordinateFileOpener>... coordinateFileOpeners) {
        for (var coordinateFileOpener : coordinateFileOpeners) {
            mSupportedFileOpeners.add(coordinateFileOpener.getName());
        }
    }

    protected abstract void load(MCoordinateFile coordinateFile);

    protected abstract void render();

    protected synchronized void render(MCoordinateFile coordinateFile) {
        boolean visible = coordinateFile.isVisible();

        if (mCoordinateFileToLayer.containsKey(coordinateFile)) {
            var layer = mCoordinateFileToLayer.get(coordinateFile);
            layer.setEnabled(visible);
            layer.setValue(MKey.LAYER_SUB_VISIBILITY, visible);
        } else {
            if (!visible) {
                return;
            }

            messageStart(coordinateFile);
            load(coordinateFile);
        }
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

    private void messageStart(MCoordinateFile coordinateFile) {
        MaptonNb.progressStart(String.format(Dict.OPENING_S.toString(), coordinateFile.getFile().getName()));
    }

    private void messageStop(MCoordinateFile coordinateFile) {
        MaptonNb.progressStop(String.format(Dict.OPENING_S.toString(), coordinateFile.getFile().getName()));
    }

}
