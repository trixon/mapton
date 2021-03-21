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
package org.mapton.addon.wikipedia.result;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Patrik Karlström
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws MalformedURLException, IOException {
        String json = IOUtils.toString(new URL("https://sv.wikipedia.org/w/api.php?action=query&prop=coordinates%7Cpageimages%7Cpageterms&colimit=50&piprop=thumbnail&pithumbsize=144&pilimit=50&wbptterms=description&generator=geosearch&ggscoord=57.694580%7C12.120856&ggsradius=10000&ggslimit=50&format=json"), "utf-8");
        System.out.println(json);
        ApiResult result = ApiResult.load(json);
        System.out.println(result.toString());
        System.out.println(result.getBatchComplete());
        System.out.println(result.getQuery());
        final LinkedHashMap<String, Page> pages = result.getQuery().getPages();
        System.out.println(pages);
        for (String key : pages.keySet()) {
            System.out.println(key);
            System.out.println(pages.get(key).toString());

        }

    }

}
