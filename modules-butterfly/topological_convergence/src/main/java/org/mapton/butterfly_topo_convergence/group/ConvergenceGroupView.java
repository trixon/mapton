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
package org.mapton.butterfly_topo_convergence.group;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.ObservableList;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.ml.clustering.DBSCANClusterer;
import org.apache.commons.math3.ml.distance.DistanceMeasure;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.Mapton;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_core.api.CopyNamesAction;
import org.mapton.butterfly_core.api.ExternalSearchAction;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo_convergence.api.ConvergenceGroupManager;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupView {

    private final DistanceMeasure mDistanceMeasure;
    private final ConvergenceGroupFilter mFilter = new ConvergenceGroupFilter();
    private final ConvergenceGroupFilterPopOver mFilterPopOver = new ConvergenceGroupFilterPopOver(mFilter);
    private final MFilterPresetPopOver mFilterPresetPopOver = new MFilterPresetPopOver(mFilterPopOver, "topo.convergence");
    private final SingleListForm<ConvergenceGroupManager, BTopoConvergenceGroup> mListForm;
    private final ConvergenceGroupManager mManager = ConvergenceGroupManager.getInstance();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public ConvergenceGroupView() {
        mDistanceMeasure = (DistanceMeasure) (double[] a, double[] b) -> {
            var plane = Math.hypot(b[1] - a[1], b[0] - a[0]);
            var height = Math.abs(b[2] - a[2]);
            if (height < 1.0) {
                return Double.MAX_VALUE;
            } else {
                return plane;
            }
        };

        var createAction = new Action(Dict.ADD.toString(), actionEvent -> createGroup(mTopoManager.getTimeFilteredItems()));
        createAction.setGraphic(MaterialIcon._Content.ADD.getImageView(Mapton.getIconSizeToolBarInt()));
        var create2Action = new Action(Dict.ADD.toString(), actionEvent -> createGroups());
        create2Action.setGraphic(MaterialIcon._Content.ADD_CIRCLE_OUTLINE.getImageView(Mapton.getIconSizeToolBarInt()));

        var actions = Arrays.asList(
                new ExternalSearchAction(mManager),
                createAction,
                new CopyNamesAction(mManager),
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilter.getInfoPopOver().getAction(),
                mFilterPresetPopOver.getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_ConvergenceGroupAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new ConvergenceGroupListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.GROUP.toString(),
                Dict.COMMENT.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

    private void createGroup(ObservableList<BTopoControlPoint> items) {
        if (items.isEmpty()) {
            return;
        }

        var ref = String.join(",", items.stream().map(p -> p.getName()).toList());
        var g = new BTopoConvergenceGroup();
        g.setName("Dynamic " + LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        g.setLimit(0.010);
        g.setRef(ref);

        var p = items.getFirst();
        g.setCategory(p.getCategory());
        g.setGroup(p.getGroup());
        g.setStatus(p.getStatus());
        g.setZeroX(items.stream().mapToDouble(pp -> pp.getZeroX()).average().orElse(0));
        g.setZeroY(items.stream().mapToDouble(pp -> pp.getZeroY()).average().orElse(0));
//        g.setZeroZ(items.stream().mapToDouble(pp -> pp.getZeroZ()).average().orElse(0));
        g.setZeroZ(0.0);
        g.setButterfly(p.getButterfly());
        g.setDimension(BDimension._3d);
        g.setFrequency(0);
        g.setMeasurementMode(BMeasurementMode.UNDEFINED);
        ButterflyManager.getInstance().calculateLatLons(new ArrayList<>(List.of(g)));
        mManager.add(g);
    }

    private void createGroups() {
        var epsilon = 2.5;
        var minPoints = 1;
        var dbscan = new DBSCANClusterer<BTopoControlPoint>(epsilon, minPoints, mDistanceMeasure);
        var filteredPoints = mTopoManager.getTimeFilteredItems().stream()
                //                .filter(p -> p.getDimension() == BDimension._3d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .toList();

        if (filteredPoints.isEmpty()) {
            return;
        }
        var clusters = dbscan.cluster(filteredPoints);

    }
}
