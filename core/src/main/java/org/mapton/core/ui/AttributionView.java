/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.core.ui;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.controlsfx.control.PopOver;
import org.mapton.api.MAttribution;
import org.mapton.api.MDocumentInfo;
import org.openide.util.NbBundle;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AttributionView extends BorderPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(AttributionView.class);
    private PopOver mPopOver;
    private WebView mWebView;

    public AttributionView(PopOver popOver) {
        mPopOver = popOver;
        createUI();
        initListeners();
    }

    public void updateDocumentInfo(MDocumentInfo documentInfo) {
        LinkedHashMap<MAttribution, String> keys = new LinkedHashMap<>();

        LinkedHashMap<String, MAttribution> attributions = documentInfo.getAttributions();
        for (Map.Entry<String, MAttribution> entry : attributions.entrySet()) {
            keys.put(entry.getValue(), entry.getKey());
        }

        ContainerTag html = html(
                head(
                        title(documentInfo.getName())
                ),
                body(
                        h1(documentInfo.getName()),
                        hr(),
                        div(
                                each(attributions.values(), attribution
                                        -> div(
                                        h2(keys.get(attribution)),
                                        p(iff(!attribution.isOnlyRaw(), rawHtml(String.format(mBundle.getString("attribution"),
                                                attribution.getProviderUrl(),
                                                attribution.getProviderName(),
                                                attribution.getLicenseUrl(),
                                                attribution.getLicenseName()
                                        )))),
                                        p(rawHtml(attribution.getRawHtml())),
                                        hr())
                                )
                        )
                )
        );

        //System.out.println(html.render());
        mWebView.getEngine().loadContent(html.render());
    }

    private void createUI() {
        mWebView = new WebView();
        //mWebView.setFontScale(1.0);
        setCenter(mWebView);
    }

    private void initListeners() {
        mWebView.getEngine().getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) -> {
            if (t1 == Worker.State.SUCCEEDED) {
                org.w3c.dom.NodeList nodeList = mWebView.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < nodeList.getLength(); i++) {
                    org.w3c.dom.Node node = nodeList.item(i);
                    org.w3c.dom.events.EventTarget eventTarget = (org.w3c.dom.events.EventTarget) node;
                    eventTarget.addEventListener("click", (org.w3c.dom.events.Event evt) -> {
                        org.w3c.dom.events.EventTarget target = evt.getCurrentTarget();
                        org.w3c.dom.html.HTMLAnchorElement anchorElement = (org.w3c.dom.html.HTMLAnchorElement) target;
                        String href = anchorElement.getHref();
                        mPopOver.hide();
                        SystemHelper.desktopBrowse(href);
                        evt.preventDefault();
                    }, false);
                }
            }
        });
    }
}
