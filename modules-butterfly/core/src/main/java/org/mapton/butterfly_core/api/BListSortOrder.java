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
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public enum BListSortOrder {
    STANDARD(Order.ASC, "Ursprung och namn",
            comparing(BXyzPoint::getOrigin).thenComparing(comparing(BXyzPoint::getName))),
    NAME(Order.ASC, "Namn",
            comparing(BXyzPoint::getName)),
    DELTA_1D(Order.DESC, "Δ1d",
            comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta1();
                return (value == null) ? null : Math.abs(value);
            }, nullsLast(reverseOrder()))),
    DELTA_2D(Order.DESC, "Δ2d",
            comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta2();
                return (value == null) ? null : Math.abs(value);
            }, nullsLast(reverseOrder()))),
    DELTA_3D(Order.DESC, "Δ3d",
            comparing((BXyzPoint p) -> {
                var value = p.extOrNull().deltaZero().getDelta3();
                return (value == null) ? null : Math.abs(value);
            }, nullsLast(reverseOrder()))),
    Z_ASC(Order.ASC, "Z",
            comparing((BXyzPoint p) -> {
                return p.getZeroZ();
            }, nullsLast(naturalOrder()))),
    Z_DESC(Order.DESC, "Z",
            comparing((BXyzPoint p) -> {
                return p.getZeroZ();
            }, nullsLast(reverseOrder()))),
    DATE_PREV_ASC(Order.ASC, "Datum, senaste",
            comparing(BXyzPoint::getDateLatest, nullsLast(naturalOrder()))),
    DATE_PREV_DESC(Order.DESC, "Datum, senaste",
            comparing(BXyzPoint::getDateLatest, nullsLast(reverseOrder()))),
    DATE_NEXT_ASC(Order.ASC, "Datum, nästa",
            comparing((BXyzPoint p) -> p.extOrNull().getObservationRawNextDate(), nullsLast(naturalOrder()))),
    DATE_NEXT_DESC(Order.DESC, "Datum, nästa",
            comparing((BXyzPoint p) -> p.extOrNull().getObservationRawNextDate(), nullsLast(reverseOrder()))),
    NORTH_SOUTH(Order.NONE, "Nord-Syd, Väst-Öst",
            comparing(BXyzPoint::getZeroY, nullsLast(reverseOrder()))
                    .thenComparing(BXyzPoint::getZeroX, nullsLast(naturalOrder()))),
    WEST_EAST(Order.NONE, "Väst-Öst, Nord-Syd",
            comparing(BXyzPoint::getZeroX, nullsLast(naturalOrder()))
                    .thenComparing(BXyzPoint::getZeroY, nullsLast(reverseOrder())));
    private final Comparator<BXyzPoint> mComparator;
    private final String mName;
    private final Order mOrder;

    private BListSortOrder(Order order, String name, Comparator<BXyzPoint> comparator) {
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
