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
public class SW992145 extends BaseSR {

    public SW992145() {
        mName = "SWEREF 99 21 45";
        mProjection = SWEREFProjection.sweref_99_21_45;
        mBoundsWgs84 = new MBounds(20.0000, 65.0000, 21.6500, 65.3800);
        mBoundsProjected = new MBounds(67451.0699, 7211342.8483, 145349.5699, 7254837.2540);
    }
}
