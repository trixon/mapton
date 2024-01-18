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

import javafx.scene.Node;
import javafx.scene.control.Label;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 *
 * @author Patrik Karlström
 */
public abstract class ExportProvider {

    private final Object mLookupKey;
    private Label mPlaceHolderNode;
    private boolean mSupportsEncoding;
    private boolean mSupportsTransformation;

    public ExportProvider(Object lookupKey) {
        mLookupKey = lookupKey;
    }

    public abstract void export(ExportConfiguration exportConfiguration);

    public abstract FileNameExtensionFilter getExtensionFilter();

    public Object getLookupKey() {
        return mLookupKey;
    }

    public abstract String getName();

    public Node getNode() {
        if (mPlaceHolderNode == null) {
            mPlaceHolderNode = new Label();
        }
        return mPlaceHolderNode;
    }

    public boolean isSupportsEncoding() {
        return mSupportsEncoding;
    }

    public boolean isSupportsTransformation() {
        return mSupportsTransformation;
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
