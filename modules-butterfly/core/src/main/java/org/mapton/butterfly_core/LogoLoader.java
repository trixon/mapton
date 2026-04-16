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
package org.mapton.butterfly_core;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.Timer;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.openide.util.Utilities;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LogoLoader {

    private static final int SWAP_DELAY = 2 * 60 * 1000;
    private static final int SWAP_INITIAL_DELAY = 10 * 1000;
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();
    private final URL mDefaultUrl = getClass().getResource("scior-logo.png");
    private Timer mTimer;
    private boolean mUsingExtraLogo = true;

    public LogoLoader() {
        loadLogo(mDefaultUrl);
    }

    public void load() {
        if (mTimer != null) {
            mTimer.stop();
            mUsingExtraLogo = true;
        }

        var extraLogo = mButterflyManager.getFile("logo.png");
        var extraLogoNoSwap = mButterflyManager.getFile("logo_NoSwap.png");
        var hasLogo = extraLogo != null && extraLogo.isFile();
        var hasLogoNoSwap = extraLogoNoSwap != null && extraLogoNoSwap.isFile();

        if (hasLogoNoSwap) {
            SwingHelper.runLaterDelayed(SWAP_INITIAL_DELAY, () -> loadLogo(fileToUrl(extraLogoNoSwap)));
        } else if (hasLogo) {
            mTimer = new Timer(SWAP_INITIAL_DELAY, actionEvent -> {
                loadLogo(mUsingExtraLogo ? fileToUrl(extraLogo) : mDefaultUrl);
                mUsingExtraLogo = !mUsingExtraLogo;
            });

            mTimer.setRepeats(true);
            mTimer.setInitialDelay(SWAP_INITIAL_DELAY);
            mTimer.setDelay(SWAP_DELAY);
            mTimer.start();
        }
    }

    private URL fileToUrl(File file) {
        try {
            return Utilities.toURI(file).toURL();
        } catch (MalformedURLException ex) {
            return null;
        }
    }

    private void loadLogo(URL url) {
        Mapton.getGlobalState().put(MKey.MAP_LOGO_URL, url);
    }
}
