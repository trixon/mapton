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
package org.mapton.butterfly_topo.report.grade;

import static j2html.TagCreator.body;
import static j2html.TagCreator.h1;
import static j2html.TagCreator.hr;
import static j2html.TagCreator.html;
import static j2html.TagCreator.pre;
import j2html.tags.ContainerTag;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ResourceBundle;
import org.mapton.api.report.MReport;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import org.mapton.butterfly_topo.grade.horizontal.GradeHManager;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class SelectedGradeHReport extends BaseTopoGradeReport {

    private final GradeHManager mManager = GradeHManager.getInstance();
    private final String mName = "Differentialsättning, valt punktpar";
    protected final ResourceBundle mBundle = NbBundle.getBundle(GradeManagerBase.class);
    private static final String DATE_PATTERN = "YYYY-'W'ww";
    private final DateTimeFormatter mWeeklyAvgFormatterTo = DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault());

    public SelectedGradeHReport() {
        setName(mName);
    }

    @Override
    public ContainerTag getContent() {
        var p = mManager.getSelectedItem();
        if (p == null) {
            return html();
        }

        var sb = new StringBuilder();
        var rows = new ArrayList<ArrayList<String>>();

        addRow(rows, p.getName());
        addRow(rows, Dict.DATE.toString(), p.getPeriod());
        addRow(rows, Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower()), p.ext().getNumOfCommonObservations());
        addRow(rows, Dict.NUM_OF_S.toString().formatted(Dict.Time.DAYS.toLower()), p.ext().getNumOfCommonDays());
        addRow(rows, Dict.AGE.toString(), p.ext().getNumOfDaysSinceLast());
        addRow(rows, mBundle.getString("filterDeltaH"), MathHelper.convertDoubleToString(p.getDistanceHeight(), 3));
        addRow(rows, mBundle.getString("filterDeltaR"), MathHelper.convertDoubleToString(p.getDistancePlane(), 3));
        addRow(rows, mBundle.getString("filterDabbaH"), "%.1f".formatted(p.ext().getDiff().getPartialDiffZ() * 1000));
        addRow(rows, mBundle.getString("filterDabbaR"), "%.1f".formatted(p.ext().getDiff().getPartialDiffR() * 1000));

        addRow(rows, "Vecka", mBundle.getString("filterDabbaH"), "1:1000");
        p.getCommonObservations().entrySet().forEach(entry -> {
            var date = entry.getKey();
            var p1 = entry.getValue();
            var p2 = entry.getValue();
            var yyyyww = date.format(mWeeklyAvgFormatterTo);
            var gradeDiff = p.ext().getDiff(p.getFirstObservation(), p2);
            var partialDiff = MathHelper.convertDoubleToString(gradeDiff.getPartialDiffZ(), 3);
            var zPerMille = MathHelper.convertDoubleToString(gradeDiff.getZPerMille(), 1);
            addRow(rows, yyyyww, partialDiff, zPerMille);
        });

        for (var columns : rows) {
            sb.append(String.join("\t", columns)).append("\n");
        }

        var html = html(
                body(
                        h1(mName),
                        hr(),
                        pre(sb.toString())
                )
        );

        return html;
    }

}
