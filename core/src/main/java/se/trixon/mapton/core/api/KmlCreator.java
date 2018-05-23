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
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;
import javafx.geometry.Point2D;
import org.apache.commons.io.FileUtils;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public abstract class KmlCreator {

    protected static final Logger LOGGER = Logger.getLogger(KmlCreator.class.getName());
    protected final Document mDocument;
    protected final Kml mKml = new Kml();
    protected final Folder mRootFolder;
    protected final SimpleDateFormat mTimeStampDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");

    public KmlCreator(String name) {
        mDocument = mKml.createAndSetDocument().withOpen(true);
        mRootFolder = mDocument.createAndAddFolder().withName(name).withOpen(true);
    }

    public String save(File f) {
        StringWriter stringWriter = new StringWriter();
        mKml.marshal(stringWriter);
        String kmlString = stringWriter.toString();

        try {
            FileUtils.writeStringToFile(f, kmlString, "utf-8");
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return kmlString;
    }

    public abstract Point2D toWgs84(double latitude, double longitude);
}
