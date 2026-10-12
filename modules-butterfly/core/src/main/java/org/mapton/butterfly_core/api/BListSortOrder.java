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

import java.util.Comparator;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;
import static java.util.Comparator.reverseOrder;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BSortOrder;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public enum BListSortOrder {
    STANDARD(BSortOrder.ASC, "Ursprung och namn",
            comparing(BXyzPoint::getOrigin).thenComparing(comparing(BXyzPoint::getName))),
    NAME(BSortOrder.ASC, "Namn",
            comparing(BXyzPoint::getName)),
    DELTA_1D(BSortOrder.DESC, "Δ1d",
            comparing(p -> Math.abs(p.extOrNull().deltaZero().getDelta1()),
                    Butterfly.DOUBLE_NaN_LAST_DESC)),
    DELTA_2D(BSortOrder.DESC, "Δ2d",
            comparing(p -> Math.abs(p.extOrNull().deltaZero().getDelta2()),
                    Butterfly.DOUBLE_NaN_LAST_DESC)),
    DELTA_3D(BSortOrder.DESC, "Δ3d",
            comparing(p -> Math.abs(p.extOrNull().deltaZero().getDelta3()),
                    Butterfly.DOUBLE_NaN_LAST_DESC)),
    Z_ASC(BSortOrder.ASC, "Z",
            comparing((BXyzPoint p) -> {
                return p.getZeroZ();
            }, nullsLast(naturalOrder()))),
    Z_DESC(BSortOrder.DESC, "Z",
            comparing((BXyzPoint p) -> {
                return p.getZeroZ();
            }, nullsLast(reverseOrder()))),
    DATE_PREV_ASC(BSortOrder.ASC, "Datum, senaste",
            comparing(BXyzPoint::getDateLatest, nullsLast(naturalOrder()))),
    DATE_PREV_DESC(BSortOrder.DESC, "Datum, senaste",
            comparing(BXyzPoint::getDateLatest, nullsLast(reverseOrder()))),
    DATE_NEXT_ASC(BSortOrder.ASC, "Datum, nästa",
            comparing((BXyzPoint p) -> p.extOrNull().getObservationRawNextDate(), nullsLast(naturalOrder()))),
    DATE_NEXT_DESC(BSortOrder.DESC, "Datum, nästa",
            comparing((BXyzPoint p) -> p.extOrNull().getObservationRawNextDate(), nullsLast(reverseOrder()))),
    NORTH_SOUTH(BSortOrder.NONE, "Nord-Syd, Väst-Öst",
            comparing(BXyzPoint::getZeroY, nullsLast(reverseOrder()))
                    .thenComparing(BXyzPoint::getZeroX, nullsLast(naturalOrder()))),
    WEST_EAST(BSortOrder.NONE, "Väst-Öst, Nord-Syd",
            comparing(BXyzPoint::getZeroX, nullsLast(naturalOrder()))
                    .thenComparing(BXyzPoint::getZeroY, nullsLast(reverseOrder())));
    private final Comparator<BXyzPoint> mComparator;
    private final String mName;
    private final BSortOrder mOrder;

    private BListSortOrder(BSortOrder order, String name, Comparator<BXyzPoint> comparator) {
        mOrder = order;
        mName = name;
        mComparator = comparator;
    }

    public Comparator getComparator() {
        return mComparator;
    }

    @Override

    public String toString() {
        return switch (mOrder) {
            case ASC ->
                "▲ " + mName;
            case DESC ->
                "▼ " + mName;
            case NONE ->
                "● " + mName;
        };
    }

}
