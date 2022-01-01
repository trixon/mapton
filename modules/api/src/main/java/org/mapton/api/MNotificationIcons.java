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
package org.mapton.api;

import javax.swing.ImageIcon;
import se.trixon.almond.util.icons.material.swing.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MNotificationIcons {

    public static ImageIcon getErrorIcon() {
        return MaterialIcon._Alert.ERROR.getImageIcon(getSize());
    }

    public static ImageIcon getInformationIcon() {
        return MaterialIcon._Action.INFO.getImageIcon(getSize());
    }

    public static ImageIcon getWarningIcon() {
        return MaterialIcon._Alert.WARNING.getImageIcon(getSize());
    }

    private static int getSize() {
        return SwingHelper.getUIScaled(36);
    }
}
