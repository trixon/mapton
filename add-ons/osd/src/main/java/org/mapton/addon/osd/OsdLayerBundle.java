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
package org.mapton.addon.osd;

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.layers.AnnotationLayer;
import gov.nasa.worldwind.render.AnnotationAttributes;
import gov.nasa.worldwind.render.ScreenRelativeAnnotation;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class OsdLayerBundle extends LayerBundle {

    private final String mPattern = "yyyy-MM-dd HH.mm.ss";
    public DateTimeFormatter mDateTimeFormatter = DateTimeFormatter.ofPattern(mPattern);
    private final ScreenRelativeAnnotation mAnnotation = new ScreenRelativeAnnotation(mPattern, .5, 0.0);

    private final AnnotationLayer mLayer = new AnnotationLayer();
    private Timer mTimer;

    public OsdLayerBundle() {
        init();
        initAttributes();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        mLayer.setName("OSD");
        setCategoryAddOns(mLayer);
        setParentLayer(mLayer);

        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
        mTimer = new Timer(0, l -> {
            mAnnotation.setText(LocalDateTime.now().format(mDateTimeFormatter));
            repaint();
            //initAttributes();
        });

        mTimer.setDelay(250);
        mLayer.addAnnotation(mAnnotation);

        if (mLayer.isEnabled()) {
            mTimer.start();
        }
    }

    private void initAttributes() {
        var attrs = new AnnotationAttributes();
        attrs.setBackgroundColor(new Color(0f, 0f, 0f, 0.5f));
        attrs.setTextColor(Color.LIGHT_GRAY);
        attrs.setLeaderGapWidth(14);
        attrs.setCornerRadius(10);
        attrs.setSize(new Dimension(300, 0));
        attrs.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);
        attrs.setFont(Font.decode("Monospaced-BOLD-20"));
        attrs.setBorderWidth(0);
        attrs.setHighlightScale(1);
        attrs.setOpacity(0.5);
        attrs.setInsets(new Insets(0, 12, 0, 12));

        mAnnotation.getAttributes().setDefaults(attrs);
    }

    private void initListeners() {
        mLayer.addPropertyChangeListener("Enabled", pce -> {
            if (mLayer.isEnabled()) {
                mTimer.start();
            } else {
                mTimer.stop();
            }
        });
    }

    private void initRepaint() {
        setPainter(() -> {
        });
    }

}
