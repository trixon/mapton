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
public class RT90O50 extends BaseRT {

    public RT90O50() {
        mName = "RT 90 5 gon O";
        mProjection = RT90Position.RT90Projection.rt90_5_0_gon_o;
        mBoundsWgs84 = new MBounds(21.4400, 64.3300, 24.1800, 68.7200);
        mBoundsProjected = new MBounds(1445935.8347, 7136363.6827, 1578400.2193, 7626244.6023);
    }
}
