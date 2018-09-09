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
public class RT90 extends BaseRT {

    public RT90() {
        mName = "RT 90 0 gon";
        mProjection = RT90Position.RT90Projection.rt90_0_0_gon_v;
        mBoundsWgs84 = new MBounds(16.9400, 56.9000, 19.1900, 68.5800);
        mBoundsProjected = new MBounds(1431867.7653, 6308678.9677, 1568951.3087, 7610188.0479);
    }
}
