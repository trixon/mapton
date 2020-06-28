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
package org.mapton.api;

/**
 *
 * @author Patrik Karlström
 */
public class MKey {

    public static final String APP_TOOL_LABEL = "app_toolbar.label";
    public static final String APP_TOOL_STARTED = "app_tool.started";
    public static final String BACKGROUND_IMAGE = "background_image";
    public static final String CHART = "chart";
    public static final String CHART_WAIT = "chart.wait";
    public static final String DATA_SOURCES_FILES = "data_sources.files";
    public static final String DATA_SOURCES_WMS_ATTRIBUTIONS = "data_sources.wms.attributions";
    public static final String DATA_SOURCES_WMS_SOURCES = "data_sources.wms.sources";
    public static final String DATA_SOURCES_WMS_STYLES = "data_sources.wms.styles";
    public static final String LAYER_FAST_OPEN_TOOL = "map.layer.fastopen_tool";
    /**
     * Listen for this GlobalState key in order to catch ClearLayersAction
     */
    public static final String MAP_CLEAR_ALL_LAYERS = "map.clear.all.layers";
    public static final String MAP_DOCUMENT_INFO = "map.document.info";
    public static final String MAP_TOOL_STARTED = "map_tool.started";
    public static final String NOTIFICATION = "toolbar.notification";
    public static final String NOTIFICATION_CONFIRM = "toolbar.notification.confirm";
    public static final String NOTIFICATION_ERROR = "toolbar.notification.error";
    public static final String NOTIFICATION_INFORMATION = "toolbar.notification.information";
    public static final String NOTIFICATION_WARNING = "toolbar.notification.warning";
    public static final String OBJECT_PROPERTIES = "object.properties";
    public static final String INDICATOR_LAYER_LOAD = "indicator_layer.load";
    /**
     * Listen for this GlobalState key in order to catch POI category updates
     */
    public static final String POI_CATEGORIES = "poi.categories";
    /**
     * Listen for this GlobalState key in order to catch POI list selection
     * changes
     */
    public static final String POI_SELECTION = "poi.selection";
    /**
     * Listen for this GlobalState key in order to catch POI map selection
     * changes
     */
    public static final String POI_SELECTION_MAP = "poi.selection.map";
    public static final String UPDATER_LOGGER = "updater.logger";
    public static final String WIKIPEDIA_ARTICLE = "wikipedia.article";

}
