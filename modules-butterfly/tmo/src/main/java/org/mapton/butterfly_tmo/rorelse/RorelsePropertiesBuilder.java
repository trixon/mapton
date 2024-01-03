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
package org.mapton.butterfly_tmo.rorelse;

import java.util.LinkedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class RorelsePropertiesBuilder extends PropertiesBuilder<BRorelse> {

    @Override
    public Object build(BRorelse r) {
        if (r == null) {
            return r;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, "Dubbnamn"), r.getDubbnamn());
        String placering;
        if (StringUtils.isNotBlank(r.getPlacering_kommentar())) {
            placering = "%s (%s)".formatted(r.getPlacering(), r.getPlacering_kommentar());
        } else {
            placering = r.getPlacering();
        }
        propertyMap.put(getCatKey(cat1, "Placering"), placering);
        propertyMap.put(getCatKey(cat1, "Lägesbeskrivning"), r.getLägesbeskrivning());
        propertyMap.put(getCatKey(cat1, "Status"), r.getStatus());
        propertyMap.put(getCatKey(cat1, "Anmärkning"), r.getAnmärkning());
        propertyMap.put(getCatKey(cat1, "Fixpunkt"), r.getFixpunkt());
//
        propertyMap.put(getCatKey(cat1, "Installationsdatum"), r.getInstallationsdatum());
        propertyMap.put(getCatKey(cat1, "Inventeringsdatum"), r.getInventeringsdatum());
        propertyMap.put(getCatKey(cat1, "Versionsdatum"), r.getVersionsdatum());

        propertyMap.put(getCatKey(cat1, "Koordinatkvalitet"), r.getKoordinatkvalitet());
        propertyMap.put(getCatKey(cat1, "Höjd"), r.getHöjd());
        propertyMap.put(getCatKey(cat1, "Plan"), r.getPlan());
        propertyMap.put(getCatKey(cat1, "N"), r.getY());
        propertyMap.put(getCatKey(cat1, "E"), r.getX());
        propertyMap.put(getCatKey(cat1, "Lat"), r.getLat());
        propertyMap.put(getCatKey(cat1, "Lon"), r.getLon());
        propertyMap.put(getCatKey(cat1, "Gammalt id"), r.getGammalt_id());
        propertyMap.put(getCatKey(cat1, "Informationskällor"), r.getInformationskällor());
        propertyMap.put(getCatKey(cat1, "Kontrollprogram"), r.getKontrollprogram());

        return propertyMap;
    }

}
