/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.api.ui.forms;

import org.controlsfx.control.IndexedCheckModel;

/**
 *
 * @author Patrik Karlström
 */
public interface MFilterSectionPointProvider {

    public IndexedCheckModel getAlarmLevelCheckModel();

    public IndexedCheckModel getAlarmNameCheckModel();

    public IndexedCheckModel getCategoryCheckModel();

    public IndexedCheckModel getGroupCheckModel();

    public IndexedCheckModel getOperatorCheckModel();

    public IndexedCheckModel getOriginCheckModel();

    public IndexedCheckModel getStatusCheckModel();

    public void setAlarmNameCheckModel(IndexedCheckModel checkModel);

    public void setCategoryCheckModel(IndexedCheckModel checkModel);

    public void setFrequencyCheckModel(IndexedCheckModel checkModel);

    public void setGroupCheckModel(IndexedCheckModel checkModel);

    public void setMeasNextCheckModel(IndexedCheckModel checkModel);

    public void setOperatorCheckModel(IndexedCheckModel checkModel);

    public void setOriginCheckModel(IndexedCheckModel checkModel);

    public void setStatusCheckModel(IndexedCheckModel checkModel);

}
