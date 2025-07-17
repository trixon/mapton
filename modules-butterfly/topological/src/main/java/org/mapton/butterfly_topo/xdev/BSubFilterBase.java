/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_topo.xdev;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BSubFilterBase<T extends BXyzPoint> {

    public abstract void clear();

    public abstract void createInfoContent(LinkedHashMap<String, String> map);

    public abstract boolean filter(T p);

    public abstract void initListeners(ChangeListener changeListener, ListChangeListener listChangeListener);

    public abstract void initSession(SessionManager sessionManager);

    public abstract void load(ArrayList<T> items);

    public abstract void onShownFirstTime();

}
