/*
 * Copyright 2013 Google Inc. All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.appengine.tck.env.sdk;

import com.google.appengine.tck.event.AbstractExecutionLifecycle;
import com.google.appengine.tck.event.ExecutionLifecycleEvent;
import com.google.appengine.tck.event.TestLifecycle;
import org.kohsuke.MetaInfServices;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@MetaInfServices(TestLifecycle.class)
public class SdkExecutionLifecycle extends AbstractExecutionLifecycle {
    protected void doBefore(ExecutionLifecycleEvent event) {
        Boolean raw = getContextValueAsBoolean(event);
        if (raw != null) {
            log(event, raw);
            event.setExecute(raw);
        }
    }

    protected void doAfter(ExecutionLifecycleEvent event) {
    }
}
