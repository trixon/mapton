/* 
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.core.ui.context.open;

import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.File;
import java.io.IOException;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import org.mapton.api.MContextMenuItem;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MContextMenuItem.class)
public class GoogleEarth extends MContextMenuItem {

    @Override
    public String getName() {
        return "Google Earth";
    }

    @Override
    public ContextType getType() {
        return ContextType.OPEN;
    }

    @Override
    public String getUrl() {
        try {
            File file = File.createTempFile("mapton", ".kml");
            file.deleteOnExit();

            Kml kml = new Kml();
            kml.createAndSetPlacemark().withName("Mapton").createAndSetPoint().addToCoordinates(getLongitude(), getLatitude());
            kml.marshal(file);

            SystemHelper.desktopOpen(file);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return null;
    }
}
