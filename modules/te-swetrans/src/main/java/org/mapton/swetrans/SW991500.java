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
public class SW991500 extends BaseSR {

    public SW991500() {
        mName = "SWEREF 99 15 00";
        mProjection = SWEREFProjection.sweref_99_15_00;
        mBoundsWgs84 = new MBounds(13.5000, 55.9500, 16.1000, 61.6200);
        mBoundsProjected = new MBounds(56294.0365, 6203542.5282, 218719.0581, 6835499.2391);
    }
}
