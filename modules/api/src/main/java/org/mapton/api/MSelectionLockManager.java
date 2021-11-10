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
package org.mapton.api;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MSelectionLockManager {

    public static long DEFAULT_TIMEOUT = TimeUnit.SECONDS.toMillis(5);
    private static final Logger LOGGER = Logger.getLogger(MSelectionLockManager.class.getName());

    private final Set<Object> mLocks = Collections.synchronizedSet(new HashSet<>());
    private final HashSet<MObjectSelectionLockListener> mObjectSelectionLockListeners = new HashSet<>();

    public static MSelectionLockManager getInstance() {
        return Holder.INSTANCE;
    }

    private MSelectionLockManager() {
//        addListener(new MObjectSelectionLockListener() {
//            @Override
//            public void onLocked() {
//                LOGGER.info("->LOCKED");
//            }
//
//            @Override
//            public void onUnlocked() {
//                LOGGER.info("->UNLOCKED");
//            }
//        });
    }

    public boolean addListener(MObjectSelectionLockListener objectSelectionLockListener) {
        return mObjectSelectionLockListeners.add(objectSelectionLockListener);
    }

    public void addLock(Object lockObject) {
        addLock(lockObject, DEFAULT_TIMEOUT);
    }

    public void addLock(Object lockObject, long timeout) {
        var wasUnlocked = mLocks.isEmpty();
        if (mLocks.contains(lockObject)) {
            return;
        } else {
            mLocks.add(lockObject);
        }

        if (wasUnlocked) {
            mObjectSelectionLockListeners.forEach(objectSelectionLockListener -> {
                try {
                    objectSelectionLockListener.onLocked();
                } catch (Exception e) {
                }
            });
        }

        if (timeout > 0) {
            new Thread(() -> {
                try {
                    Thread.sleep(DEFAULT_TIMEOUT);
                    if (mLocks.contains(lockObject)) {
                        //LOGGER.log(Level.INFO, "Lock {0} is older than {1}ms, unlocking anyway", new Object[]{lockObject, DEFAULT_TIMEOUT});
                        removeLock(lockObject);
                    }
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                    Thread.currentThread().interrupt();
                }
            }).start();
        }
    }

    public boolean isLocked() {
        return !mLocks.isEmpty();
    }

    public void printLocks() {
        StringBuilder sb = new StringBuilder();
        mLocks.forEach(lock -> {
            sb.append(lock).append("\n");
        });

        LOGGER.info(sb.toString());
    }

    public boolean removeListener(MObjectSelectionLockListener objectSelectionLockListener) {
        return mObjectSelectionLockListeners.remove(objectSelectionLockListener);
    }

    public void removeLock(Object lockObject) {
        var wasLocked = !mLocks.isEmpty();
        if (!mLocks.contains(lockObject)) {
            return;
        } else {
            mLocks.remove(lockObject);
        }

        if (wasLocked && mLocks.isEmpty()) {
            mObjectSelectionLockListeners.forEach(objectSelectionLockListener -> {
                try {
                    objectSelectionLockListener.onUnlocked();
                } catch (Exception e) {
                }
            });
        }
    }

    public void removeLock(Object lockObject, long delay) {
        new Thread(() -> {
            try {
                Thread.sleep(delay);
                removeLock(lockObject);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private static class Holder {

        private static final MSelectionLockManager INSTANCE = new MSelectionLockManager();
    }

    public interface MObjectSelectionLockListener {

        void onLocked();

        void onUnlocked();
    }
}
