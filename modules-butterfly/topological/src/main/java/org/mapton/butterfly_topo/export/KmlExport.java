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

import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.butterfly_topo.TopoView;
import org.mapton.core.api.ui.ExportConfiguration;
import org.mapton.core.api.ui.ExportProvider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = ExportProvider.class)
public class KmlExport extends ExportProvider {

    public KmlExport() {
        super(TopoView.class);
        setSupportsEncoding(true);
        setSupportsTransformation(true);
    }

    @Override
    public void export(ExportConfiguration exportConfiguration) {
        System.out.println("save kml");
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return SimpleDialog.getExtensionFilters().get("kml");
    }

    @Override
    public String getName() {
        return "KML";
    }

}
