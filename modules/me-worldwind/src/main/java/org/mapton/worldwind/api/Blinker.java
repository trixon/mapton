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
package org.mapton.worldwind.api;

import javax.swing.Timer;

/**
 *
 * @author Patrik Karlström
 */
public class Blinker {

    private RoundAnnotation annotation;
    private int delay = 100;
    private double initialOpacity;
    private double initialScale;
    private int step = 0;
    private int steps = 10;
    private Timer timer;

    public Blinker(RoundAnnotation ea) {
        this.annotation = ea;
        this.initialScale = this.annotation.getAttributes().getScale() * .5;
        this.initialOpacity = this.annotation.getAttributes().getOpacity();
        this.timer = new Timer(delay, actionEvent -> {
            annotation.getAttributes().setScale(initialScale * (1f + 7f * ((float) step / (float) steps)));
            annotation.getAttributes().setOpacity(initialOpacity * (1f - ((float) step / (float) steps)));
            step = step == steps ? 0 : step + 1;
        });
        start();
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
        step = 0;
        this.annotation.getAttributes().setScale(initialScale);
        this.annotation.getAttributes().setOpacity(initialOpacity);
    }

}
