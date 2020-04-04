/*
 * Copyright 2019 Patrik Karlström.
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

import com.github.goober.coordinatetransformation.positions.RT90Position;
import org.openide.util.lookup.ServiceProvider;
import org.mapton.api.MBounds;
import org.mapton.api.MCooTrans;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCooTrans.class)
public class RT90V25 extends BaseRT {

    public RT90V25() {
        mName = "RT 90 2.5 gon V";
        mProjection = RT90Position.RT90Projection.rt90_2_5_gon_v;
        mBoundsWgs84 = new MBounds(14.0900, 56.0000, 16.9400, 68.0000);
        mBoundsProjected = new MBounds(1392811.0743, 6208496.7665, 1570600.8906, 7546077.6984);
    }
}
