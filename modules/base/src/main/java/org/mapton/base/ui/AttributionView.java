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
package org.mapton.base.ui;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.controlsfx.control.PopOver;
import org.mapton.api.MAttribution;
import org.mapton.api.MDocumentInfo;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.NbBundle;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AttributionView extends BorderPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(AttributionView.class);
    private PopOver mPopOver;
    private WebView mWebView;

    public AttributionView() {
        createUI();
        initListeners();
    }

    public AttributionView(PopOver popOver) {
        mPopOver = popOver;
        createUI();
        initListeners();

        updateDocumentInfo(Mapton.getGlobalState().get(MKey.MAP_DOCUMENT_INFO));
    }

    private void createUI() {
        mWebView = new WebView();
        mWebView.setPrefSize(FxHelper.getUIScaled(500), FxHelper.getUIScaled(300));
        Mapton.applyHtmlCss(mWebView, "attribution.css");
        setCenter(mWebView);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                MDocumentInfo documentInfo = evt.getValue();
                updateDocumentInfo(documentInfo);
            });
        }, MKey.MAP_DOCUMENT_INFO);

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
                        if (mPopOver != null) {
                            mPopOver.hide();
                        }
                        SystemHelper.desktopBrowse(href);
                        evt.preventDefault();
                    }, false);
                }
            }
        });
    }

    private void updateDocumentInfo(MDocumentInfo documentInfo) {
        ContainerTag html;
        if (documentInfo != null) {
            LinkedHashMap<MAttribution, String> keys = new LinkedHashMap<>();

            LinkedHashMap<String, MAttribution> attributions = documentInfo.getAttributions();
            for (Map.Entry<String, MAttribution> entry : attributions.entrySet()) {
                keys.put(entry.getValue(), entry.getKey());
            }

            html = html(
                    head(
                            title(documentInfo.getName())
                    ),
                    body(
                            h1(documentInfo.getName()),
                            hr(),
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
            );
        } else {
            html = html();
        }

        //System.out.println(html.render());
        mWebView.getEngine().loadContent(html.render());
    }
}
