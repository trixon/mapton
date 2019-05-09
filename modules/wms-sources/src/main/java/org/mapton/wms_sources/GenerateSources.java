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
package org.mapton.wms_sources;

import java.util.ArrayList;
import java.util.TreeMap;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MAttribution;
import org.mapton.api.MWmsSource;

/**
 *
 * @author Patrik Karlström
 */
public class GenerateSources extends Generator {

    private final ArrayList<MWmsSource> mSources = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GenerateSources();
    }

    public GenerateSources() {
        initLantmateriet(true);
        initSwedGeo(false);
        initEOX(true);
        initNASA(true);
        initVirtualEarth(true);

        String json = gson.toJson(mSources);
        System.out.println(json);
    }

    private MWmsSource createSource(String name, String url, TreeMap<String, String> layers, TreeMap<String, MAttribution> attributions, boolean enabled) {
        MWmsSource source = new MWmsSource();
        source.setName(name);
        source.setEnabled(enabled);
        source.setUrl(url);
        source.setLayers(layers);
        source.setAttributions(attributions);

        return source;
    }

    private void initEOX(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("blackmarble", "at.eox.blackmarble");
        layers.put("coastline", "at.eox.coastline");
        layers.put("hydrography", "at.eox.hydrography");
        layers.put("osm", "at.eox.osm");
        layers.put("s2cloudless-2018", "at.eox.s2cloudless");
        layers.put("streets", "at.eox.streets");
        layers.put("terrain", "at.eox.terrain");
        layers.put("terrain-light", "at.eox.terrain-light");

        MAttribution s2Attribution = new MAttribution();
        s2Attribution.setProviderName("EOX");
        s2Attribution.setProviderUrl("https://eox.at");
        s2Attribution.setLicenseName("CC BY-NS-SA 4.0");
        s2Attribution.setLicenseUrl("https://creativecommons.org/licenses/by-nc-sa/4.0/");
        s2Attribution.setRawHtml("<a href=\"https://s2maps.eu\">Sentinel-2 cloudless – https://s2maps.eu</a> by <a href=\"https://eox.at/\">EOX IT Services GmbH</a> (Contains modified Copernicus Sentinel data 2017 & 2018)");

        ObjectUtils.clone(s2Attribution);

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("at.eox.s2cloudless", s2Attribution);

        mSources.add(createSource(
                "EOX",
                "http://tiles.maps.eox.at/wms?service=wms&request=getcapabilities",
                layers,
                attributions,
                enabled
        ));
    }

    private void initLantmateriet(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("OI.Histortho_60", "se.lm.hist_orto_60");
        layers.put("OI.Histortho_75", "se.lm.hist_orto_75");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Lantmäteriet");
        attribution.setProviderUrl("https://opendata.lantmateriet.se/#apis?api=Historiska-ortofoton_WMS&version=v1");
        attribution.setLicenseName("Creative Commons - CC0");
        attribution.setLicenseUrl("https://creativecommons.org/publicdomain/zero/1.0/");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("se.lm.hist_orto_60", attribution);
        attributions.put("se.lm.hist_orto_75", attribution);

        mSources.add(createSource(
                "Lantmäteriet",
                "https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms",
                layers,
                attributions,
                enabled
        ));
    }

    private void initNASA(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("AURA_UVI_CLIM_M", "gov.nasa.neo.uv");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("NASA Earth Observations");
        attribution.setProviderUrl("https://neo.sci.gsfc.nasa.gov/view.php?datasetId=AURA_UVI_CLIM_M");
        attribution.setLicenseName("\"Freely available for public use\"");
        attribution.setLicenseUrl("https://neo.sci.gsfc.nasa.gov/about/");
        attribution.setRawHtml("Imagery by Jesse Allen, NASA Earth Observatory, based on data provided by Jerry Ziemke from the Ozone Monitoring Instrument (OMI) science team.");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("gov.nasa.neo.uv", attribution);

        mSources.add(createSource(
                "NEO",
                "https://neo.sci.gsfc.nasa.gov/wms/wms?version=1.3.0&service=WMS&request=GetCapabilities",
                layers,
                attributions,
                enabled
        ));
    }

    private void initSwedGeo(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("orto025", "se.lm.orto_025");
        layers.put("topowebbkartan", "se.lm.topoweb");
        layers.put("topowebbkartan_nedtonad", "se.lm.topoweb_dim");
        layers.put("fastighet_text", "se.lm.fastighet_text");
        layers.put("fastighet_granser", "se.lm.fastighet_granser");
        layers.put("terrangskuggning", "se.lm.terrangskuggning");

        MAttribution attribution = new MAttribution();

        TreeMap<String, MAttribution> attributions = new TreeMap<>();

        mSources.add(createSource(
                "Lantmäteriet",
                "http://gis.swedgeo.se/geoserver/lantmateriet/wms?",
                layers,
                attributions,
                enabled
        ));
    }

    private void initVirtualEarth(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("ve-a", "net.emxsys.ve-a");
        layers.put("ve-h", "net.emxsys.ve-h");
        layers.put("ve-r", "net.emxsys.ve-r");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Virtual Earth");
        attribution.setProviderUrl("http://emxsys.com");
        attribution.setLicenseName("\"Unknown\"");
        attribution.setLicenseUrl("https://example.org/");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("net.emxsys.ve-a", attribution);
        attributions.put("net.emxsys.ve-h", attribution);
        attributions.put("net.emxsys.ve-r", attribution);

        mSources.add(createSource(
                "Virtual Earth",
                "https://emxsys.net/worldwind27/wms/virtualearth?request=GetCapabilities&service=WMS",
                layers,
                attributions,
                enabled
        ));
    }
}
