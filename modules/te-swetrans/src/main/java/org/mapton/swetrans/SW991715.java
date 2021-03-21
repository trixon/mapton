/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.swetrans;

import com.github.goober.coordinatetransformation.positions.SWEREF99Position.SWEREFProjection;
import org.openide.util.lookup.ServiceProvider;
import org.mapton.api.MBounds;
import org.mapton.api.MCooTrans;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCooTrans.class)
public class SW991715 extends BaseSR {

    public SW991715() {
        mName = "SWEREF 99 17 15";
        mProjection = SWEREFProjection.sweref_99_17_15;
        mBoundsWgs84 = new MBounds(14.3500, 62.1000, 18.4500, 67.2000);
        mBoundsProjected = new MBounds(-1420.2800, 6888655.5779, 212669.1333, 7459585.3378);
    }
}
