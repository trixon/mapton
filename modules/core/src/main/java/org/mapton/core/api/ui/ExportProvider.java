/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.core.api.ui;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ExportProvider {

    private final ResourceBundle mBundle = NbBundle.getBundle(ExportPanel.class);
    private Charset mCharset = StandardCharsets.UTF_8;
    private MCooTrans mCooTrans = MOptions.getInstance().getMapCooTrans();

    private final Object mLookupKey;
    private Label mPlaceHolderNode;
    private boolean mSupportsEncoding;
    private boolean mSupportsTransformation;

    public ExportProvider(Object lookupKey) {
        mLookupKey = lookupKey;
    }

    public abstract void export(ExportConfiguration exportConfiguration);

    public Charset getCharset() {
        return mCharset;
    }

    public MCooTrans getCooTrans() {
        return mCooTrans;
    }

    public abstract FileNameExtensionFilter getExtensionFilter();

    public Object getLookupKey() {
        return mLookupKey;
    }

    public abstract String getName();

    public Node getNode() {
        if (mPlaceHolderNode == null) {
            mPlaceHolderNode = new Label(mBundle.getString("noCustomConfig"));
            mPlaceHolderNode.setDisable(true);
        }

        return mPlaceHolderNode;
    }

    public boolean isSupportsEncoding() {
        return mSupportsEncoding;
    }

    public boolean isSupportsTransformation() {
        return mSupportsTransformation;
    }

    public void setCharset(Charset charset) {
        mCharset = charset;
    }

    public void setCooTrans(MCooTrans cooTrans) {
        mCooTrans = cooTrans;
    }

    public void setSupportsEncoding(boolean supportsEncoding) {
        mSupportsEncoding = supportsEncoding;
    }

    public void setSupportsTransformation(boolean supportsTransformation) {
        mSupportsTransformation = supportsTransformation;
    }

    @Override
    public String toString() {
        return getName();
    }
}
