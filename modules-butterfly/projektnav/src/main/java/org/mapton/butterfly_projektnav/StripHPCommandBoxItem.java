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
package org.mapton.butterfly_projektnav;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MCommandBoxItem;
import org.mapton.api.MCommandBoxItemSystem;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCommandBoxItem.class)
public class StripHPCommandBoxItem extends MCommandBoxItemSystem {

    public StripHPCommandBoxItem() {
    }

    @Override
    public String getParent() {
        return "Projektnav";
    }

    @Override
    public Action getAction() {
        return new Action("Rensa _H_P i 'Urklipp'", actionEvent -> {
            String string;
            try {
                string = (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
                string = StringUtils.replaceEach(string, new String[]{"_p", "_P", "_h", "_H"}, new String[]{"", "", "", ""});
                var set = new TreeSet<>(Arrays.asList(StringUtils.split(string)));
                SystemHelper.copyToClipboard(String.join("\n", set));
            } catch (UnsupportedFlavorException | IOException ex) {
//                Exceptions.printStackTrace(ex);
            }

        });
    }

}
