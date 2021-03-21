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
public class SW991800 extends BaseSR {

    public SW991800() {
        mName = "SWEREF 99 18 00";
        mProjection = SWEREFProjection.sweref_99_18_00;
        mBoundsWgs84 = new MBounds(17.0800, 58.7000, 19.2100, 60.6500);
        mBoundsProjected = new MBounds(96664.5565, 6509617.2232, 220146.6914, 6727103.5879);
    }
}
