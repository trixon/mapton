/*
 * Copyright 2026 Patrik Karlström.
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

import com.jogamp.opengl.GL;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.DrawContext;
import gov.nasa.worldwind.render.FrameFactory;
import gov.nasa.worldwind.render.GlobeAnnotation;
import java.nio.DoubleBuffer;

/**
 *
 * @author Patrik Karlström
 */
public class RoundAnnotation extends GlobeAnnotation {

    private final int mSize;
    private DoubleBuffer mShapeBuffer;

    public RoundAnnotation(Position position, int size, AnnotationAttributes defaults) {
        super("", position, defaults);
        mSize = size;
    }

    @Override
    protected void applyScreenTransform(DrawContext dc, int x, int y, int width, int height, double scale) {
        double finalScale = scale * this.computeScale(dc);

        var gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glTranslated(x, y, 0);
        gl.glScaled(finalScale, finalScale, 1);
    }

    @Override
    protected void doDraw(DrawContext dc, int width, int height, double opacity, Position pickPosition) {
        // Draw colored circle around screen point - use annotation's text color
        if (dc.isPickingMode()) {
            this.bindPickableObject(dc, pickPosition);
        }

        this.applyColor(dc, this.getAttributes().getTextColor(), 0.6 * opacity, true);

        if (mShapeBuffer == null) {
            mShapeBuffer = FrameFactory.createShapeBuffer(AVKey.SHAPE_ELLIPSE, mSize, mSize, 0, null);
        }
        var gl = dc.getGL().getGL2(); // GL initialization checks for GL2 compatibility.
        gl.glTranslated(-mSize / 2, -mSize / 2, 0);
        FrameFactory.drawBuffer(dc, GL.GL_TRIANGLE_FAN, this.mShapeBuffer);
    }

}
