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
package org.mapton.butterfly_structural.strain;

import java.util.Arrays;
import javafx.scene.layout.Pane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.ui.forms.ListFormConfiguration;
import org.mapton.api.ui.forms.SingleListForm;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class StrainView {

    private final StrainFilter mFilter = new StrainFilter();
    private final StrainFilterPopOver mFilterPopOver = new StrainFilterPopOver(mFilter);
    private final SingleListForm<StrainManager, BStructuralStrainGaugePoint> mListForm;
    private final StrainManager mManager = StrainManager.getInstance();

    public StrainView() {
        var actions = Arrays.asList(
                ActionUtils.ACTION_SPAN,
                mManager.geZoomExtentstAction(),
                mFilterPopOver.getAction()
        );

        mListForm = new SingleListForm<>(mManager, Bundle.CTL_StrainAction());
        var listFormConfiguration = new ListFormConfiguration()
                .setUseTextFilter(true)
                .setToolbarActions(actions);

        mFilter.bindFreeTextProperty(mListForm.freeTextProperty());
        mListForm.applyConfiguration(listFormConfiguration);
        mListForm.getListView().setCellFactory(listView -> new StrainListCell());

        mListForm.setFreeTextTooltip(
                Dict.NAME.toString()
        );
    }

    public Pane getView() {
        return mListForm.getView();
    }

}
