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
public class SW991200 extends BaseSR {

    public SW991200() {
        mName = "SWEREF 99 12 00";
        mProjection = SWEREFProjection.sweref_99_12_00;
        mBoundsWgs84 = new MapBounds(10.5700, 55.8500, 13.0800, 60.2500);
        mBoundsProjected = new MapBounds(60436.5084, 6192389.5650, 217643.4713, 6682784.4276);
    }
}