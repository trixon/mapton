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
package org.mapton.butterfly_tmo.grundvatten;

import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenPropertiesBuilder extends PropertiesBuilder<BGrundvatten> {

    @Override
    public Object build(BGrundvatten p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, "Lägesbeskrivning"), p.getLägesbeskrivning());
//        propertyMap.put(getCatKey(cat1, Dict.GROUP.toString()), p.getGroup());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
//        propertyMap.put(getCatKey(cat1, "Mätintervall"), "TODO");
//        propertyMap.put(getCatKey(cat1, "Mätintervall, enhet"), "TODO");
//        propertyMap.put(getCatKey(cat1, "Mätansvarig"), "TODO");
        propertyMap.put(getCatKey(cat1, "N"), p.getY());
        propertyMap.put(getCatKey(cat1, "E"), p.getX());
        propertyMap.put(getCatKey(cat1, "Koordinatkvalitet"), p.getKoordinatkvalitet());
        propertyMap.put(getCatKey(cat1, "Plan"), p.getPlan());
        propertyMap.put(getCatKey(cat1, "Höjd"), p.getHöjd());
        propertyMap.put(getCatKey(cat1, "Status"), p.getStatus());
        propertyMap.put(getCatKey(cat1, "Grundvattenmagasin"), p.getGrundvattenmagasin());
        propertyMap.put(getCatKey(cat1, "Referensnivå"), MathHelper.convertDoubleToStringWithSign(p.getReferensnivå(), 2));
        propertyMap.put(getCatKey(cat1, "Spetsnivå"), MathHelper.convertDoubleToStringWithSign(p.getSpetsnivå(), 2));
        propertyMap.put(getCatKey(cat1, "Marknivå"), MathHelper.convertDoubleToStringWithSign(p.getMarknivå(), 2));
        propertyMap.put(getCatKey(cat1, "Gradning"), p.getGradning());
        propertyMap.put(getCatKey(cat1, "Rörlutningsriktning"), p.getRörlutningsriktning());
        propertyMap.put(getCatKey(cat1, "Rördimension"), p.getRördimension());
        propertyMap.put(getCatKey(cat1, "Rörtyp"), p.getRörtyp());
        propertyMap.put(getCatKey(cat1, "Filterlängd"), p.getFilterlängd());
        propertyMap.put(getCatKey(cat1, "Max pejlbart djup"), p.getMaxPejlbartDjup());
        propertyMap.put(getCatKey(cat1, "Filtertyp"), p.getFiltertyp());
        propertyMap.put(getCatKey(cat1, "Spetstyp"), p.getSpetstyp());
        propertyMap.put(getCatKey(cat1, "Igengjuten"), p.getIgengjuten());
        propertyMap.put(getCatKey(cat1, "Informationskällor"), p.getInformationskällor());
        propertyMap.put(getCatKey(cat1, "Kontrollprogram"), p.getKontrollprogram());
//        propertyMap.put(getCatKey(cat1, ""), p.get);
//        propertyMap.put(getCatKey(cat1, ""), p.get);
        propertyMap.put(getCatKey(cat1, "Mätvärden"), p.ext().getNumOfObservations());
        if (!p.ext().getObservationsAllRaw().isEmpty()) {
            propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getMeasurementAge(ChronoUnit.DAYS));
            propertyMap.put(getCatKey(cat1, "Senaste"), GrundvattenHelper.getLevelAndDate(p.ext().getObservationRawLast()));
            propertyMap.put(getCatKey(cat1, "Första"), GrundvattenHelper.getLevelAndDate(p.ext().getObservationRawFirst()));
//            propertyMap.put(getCatKey(cat1, "Min"), GrundvattenHelper.getLevelAndDate(p.ext().getMinObservation()));
//            propertyMap.put(getCatKey(cat1, "Max"), GrundvattenHelper.getLevelAndDate(p.ext().getMaxObservation()));
        }

        return propertyMap;
    }

}
