/**
 * Copyright (c) 2013-2017 Polago AB
 * All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.polago.deployconf;

import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Element;
import org.polago.deployconf.group.ConfigGroupManager;
import org.polago.deployconf.task.Task;

/**
 * Stub implementation of Task used for testing.
 */
class TestTask implements Task {

    boolean configured;

    boolean interactive;

    String path = "test-path";

    boolean applied;

    boolean serialized;

    String serializedName = "test-task";

    boolean isconfigureInteractivelyCalled;

    boolean merged;

    ConfigGroupManager groupManager;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public void deserialize(Element root, ConfigGroupManager groupManager) {
        configured = true;
    }

    @Override
    public void merge(Task other) {
        merged = true;
    }

    @Override
    public boolean isConfigured() {
        return configured;
    }

    @Override
    public boolean configureInteractively(InteractiveConfigurer configurer, boolean force,
        ConfigGroupManager groupManager) {
        isconfigureInteractivelyCalled = true;
        return interactive;
    }

    @Override
    public void serialize(Element node, ConfigGroupManager groupManager) {
        serialized = true;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }

    @Override
    public void apply(InputStream source, OutputStream destination, ConfigGroupManager groupManager) {
        applied = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = getPath().hashCode();
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof TestTask) {
            TestTask otherTask = (TestTask) other;
            result = getPath().equals(otherTask.getPath());
        }

        return result;
    }

};
