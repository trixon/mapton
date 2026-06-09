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
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum BListSortOrder {
    STANDARD(Order.ASC, Dict.DEFAULT.toString(),
            Comparator.comparing(BXyzPoint::getOrigin).thenComparing(Comparator.comparing(BXyzPoint::getName))),
    DELTA_1D(Order.DESC, "Δ1d",
            Comparator.comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta1();
                return (value == null) ? null : Math.abs(value);
            }, Comparator.nullsLast(Comparator.reverseOrder()))),
    DELTA_2D(Order.DESC, "Δ2d",
            Comparator.comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta2();
                return (value == null) ? null : Math.abs(value);
            }, Comparator.nullsLast(Comparator.reverseOrder()))),
    DELTA_3D(Order.DESC, "Δ3d",
            Comparator.comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta3();
                return (value == null) ? null : Math.abs(value);
            }, Comparator.nullsLast(Comparator.reverseOrder()))),
    DATE_PREV(Order.DESC, "Datum, senaste",
            Comparator.comparing(BXyzPoint::getDateLatest, Comparator.nullsLast(Comparator.naturalOrder())).reversed()),
    DATE_NEXT(Order.ASC, "Datum, nästa",
            Comparator.comparing((BXyzPoint p) -> p.extOrNull().getObservationRawNextDate(), Comparator.nullsLast(Comparator.naturalOrder()))),
    NORTH_SOUTH(Order.NONE, "Nord-Syd, Väst-Öst",
            Comparator.comparingDouble(BXyzPoint::getZeroY).reversed().thenComparing(Comparator.comparingDouble(BXyzPoint::getZeroX))),
    WEST_EAST(Order.NONE, "Väst-Öst, Nord-Syd",
            Comparator.comparingDouble(BXyzPoint::getZeroX).thenComparing(Comparator.comparingDouble(BXyzPoint::getZeroY).reversed()));
    private final Comparator mComparator;
    private final String mName;
    private final Order mOrder;

    private BListSortOrder(Order order, String name, Comparator comparator) {
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

    public enum Order {
        ASC, DESC, NONE;
    }
}
