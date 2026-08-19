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
package org.mapton.butterfly_misc_user;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.CryptoHelper;
import org.mapton.butterfly_format.types.BSystemUser;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
public class UserManager extends BaseManager<BSystemUser> {

    private final static String DISRUPTOR_NAME = Bundle.CTL_UserAction();
    private final UserLayerOptions mLayerOptions = UserLayerOptions.getInstance();
    private final UserPropertiesBuilder mPropertiesBuilder = new UserPropertiesBuilder();

    public static UserManager getInstance() {
        return Holder.INSTANCE;
    }

    private UserManager() {
        super(BSystemUser.class);
    }

    @Override
    public List<String> getObjectAnnotation(BSystemUser p) {
        if (mLayerOptions.isPlotAnnotation()) {
            var ext = p.extOrNull();
            return List.of(
                    p.getName(),
                    ext.getDateLatest() != null ? ext.getDateLatest().toLocalDate().toString() : "-"
            );
        } else {
            return null;
        }
    }

    @Override
    public Object getObjectProperties(BSystemUser selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            for (var user : butterfly.sys().getUsers()) {
                user.setName(decrypt(user.getName()));
                user.setGroup(decrypt(user.getGroup()));
                user.setOperator(decrypt(user.getOperator()));
                user.setOrigin(decrypt(user.getOrigin()));
                user.setEmail(decrypt(user.getEmail()));
                user.setInitials(decrypt(user.getInitials()));
                user.setCategory(decrypt(user.getCategory()));
                user.extOrNull().setDateFirst(user.getDateCreated());
                user.extOrNull().setDateLatest(user.getDateLatest());
            }

            initAllItems(butterfly.sys().getUsers());
            var items = getAllItems();
            var dates = new TreeSet<>(items.stream()
                    .map(p -> p.getDateLatest())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }

            for (var item : items) {
//                item.ext().setDateLatest(item.getDateLatest());
//                item.ext().setDateFirst(item.getDateLatest());
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    private String decrypt(String s) {
        try {
            return CryptoHelper.decrypt(s, getKey());
        } catch (Exception ex) {
            return "KRYPTERAD";
        }
    }

    private String getKey() {
        return MSimpleObjectStorageManager.getInstance().getString(UserApiKeyProvider.class, null);
    }

    @Override
    protected void applyTemporalFilter() {
        var timeFilteredItems = getFilteredItems().stream()
                .filter(p -> p.getDateLatest() == null ? true : getTemporalManager().isValid(p.getDateLatest()))
                .toList();

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BSystemUser> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class UserDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final UserManager INSTANCE = new UserManager();
    }
}
