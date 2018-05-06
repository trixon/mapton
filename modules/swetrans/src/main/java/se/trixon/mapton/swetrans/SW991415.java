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
package se.trixon.mapton.swetrans;

import com.github.goober.coordinatetransformation.positions.SWEREF99Position.SWEREFProjection;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.core.api.CooTransProvider;
import se.trixon.mapton.core.api.MapBounds;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = CooTransProvider.class)
public class SW991415 extends BaseSR {

    public SW991415() {
        mName = "SWEREF 99 14 15";
        mProjection = SWEREFProjection.sweref_99_14_15;
        mBoundsWgs84 = new MapBounds(12.0000, 61.5700, 15.5000, 64.4700);
    }
}
