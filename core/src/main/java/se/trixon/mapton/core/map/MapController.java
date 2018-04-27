/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.map;

import com.lynden.gmapsfx.javascript.object.GoogleMap;
import se.trixon.mapton.core.Mapton;
import se.trixon.mapton.core.MaptonOptions;

/**
 *
 * @author Patrik Karlström
 */
public class MapController {

    private final MaptonOptions mOptions = MaptonOptions.getInstance();

    public static MapController getInstance() {
        return Holder.INSTANCE;
    }

    private MapController() {
    }

    public void goHome() {
        getMap().panTo(mOptions.defaultHome());
    }

    private GoogleMap getMap() {
        return Mapton.getInstance().getMap();
    }

    private static class Holder {

        private static final MapController INSTANCE = new MapController();
    }

}
