/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_projektnav;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TreeSet;
import java.util.stream.DoubleStream;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.Strings;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ParameterEditorZero extends ParameterEditorBase {

    public final String DEFAULT_INSTRUMENT = "BERÄKNAD";
    public final String DEFAULT_OPERATOR = "Mapton";
    private final RadioButton mCountDaysRadioButton = new RadioButton("...dagar");
    private final RadioButton mCountMeasurementsRadioButton = new RadioButton("...mätningar");
    private final ToggleGroup mCountToggleGroup = new ToggleGroup();
    private final DatePicker mDatePicker = new DatePicker(LocalDate.parse("2030-12-15"));
    private final DateTimeFormatter mDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final TextField mInstrumentTextField = new TextField(DEFAULT_INSTRUMENT);
    private final Spinner<Integer> mMaxSpinner = new Spinner<>(2, 999, 10, 1);
    private final Spinner<Integer> mMinSpinner = new Spinner<>(2, 999, 3, 1);
    private final RadioButton mModeAvgRadioButton = new RadioButton("Medelvärde");
    private final RadioButton mModeMedianRadioButton = new RadioButton("Medianvärde");
    private final ToggleGroup mModeToggleGroup = new ToggleGroup();
    private final TextField mOperatorTextField = new TextField(DEFAULT_OPERATOR);

    public ParameterEditorZero() {
        createUI();
    }

    public String preview(BaseManager<? extends BXyzPoint> manager, String[] names) {
        var sb = new StringBuilder();
        var nameSet = new TreeSet<String>();
        for (var name : names) {
            var p = manager.getAllItemsMap().get(name);
            var row = calculateZero(p);
            if (row != null) {
                sb.append(row).append('\n');
            } else {
                nameSet.add(p.getName());
            }
        }

        if (!sb.isEmpty()) {
            sb.insert(0, "nm\tprojid\tnr\tdtm\tn\te\th\tinr\topr"
                    + "\t#Antal\t#MinDat\t#MaxDat\t#SpannN\t#SpannE\t#SpannH\t#NollDat\t#DiffN\t#DiffE\t#DiffH"
                    + "\n");
        }

        if (!nameSet.isEmpty()) {
            sb.append("\n\nInte tillräckligt med observationer\n");
            sb.append(String.join("\n", nameSet));
        }

        return sb.toString();
    }

    private String calculateZero(BXyzPoint p) {
        var observationsTimeFiltered = p.extOrNull().getObservationsTimeFiltered();
        var min = mMinSpinner.getValue();
        var max = mMaxSpinner.getValue();

        if (observationsTimeFiltered.size() < min) {
            return null;
        }

        HashMap<String, String> originToId = Mapton.getGlobalState().getOrDefault("ParamEditor.originToId", new HashMap<String, String>());
        var projId = originToId.get(p.getOrigin());
        var startDate = mDatePicker.getValue();
//        var endDate = startDate.plusDays(max + 1).atStartOfDay();
        var observations = new ArrayList<BXyzPointObservation>();
        var firstDate = observationsTimeFiltered.getFirst().getDate();
        var endDate = firstDate.plusDays(max);

        for (var o : observationsTimeFiltered) {
            if (observations.size() == max) {
                break;
            }

            if (DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), startDate)) {
                if (mCountDaysRadioButton.isSelected()) {
                    if (o.getDate().isAfter(endDate)) {
                        break;
                    }
                }
                if (!Strings.CI.equals(o.getInstrument(), DEFAULT_INSTRUMENT)) {
                    observations.add(o);
                }
            }
        }

        if (observations.size() < min) {
            return null;
        } else {
            var minN = p.getZeroX();
            var minE = p.getZeroY();
            var minH = p.getZeroZ();
            var maxN = minN;
            var maxE = minE;
            var maxH = minH;

            if (p.getDimension() != BDimension._2d) {
                minH = observations.stream().mapToDouble(o -> o.getMeasuredZ()).min().getAsDouble();
                maxH = observations.stream().mapToDouble(o -> o.getMeasuredZ()).max().getAsDouble();
            }

            if (p.getDimension() != BDimension._1d) {
                minN = observations.stream().mapToDouble(o -> o.getMeasuredY()).min().getAsDouble();
                minE = observations.stream().mapToDouble(o -> o.getMeasuredX()).min().getAsDouble();
                maxN = observations.stream().mapToDouble(o -> o.getMeasuredY()).max().getAsDouble();
                maxE = observations.stream().mapToDouble(o -> o.getMeasuredX()).max().getAsDouble();
            }
            double n = p.getZeroY(), e = p.getZeroX(), h = p.getZeroZ();

            if (mModeAvgRadioButton.isSelected()) {
                if (p.getDimension() != BDimension._2d) {
                    h = observations.stream().mapToDouble(o -> o.getMeasuredZ()).average().getAsDouble();
                }
                if (p.getDimension() != BDimension._1d) {
                    n = observations.stream().mapToDouble(o -> o.getMeasuredY()).average().getAsDouble();
                    e = observations.stream().mapToDouble(o -> o.getMeasuredX()).average().getAsDouble();
                }

            } else {
                if (p.getDimension() != BDimension._2d) {
                    h = getMedian(observations.stream().mapToDouble(o -> o.getMeasuredZ()));
                }
                if (p.getDimension() != BDimension._1d) {
                    n = getMedian(observations.stream().mapToDouble(o -> o.getMeasuredY()));
                    e = getMedian(observations.stream().mapToDouble(o -> o.getMeasuredX()));
                }
            }
            var date = mDateTimeFormatter.format(observations.getFirst().getDate().minusMinutes(1));
            var minDate = observations.getFirst().getDate().toLocalDate();
            var maxDate = observations.getLast().getDate().toLocalDate();
            var dateZero = p.getDateZero();
            var diffN = 0d;
            var diffE = 0d;
            var diffH = 0d;
            if (p.getDimension() != BDimension._2d) {
                diffH = h - p.getZeroZ();

            }
            if (p.getDimension() != BDimension._1d) {
                diffN = n - p.getZeroY();
                diffE = e - p.getZeroX();
            }

            var row = String.format(Locale.ENGLISH, "1\t%s\t%s\t%s\t%.3f\t%.3f\t%.3f\t%s\t%s\t%d\t%s\t%s\t%.0f\t%.0f\t%.0f\t%s\t%.0f\t%.0f\t%.0f",
                    projId,
                    p.getName(),
                    date,
                    n,
                    e,
                    h,
                    mInstrumentTextField.getText(),
                    mOperatorTextField.getText(),
                    observations.size(),
                    minDate.toString(),
                    maxDate.toString(),
                    1000 * (maxN - minN),
                    1000 * (maxE - minE),
                    1000 * (maxH - minH),
                    dateZero.toString(),
                    1000 * diffN,
                    1000 * diffE,
                    1000 * diffH
            );

            return row;
        }
    }

    private void createUI() {
        mModeAvgRadioButton.setToggleGroup(mModeToggleGroup);
        mModeMedianRadioButton.setToggleGroup(mModeToggleGroup);
        mModeToggleGroup.selectToggle(mModeMedianRadioButton);
        mCountDaysRadioButton.setToggleGroup(mCountToggleGroup);
        mCountMeasurementsRadioButton.setToggleGroup(mCountToggleGroup);
        mCountToggleGroup.selectToggle(mCountDaysRadioButton);
        mInstrumentTextField.setDisable(true);
        mOperatorTextField.setDisable(true);

        int row = 0;
        addRow(row++, mModeAvgRadioButton, mModeMedianRadioButton);
        addRow(row++, new Label("Första möjliga startdatum"));
        addRow(row++, mDatePicker);
        addRow(row++, new Label("Min antal"), new Label("Max antal"));
        addRow(row++, mMinSpinner, mMaxSpinner);
        addRow(row++, new Label(), new HBox(mCountDaysRadioButton, mCountMeasurementsRadioButton));
        addRow(row++, new Label("Instrument"), new Label("Operatör"));
        addRow(row++, mInstrumentTextField, mOperatorTextField);

        FxHelper.setEditable(true, mMaxSpinner, mMinSpinner);
        FxHelper.autoSizeColumn(this, 3);
    }

    private double getMedian(DoubleStream stream) {
        var values = stream.sorted().toArray();
        int length = values.length;

        if (length == 0) {
            throw new IllegalArgumentException("Stream is empty");
        } else if (length % 2 == 1) {
            return values[length / 2];
        } else {
            return (values[length / 2 - 1] + values[length / 2]) / 2.0;
        }
    }

    public record Meas(String name, String date) {

    }
}
