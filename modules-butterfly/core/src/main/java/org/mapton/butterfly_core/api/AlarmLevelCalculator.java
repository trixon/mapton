/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_core.api;

import java.util.function.Function;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class AlarmLevelCalculator {

    private final Function<BXyzPoint, Integer> mFunction;
    private final Function<BXyzPoint, Integer> mFunction1;
    private final Function<BXyzPoint, Integer> mFunction2;

    public AlarmLevelCalculator(Function<BXyzPoint, Integer> function, Function<BXyzPoint, Integer> function1, Function<BXyzPoint, Integer> function2) {
        mFunction = function;
        mFunction1 = function1;
        mFunction2 = function2;
    }

    public int getLevel(BXyzPoint p) {
        return mFunction.apply(p);
    }

    public int getLevel1(BXyzPoint p) {
        return mFunction1.apply(p);
    }

    public int getLevel2(BXyzPoint p) {
        return mFunction2.apply(p);
    }
}
