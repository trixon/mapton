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
package org.mapton.api.report;

import j2html.tags.ContainerTag;
import java.util.concurrent.Callable;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MReport extends MSplitNavMaskerPane {

    private WebView mWebView;

    public MReport() {
        setParent(String.format("- %s -", Dict.SYSTEM.toString()));
    }

    public ContainerTag getContent() {
        return null;
    }

    @Override
    public Node getNode() {
        if (mNotificationPane.getContent() == null) {
            mNotificationPane.setContent(getWebView());
        }

        return mBody;
    }

    public WebView getWebView() {
        if (mWebView == null) {
            mWebView = new WebView();
            Mapton.applyHtmlCss(mWebView, "report.css");
        }

        return mWebView;
    }

    @Override
    public void onSelect() {
        update(() -> {
            return getContent();
        });
    }

    public void update(Callable<ContainerTag> callable) {
        if (callable == null) {
            return;
        }

        getWebView().getEngine().loadContent("");
        mMaskerPane.setVisible(true);

        new Thread(() -> {
            try {
                ContainerTag containerTag = callable.call();
                Platform.runLater(() -> {
                    try {
                        getWebView().getEngine().loadContent(containerTag.render());
                    } catch (NullPointerException e) {
                        //
                    }
                    mMaskerPane.setVisible(false);
                });
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }, getClass().getCanonicalName()).start();
    }
}
