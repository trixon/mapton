/*
 * Copyright (C) 2021 Peab AB - All Rights Reserved.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Patrik Karlstrom <patrik.karlstrom@peab.se>, May 2018
 *
 */
package org.mapton.api.ui;

import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MInfoPopOver extends MPopOver {

    public MInfoPopOver() {
        setTitle(Dict.INFORMATION.toString());
        getAction().setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(getIconSizeToolBarInt()));
    }

}
