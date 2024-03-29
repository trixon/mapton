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

import java.util.Arrays;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class RorelseView {

    private final RorelseFilter mFilter = new RorelseFilter();
    private final RorelseFilterPopOver mFilterPopOver = new RorelseFilterPopOver(mFilter);
    private final SingleListForm<RorelseManager, BRorelse> mListForm;
    private final RorelseManager mManager = RorelseManager.getInstance();

    public RorelseView() {
        var exportAction = new Action("Sammanfoga med kontrollpunkter", actionEvent -> {
            new Merger().merge();
        });
        exportAction.setGraphic(MaterialIcon._Editor.MERGE_TYPE.getImageView(getIconSizeToolBarInt()));

        var actions = Arrays.asList(
                exportAction,
                ActionUtils.ACTION_SPAN,
                mFilter.getInfoPopOver().getAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_RorelseAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new RorelseListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString(),
                Dict.GROUP.toString(),
                Dict.COMMENT.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
