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
package org.mapton.butterfly_topo.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.api.MCooTrans;
import org.mapton.butterfly_topo.TopoView;
import org.mapton.core.api.ui.ExportConfiguration;
import org.mapton.core.api.ui.ExportProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = ExportProvider.class)
public class KmzExport extends ExportProvider {

    public KmzExport() {
        super(TopoView.class);
        setCooTrans(MCooTrans.getCooTrans("WGS 84"));
    }

    @Override
    public void export(ExportConfiguration ec) {
        var generator = new KmlGenerator();
        var kml = generator.generate(ec);

        try {
            kml.marshalAsKmz(ec.getFile().getAbsolutePath());
        } catch (FileNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return SimpleDialog.getExtensionFilters().get("kmz");
    }

    @Override
    public String getName() {
        return "Keyhole Markup Language (*.kmz)";
    }

}
