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

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBannerManager;
import org.mapton.butterfly_format.types.BBaseControlPoint;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class WatchlistManager {

    private final MBannerManager mBannerManager = MBannerManager.getInstance();
    private final DelayedResetRunner mDelayedResetRunner;
    private final Set<String> mManagerNames = new ConcurrentSkipListSet<>();

    public static WatchlistManager getInstance() {
        return WatchlistManagerHolder.INSTANCE;
    }

    private WatchlistManager() {
        mDelayedResetRunner = new DelayedResetRunner(10_000, () -> {
            mBannerManager.setMessage("Det finns nya mätvärden på bevakningspunkter i: %s".formatted(String.join(", ", mManagerNames)));
            mManagerNames.clear();
        });
    }

    public void check(BaseManager<? extends BBaseControlPoint> manager) {
        var nameToPoinMap = manager.getAllItemsMap();
        var preferences = NbPreferences.forModule(manager.getClass()).node("watchlist");
        var pointNode = preferences.node("points");
        var definition = preferences.node("definition").get("content", "");
        int changedPoints = 0;

        manager.getAllItems().forEach(p -> p.setValue("watchlistMember", Boolean.FALSE));

        for (var line : StringUtils.split(definition, "\n")) {
            //TODO Parse polygon
            var p = nameToPoinMap.get(line.trim());
            if (p != null) {
                p.setValue("watchlistMember", Boolean.TRUE);
                if (p.getDateLatest() != null) {
                    var name = p.getName();
                    p.setValue("watchlistChanged", Boolean.FALSE);
                    var latest = getLocalDateTimeAsLong(p.getDateLatest());
                    var stored = pointNode.getLong(name, -1);
                    if (stored != latest) {
                        pointNode.putLong(name, latest);
                        if (stored > 0) {
                            p.setValue("watchlistChanged", Boolean.TRUE);
                            changedPoints++;
                        }
                    }
                }
            }
        }

        if (changedPoints > 0) {
            mManagerNames.add(manager.getName());
            mDelayedResetRunner.reset();
        }
    }

    private long getLocalDateTimeAsLong(LocalDateTime ldt) {
        return (ldt.getYear() * 10000000000L)
                + (ldt.getMonthValue() * 100000000L)
                + (ldt.getDayOfMonth() * 1000000L)
                + (ldt.getHour() * 10000)
                + (ldt.getMinute() * 100)
                + ldt.getSecond();
    }

    private static class WatchlistManagerHolder {

        private static final WatchlistManager INSTANCE = new WatchlistManager();
    }
}
