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
package se.trixon.mapton.core.api;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlström
 */
public abstract class KmlCreator {

    protected static final Logger LOGGER = Logger.getLogger(KmlCreator.class.getName());
    protected final Document mDocument;
    protected final Kml mKml = new Kml();
    protected Folder mRootFolder;
    protected final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public KmlCreator() {
        mDocument = mKml.createAndSetDocument().withOpen(true);
    }

    public String save(File f) throws IOException {
        return save(f, false, false);
    }

    public String save(File f, boolean cleanNS2, boolean cleanSpace) throws IOException {
        StringWriter stringWriter = new StringWriter();
        mKml.marshal(stringWriter);
        String kmlString = stringWriter.toString();

        if (cleanNS2) {
            kmlString = cleanNS2(kmlString);
        }

        if (cleanSpace) {
            kmlString = cleanSpace(kmlString);
        }

        FileUtils.writeStringToFile(f, kmlString, "utf-8");

        return kmlString;
    }

    public void setVisible(Feature feature, boolean visible) {
        feature.setVisibility(visible);
        if (feature instanceof Folder) {
            ((Folder) feature).getFeature().forEach((f) -> {
                setVisible(f, visible);
            });
        }
    }

    protected String getSafeXmlString(String s) {
        if (StringUtils.containsAny(s, '<', '>', '&')) {
            s = new StringBuilder("<![CDATA[").append(s).append("]]>").toString();
        }

        return s;
    }

    private String cleanNS2(String kmlString) {
        kmlString = StringUtils.replace(kmlString, "xmlns:ns2=", "xmlns=");
        kmlString = StringUtils.replace(kmlString, "<ns2:", "<");
        kmlString = StringUtils.replace(kmlString, "</ns2:", "</");

        return kmlString;
    }

    private String cleanSpace(String kmlString) {
        kmlString = StringUtils.replace(kmlString, "        ", "\t");
        kmlString = StringUtils.replace(kmlString, "    ", "\t");

        return kmlString;
    }

}
