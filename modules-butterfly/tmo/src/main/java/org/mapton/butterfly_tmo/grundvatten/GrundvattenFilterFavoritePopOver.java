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
package org.mapton.butterfly_tmo.grundvatten;

import org.mapton.butterfly_core.api.BaseFilterFavoritePopOver;
import org.mapton.butterfly_core.api.BaseFilterPopOver;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenFilterFavoritePopOver extends BaseFilterFavoritePopOver<GrundvattenFilterFavorite> {

    public GrundvattenFilterFavoritePopOver(BaseFilterPopOver filterPopOver) {
        super(GrundvattenFilterFavorite.class, filterPopOver);
    }

    @Override
    public void onEdit(String title, GrundvattenFilterFavorite filterFavorite) {
        if (filterFavorite == null) {
            filterFavorite = new GrundvattenFilterFavorite();
        }

        filterFavorite.setName("objekt GV");
        filterFavorite.setNameObject("NameObjekt GV");
    }

}