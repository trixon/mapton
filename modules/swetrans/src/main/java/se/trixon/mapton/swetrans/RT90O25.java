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

import com.github.goober.coordinatetransformation.positions.RT90Position;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.api.MBounds;
import se.trixon.mapton.api.MCooTrans;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCooTrans.class)
public class RT90O25 extends BaseRT {

    public RT90O25() {
        mName = "RT 90 2.5 gon O";
        mProjection = RT90Position.RT90Projection.rt90_2_5_gon_o;
        mBoundsWgs84 = new MBounds(19.1900, 63.3000, 21.4400, 69.1000);
        mBoundsProjected = new MBounds(1443925.0767, 7021572.1361, 1556749.0296, 7668177.0436);
    }
}
