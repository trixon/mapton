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
package org.mapton.api;

import java.io.IOException;
import java.net.URI;
import javax.imageio.ImageIO;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MBackgroundImage {

    private Object mImageSource;
    private double mOpacity = 1.0;

    public MBackgroundImage() {
    }

    public MBackgroundImage(Object imageSource) {
        mImageSource = imageSource;
        mOpacity = 1.0;
    }

    public MBackgroundImage(Object imageSource, double opacity) {
        mImageSource = imageSource;
        mOpacity = opacity;
    }

    public Object getImageSource() {
        if (mImageSource instanceof String s && StringUtils.contains((String) mImageSource, "//")) {
            try {
                return ImageIO.read(URI.create(s).toURL());
            } catch (IOException ex) {
                return "";
            }
        } else {
            return mImageSource;
        }
    }

    public double getOpacity() {
        return mOpacity;
    }

    public void setImageSource(Object imageSource) {
        mImageSource = imageSource;
    }

    public void setOpacity(double opacity) {
        mOpacity = opacity;
    }
}
