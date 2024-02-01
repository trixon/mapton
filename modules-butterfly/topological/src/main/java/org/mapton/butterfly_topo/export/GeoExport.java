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

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoView;
import org.mapton.core.api.ui.ExportConfiguration;
import org.mapton.core.api.ui.ExportProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.io.Geo;
import se.trixon.almond.util.io.GeoHeader;
import se.trixon.almond.util.io.GeoPoint;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = ExportProvider.class)
public class GeoExport extends BaseExportProvider {

    public GeoExport() {
        super(TopoView.class);
        setSupportsEncoding(false);
        setSupportsTransformation(true);
    }

    @Override
    public void export(ExportConfiguration exportConfiguration) {
        var map = new LinkedHashMap<String, String>();
        map.put("Application", "Mapton");
        map.put("Author", SystemHelper.getUserName());
        map.put("Created", LocalDateTime.now().toString());
        var geo = new Geo(new GeoHeader(map));
        var progressHandle = exportConfiguration.getProgressHandle();
        progressHandle.switchToDeterminate(mManager.getTimeFilteredItems().size());
        var i = 1;
        for (var p : mManager.getTimeFilteredItems()) {
            var point = new GeoPoint();
            point.setPointId(p.getName());
            point.setRemark(p.getCategory());
            setCoordinate(exportConfiguration.getCooTrans(), point, p);

            geo.addPoint(point);
            progressHandle.progress(i++);
        }

        try {
            geo.write(exportConfiguration.getFile());
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return SimpleDialog.getExtensionFilters().get("geo");
    }

    @Override
    public String getName() {
        return SimpleDialog.getExtensionFilters().get("geo").getDescription();
    }

    private void setCoordinate(MCooTrans targetCT, GeoPoint point, BTopoControlPoint p) {
        var sourceCT = MOptions.getInstance().getMapCooTrans();
        if (sourceCT == targetCT) {
            point.setX(p.getZeroY());
            point.setY(p.getZeroX());
            point.setZ(p.getZeroZ());
        } else {
            sourceCT.toWgs84(p.getZeroY(), p.getZeroX());
        }
    }

}
