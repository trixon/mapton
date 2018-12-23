/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.wikipedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javax.swing.SwingUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MLatLon;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MContextMenuItem.class)
public class WikipediaContextExtras extends MContextMenuItem {

    private final WindowManager mWindowManager = WindowManager.getDefault();
    private WikipediaTopComponent mWikipediaTopComponent;

    public WikipediaContextExtras() {
        SwingUtilities.invokeLater(() -> {
            mWikipediaTopComponent = (WikipediaTopComponent) mWindowManager.findTopComponent("WikipediaTopComponent");
        });
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return (event) -> {
            String base = String.format(Locale.ENGLISH,
                    "https://%s.wikipedia.org/w/api.php?action=query",
                    Locale.getDefault().getLanguage()
            );

            String template = String.format(Locale.ENGLISH,
                    "&prop=coordinates"
                    + "|pageimages"
                    + "|pageterms"
                    + "&colimit=%d"
                    + "&piprop=thumbnail"
                    + "&pithumbsize=%d"
                    + "&pilimit=%d"
                    + "&wbptterms=description"
                    + "&generator=geosearch"
                    + "&ggscoord=%f|%f"
                    + "&ggsradius=%d"
                    + "&ggslimit=%d"
                    + "&format=json",
                    50,
                    144,
                    50,
                    getLatitude(),
                    getLongitude(),
                    10000,
                    50
            );

            try {
                URL url = new URL(base + StringUtils.replaceEach(URLEncoder.encode(template, "utf-8"),
                        new String[]{"%26", "%3D"},
                        new String[]{"&", "="}));

                System.out.println(url);

                new Thread(() -> {
                    String json;
                    try {
                        json = IOUtils.toString(url, "utf-8");
                        mWikipediaTopComponent.load(new MLatLon(getLatitude(), getLongitude()), json);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
    }

    @Override
    public String getName() {
        return "Wikipedia";
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

    @Override
    public String getUrl() {
        return String.format(Locale.ENGLISH, "geo:%.6f,%.6f;crs=wgs84",
                getLatitude(),
                getLongitude()
        );
    }

}
