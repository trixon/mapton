/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.files_nb.renderers;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.mapton.worldwind.api.LayerBundleManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class Renderer {

    protected static final ConcurrentHashMap<String, ArrayList<Renderable>> DIGEST_RENDERABLE_MAP = new ConcurrentHashMap<>();
    protected File mFile;
    protected RenderableLayer mLayer;
    private final DigestUtils mDigestUtils = new DigestUtils(MessageDigestAlgorithms.SHA_256);

    public String getDigest() {
        try {
            return mDigestUtils.digestAsHex(mFile);
        } catch (IOException ex) {
            return "-";
        }
    }

    protected abstract void render(RenderableLayer layer);

    protected void render(Runnable r) {
        new Thread(() -> {
            r.run();
            LayerBundleManager.getInstance().redraw();
        }).start();
    }
}
