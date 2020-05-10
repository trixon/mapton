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
import static org.mapton.addon.files_nb.renderers.Renderer.DIGEST_RENDERABLE_MAP;
import org.openide.util.Exceptions;
import se.trixon.almond.util.io.Geo;

/**
 *
 * @author Patrik Karlström
 */
public class GeoRenderer extends Renderer {

    public static void render(File file, RenderableLayer layer) {
        GeoRenderer renderer = new GeoRenderer(file, layer);
        renderer.render(layer);
    }

    public GeoRenderer(File file, RenderableLayer layer) {
        mFile = file;
        mLayer = layer;
    }

    @Override
    protected void render(RenderableLayer layer) {
        render(() -> {
            String digest = getDigest();
            ArrayList<Renderable> renderables = DIGEST_RENDERABLE_MAP.computeIfAbsent(digest, k -> {
                ArrayList<Renderable> newRenderables = new ArrayList<>();
                Geo geo = new Geo();
                try {
                    geo.read(mFile);
                    //Dynamic transform
                    //Create Points
                    //Create Lines
                    System.out.println(geo.toString());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
                return newRenderables;
            });

            for (Renderable renderable : renderables) {
                layer.addRenderable(renderable);
            }
        });
    }

}
