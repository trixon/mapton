/*
 * Copyright (C) 2021 Peab AB - All Rights Reserved.
 *
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Patrik Karlstrom <patrik.karlstrom@peab.se>, May 2018
 *
 */
package org.mapton.api.ui;

import javafx.beans.value.ObservableValue;
import javafx.concurrent.Worker;
import javafx.scene.web.WebView;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MInfoPopOver extends MPopOver {

    private WebView mWebView;

    public MInfoPopOver() {
        createUI();
        initListeners();

        setTitle(Dict.INFORMATION.toString());
        getAction().setGraphic(MaterialIcon._Action.INFO_OUTLINE.getImageView(getIconSizeToolBarInt()));
    }

    public void load(String url) {
        mWebView.getEngine().load(url);
    }

    public void loadContent(String content) {
        mWebView.getEngine().loadContent(content);
    }

    private void createUI() {
        mWebView = new WebView();
        mWebView.setPrefSize(FxHelper.getUIScaled(500), FxHelper.getUIScaled(300));
        Mapton.applyHtmlCss(mWebView, "info.css");
        setContentNode(mWebView);
    }

    private void initListeners() {
        mWebView.getEngine().getLoadWorker().stateProperty().addListener((ObservableValue<? extends Worker.State> ov, Worker.State t, Worker.State t1) -> {
            if (t1 == Worker.State.SUCCEEDED) {
                var domNodeList = mWebView.getEngine().getDocument().getElementsByTagName("a");
                for (int i = 0; i < domNodeList.getLength(); i++) {
                    var domNode = domNodeList.item(i);
                    var domEventTarget = (org.w3c.dom.events.EventTarget) domNode;
                    domEventTarget.addEventListener("click", domEvent -> {
                        var domCurrentTarget = domEvent.getCurrentTarget();
                        var domHtmlAnchorElement = (org.w3c.dom.html.HTMLAnchorElement) domCurrentTarget;
                        String href = domHtmlAnchorElement.getHref();
                        hide();
                        SystemHelper.desktopBrowse(href);
                        domEvent.preventDefault();
                    }, false);
                }
            }
        });
    }
}
