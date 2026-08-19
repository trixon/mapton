/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_misc_user.reports;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.mapton.butterfly_format.types.BSystemUser;
import org.mapton.butterfly_misc_user.UserManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseUserReport extends BaseSystemReport {

    public BaseUserReport() {
    }

    public List<BSystemUser> getUsers() {
        return UserManager.getInstance().getTimeFilteredItems();
    }

    public List<BSystemUser> getUniqueUsers() {
        return getUsers().stream()
                .collect(Collectors.toMap(
                        BSystemUser::getExternalId,
                        user -> user,
                        (existing, replacement) -> existing
                ))
                .values()
                .stream()
                .sorted(Comparator.comparing(BSystemUser::getOperator)
                        .thenComparing(BSystemUser::getName))
                .toList();
    }

    @Override
    public String getParent() {
        return super.getParent() + "/Användare";
    }

}
