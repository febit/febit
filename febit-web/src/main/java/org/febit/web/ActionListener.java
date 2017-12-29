/**
 * Copyright 2013-present febit.org (support@febit.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.febit.web;

import org.febit.Listener;
import org.febit.util.Priority;
import org.febit.web.component.ActionManager;

/**
 *
 * @author zqq90
 */
@Priority.Level(Priority.PRI_LOWER)
public class ActionListener implements Listener {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ActionListener.class);

    protected ActionManager actionManager;

    @Override
    public void start() {
        this.actionManager.scanActions();
        LOG.info("> Find Actions: " + this.actionManager.getActionCount());
    }

    @Override
    public void stop() {
    }
}
