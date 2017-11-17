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

package org.polago.deployconf.task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Element;
import org.polago.deployconf.InteractiveConfigurer;

/**
 * Describes a deployment task.
 */
public interface Task {

    /**
     * Gets the Task Path.
     *
     * @return the Task path
     */
    String getPath();

    /**
     * Gets the serialized Task Name.
     *
     * @return the serialized name of this task
     */
    String getSerializedName();

    /**
     * Load the task from a list of JDOM Elements.
     *
     * @param node the JDOM Element to use for configuring this instance
     * @throws IOException indicating IO Error
     */
    void deserialize(Element node) throws IOException;

    /**
     * Merge another Task with this one.
     *
     * @param other the other Task
     */
    void merge(Task other);

    /**
     * Determine if the Task is completely configured.
     *
     * @return true if the Task is completely configured
     * @throws IOException indicating IO Error
     */
    boolean isConfigured() throws IOException;

    /**
     * Configure the task by asking the user for inputs.
     *
     * @param configurer the InteractiveConfigurer to use
     * @param force if true, the Task is always configured
     * @return true if the task was configured
     * @throws Exception indicating processing failure
     */
    boolean configureInteractively(InteractiveConfigurer configurer, boolean force) throws Exception;

    /**
     * Serialize this Task to a JDOM Element.
     *
     * @param node the JDOM Element to serialize to
     * @throws IOException indicating IO Error
     */
    void serialize(Element node) throws IOException;

    /**
     * Apply this Task by copying source to destination.
     *
     * @param source the Input stream
     * @param destination the Output stream
     * @throws Exception indicating processing failure
     */
    void apply(InputStream source, OutputStream destination) throws Exception;

}
