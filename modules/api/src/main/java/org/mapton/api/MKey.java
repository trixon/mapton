/*
 * Copyright 2022 Patrik Karlström.
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

    public static final String APP_NEWS_DYNAMIC = "app.news.dynamic";
    public static final String APP_THEME_BACKGROUND = "app_theme_background";
    public static final String APP_THEME_FOREGROUND = "app_theme_foreground";
    public static final String APP_TOOL_STARTED = "app_tool.started";
    public static final String BACKGROUND_IMAGE = "background_image";
    public static final String BEFORE_AFTER_IMAGE = "before_after_image";
    public static final String CHART = "chart";
    public static final String CHART_WAIT = "chart.wait";
    public static final String DATA_SOURCES_FILES = "data_sources.files";
    public static final String DATA_SOURCES_WMS_ATTRIBUTIONS = "data_sources.wms.attributions";
    public static final String DATA_SOURCES_WMS_SOURCES = "data_sources.wms.sources";
    public static final String DATA_SOURCES_WMS_STYLES = "data_sources.wms.styles";
    public static final String EXECUTION_FLOW_MAP_INITIALIZED = "execution_flow_map_initialized";
    public static final String EXECUTION_FLOW_MAP_WW_INITIALIZED = "execution_flow_map_ww_initialized";
    public static final String INDICATOR_LAYER_LOAD = "indicator_layer.load";
    public static final String LAYER_FAST_OPEN_TOOL = "map.layer.fastopen_tool";
    public static final String LAYER_SUB_VISIBILITY = "map.layer.sub_visibility";
    /**
     * Listen for this GlobalState key in order to catch ClearLayersAction
     */
    public static final String MAP_CLEAR_ALL_LAYERS = "map.clear.all.layers";
    public static final String MAP_DOCUMENT_INFO = "map.document.info";
    public static final String MAP_POPULATE_CONTEXT_MENY = "map.populate.context.menu";
    public static final String MAP_TOOL_STARTED = "map_tool.started";
    public static final String NOTIFICATION_FX = "toolbar.notification";
    public static final String NOTIFICATION_FX_CONFIRM = "toolbar.notification.confirm";
    public static final String NOTIFICATION_FX_ERROR = "toolbar.notification.error";
    public static final String NOTIFICATION_FX_INFORMATION = "toolbar.notification.information";
    public static final String NOTIFICATION_FX_WARNING = "toolbar.notification.warning";
    public static final String OBJECT_PROPERTIES = "object.properties";
    public static final String UPDATER_LOGGER = "updater.logger";
    public static final String WW_DRAG_LAYER_BUNDLE = "ww.drag.layer_bundle";
    public static final String WW_DRAG_OBJECT = "ww.drag.object";

}
