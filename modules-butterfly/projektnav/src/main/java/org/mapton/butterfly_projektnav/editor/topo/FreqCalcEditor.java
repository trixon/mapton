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
package org.mapton.butterfly_projektnav.editor.topo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.mapton.api.MPoi;
import org.mapton.api.MTemporaryPoiManager;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MSplitNavSettings;
import org.mapton.butterfly_activities.api.ActManager;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_projektnav.RuleFreqFormulaConfig;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MEditor.class)
public class FreqCalcEditor extends BaseTopoEditor {

    private final ActManager mActManager = ActManager.getInstance();
    private BorderPane mBorderPane;
    private final RuleFreqFormulaConfig mConfig = RuleFreqFormulaConfig.getInstance();
    private final LogPanel mLogPanel = new LogPanel();
    private final String mName = "Auto-frekvens";
    private final MTemporaryPoiManager mTempPoiManager = MTemporaryPoiManager.getInstance();

    public FreqCalcEditor() {
        setName(mName);
        getSplitNavSettings().setTitleMode(MSplitNavSettings.TitleMode.FULL_PATH);
    }

    @Override
    public Node getNode() {
        if (mBorderPane == null) {
            initToolBar();
            createUI();
            initListeners();
        }

        update();

        return mBody;
    }

    private void createUI() {
        mLogPanel.setWrapText(true);
        mBorderPane = new BorderPane(mLogPanel);
        mNotificationPane.setContent(mBorderPane);
    }

    private void initListeners() {
    }

    private void initToolBar() {
    }

    private void update() {
        var pointToFormulaMap = new HashMap<BTopoControlPoint, String>();
        var pointsWithActiveFormula = mManager.getAllItems().stream()
                .filter(p -> {
                    var map = p.ext().getMetaAsMap();
                    boolean hasEnabledFormula = map.containsKey("FREQ_FORMULA")
                            && !map.getOrDefault("FREQ_ENABLED", "1").equalsIgnoreCase("0");

                    if (!hasEnabledFormula) {
                        return false;
                    }

                    var formulaId = map.get("FREQ_FORMULA");
                    var formula = mConfig.getConfig().getString(formulaId, null);
                    if (formula == null) {
                        return false;
                    }

                    pointToFormulaMap.put(p, formula);

                    return true;
                })
                .toList();
        //--< CUT
        //Use all filtered during simulation
        pointsWithActiveFormula = mManager.getTimeFilteredItems();
        for (var p : pointsWithActiveFormula) {
            pointToFormulaMap.put(p, "-1:365,50:1,100:7,150:14,200:28");
        }
        //--< CUT
        mLogPanel.clear();
        mLogPanel.println(LocalDateTime.now().toString());

        pointsWithActiveFormula.forEach(p -> {
            mLogPanel.println("");
            mLogPanel.println(p.getName());
            mLogPanel.println("formula: " + pointToFormulaMap.get(p));
            mLogPanel.println(p.ext().getMetaAsString());
            for (Map.Entry<String, String> entry : p.ext().getMetaAsMap().entrySet()) {
                mLogPanel.println("key: " + entry.getKey());
                mLogPanel.println("val: " + entry.getValue());
            }
        });

        var triggerAreas = mActManager.getAllItems().stream()
                .filter(aa -> aa.getStatus() == BAreaActivity.BAreaStatus.TRIGGER)
                .filter(aa -> aa.getTargetGeometry() != null)
                .toList();

        var geometryFactory = JTSFactoryFinder.getGeometryFactory();
        var tempPois = new ArrayList<MPoi>();

        pointsWithActiveFormula.forEach(p -> {
            var distanceToClosestTriggerArea = Double.MAX_VALUE;
            var coordinate = new Coordinate(p.getZeroY(), p.getZeroX());
            var point = geometryFactory.createPoint(coordinate);

            for (var ta : triggerAreas) {
                distanceToClosestTriggerArea = Math.min(distanceToClosestTriggerArea, ta.getTargetGeometry().distance(point));
            }

            var formula = pointToFormulaMap.get(p);
            var map = new TreeMap<Integer, Integer>();

            for (var string : StringUtils.split(formula, ",")) {
                var item = StringUtils.split(string, ":");
                map.put(Integer.valueOf(item[0]), Integer.valueOf(item[1]));
            }

            var frequency = map.getOrDefault(-1, 365);

            for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
                if (distanceToClosestTriggerArea <= entry.getKey()) {
                    frequency = entry.getValue();
                    break;
                }
            }

            if (!Objects.equals(frequency, p.getFrequency())) {
                var poi = new MPoi();
                poi.setName("%s, %d (%d)".formatted(p.getName(), frequency, p.getFrequency()));
                poi.setName("%d".formatted(frequency));
                poi.setLatitude(p.getLat());
                poi.setLongitude(p.getLon());
                poi.setColor("00FFFF");

                tempPois.add(poi);
                //TODO que this for commit
                /*
        Spärr för uppdatering?
        om ålder >frekvens och ny frekvens är ett större tal
                 */
            }

        });
        mTempPoiManager.getItems().setAll(tempPois);
    }
}
