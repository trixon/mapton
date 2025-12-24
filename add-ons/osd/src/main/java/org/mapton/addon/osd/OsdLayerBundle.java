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
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.scene.Node;
import javax.swing.Timer;
import org.apache.commons.lang3.StringUtils;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class OsdLayerBundle extends LayerBundle {

    private final ScreenRelativeAnnotation mAnnotation = new ScreenRelativeAnnotation("", .5, 0.0);
    private final AnnotationAttributes mAttributes = new AnnotationAttributes();
    private DateTimeFormatter mDateTimeFormatter = DateTimeFormatter.ofPattern(ModuleOptions.DEFAULT_PATTERN);
    private final AnnotationLayer mLayer = new AnnotationLayer();
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private OsdOptionsView mOptionsView;
    private String mPatternError = null;
    private Timer mTimer;

    public OsdLayerBundle() {
        init();
        initAttributes();
        initListeners();
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = new OsdOptionsView();
        }

        return mOptionsView.getBorderPane();
    }

    @Override
    public void populate() throws Exception {
        getLayers().addAll(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        setName("OSD");
        mLayer.setName("OSD");
        setCategoryAddOns(mLayer);
        setParentLayer(mLayer);

        mLayer.setEnabled(true);
        mLayer.setPickEnabled(false);
        mAttributes.setSize(SwingHelper.getUIScaledDim(3000, 0));
        mAttributes.setAdjustWidthToText(AVKey.SIZE_FIT_TEXT);

        mTimer = new Timer(0, actionListener -> {
            var text = mPatternError == null ? LocalDateTime.now().format(mDateTimeFormatter) : mPatternError;
            mAnnotation.setText(text);
            repaint();
        });

        mTimer.setDelay(250);
        mLayer.addAnnotation(mAnnotation);

        if (mLayer.isEnabled()) {
            mTimer.start();
        }
    }

    private void initAttributes() {
        var fontSize = mOptions.getFontSize();
        var fontSizeHalf = (int) (fontSize * .5);
        mAttributes.setOpacity(mOptions.getOpacity());
        mAttributes.setCornerRadius(fontSizeHalf);
        mAttributes.setInsets(SwingHelper.getUIScaledInsets(0, fontSizeHalf, 0, fontSizeHalf));
        mAttributes.setFont(Font.decode("Monospaced-BOLD-%d".formatted(fontSize)));
        mAttributes.setBackgroundColor(mOptions.getBackgroundColor());
        mAttributes.setTextColor(mOptions.getFontColor());
        mAttributes.setBorderWidth(mOptions.getBorderSize());
        mAttributes.setBorderColor(mOptions.getBorderColor());

        mAnnotation.getAttributes().setDefaults(mAttributes);

        try {
            mPatternError = null;
            var pattern = StringUtils.isBlank(mOptions.getPattern()) ? ModuleOptions.DEFAULT_PATTERN : mOptions.getPattern();
            mDateTimeFormatter = DateTimeFormatter.ofPattern(pattern);
        } catch (Exception e) {
            mPatternError = e.getMessage();
        }
    }

    private void initListeners() {
        mLayer.addPropertyChangeListener("Enabled", pce -> {
            if (mLayer.isEnabled()) {
                mTimer.start();
            } else {
                mTimer.stop();
            }
        });

        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            initAttributes();
        });
    }

}
