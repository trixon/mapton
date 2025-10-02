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
package org.mapton.addon.xkcd;

import java.io.IOException;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.mapton.api.MBackgroundImage;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class XkcdManager {

    private static final String BASE_URL = "https://xkcd.com/";
    private Notifier mDisplayer;
    private int mIndex;
    private String mUrl;

    public static void main(String[] args) throws IOException {
        Xkcd x = new XkcdManager().parse();
        System.out.println(ToStringBuilder.reflectionToString(x, ToStringStyle.MULTI_LINE_STYLE));
    }

    public void setDisplayer(Notifier displayer) {
        mDisplayer = displayer;
    }

    void go(Xkcd xkcd) {
        Mapton.getGlobalState().put(MKey.BACKGROUND_IMAGE, new MBackgroundImage(xkcd.getSrc(), .85));
        mDisplayer.onShow(xkcd);
    }

    void goFirst() {
        new Thread(() -> {
            try {
                go(parse(mIndex = 1));
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }

    void goLast() {
        new Thread(() -> {
            try {
                go(parse());
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }

    void goNext() {
        new Thread(() -> {
            try {
                go(parse(++mIndex));
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }

    void goPrevious() {
        new Thread(() -> {
            try {
                go(parse(--mIndex));
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }

    void goRandom() {
        new Thread(() -> {
            try {
                go(parse("https://c.xkcd.com/random/comic/"));
            } catch (IOException ex) {
                //Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }

    private Xkcd parse(int number) throws IOException {
        return parse("%s%d".formatted(BASE_URL, number));
    }

    private Xkcd parse(String url) throws IOException {
        Document doc = Jsoup.connect(url).get();
        //System.out.println(doc.toString());

        Elements metaTags = doc.getElementsByTag("meta");
        for (Element metaTag : metaTags) {
            if (Strings.CI.equals(metaTag.attr("property"), "og:url")) {
                mUrl = metaTag.attr("content");
                mIndex = Integer.parseInt(mUrl.replaceAll("\\D+", ""));
            }
        }

        Element comic = doc.getElementById("comic");
        Element imageElement = comic.select("img").first();

        Xkcd xkcd = new Xkcd();
        xkcd.setAlt(imageElement.attr("alt"));
        xkcd.setFootnote(doc.getElementById("footnote").text());
        xkcd.setLicense(doc.getElementById("licenseText").text());
        xkcd.setSrc(imageElement.absUrl("src"));
        xkcd.setTitle(imageElement.attr("title"));
        xkcd.setUrl(mUrl);

        return xkcd;
    }

    private Xkcd parse() throws IOException {
        return parse(BASE_URL);
    }

    public interface Notifier {

        void onShow(Xkcd xkcd);
    }
}
