/**
 * Copyright (c) 2013 Polago AB
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

package se.polago.deployconf;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.polago.deployconf.task.Task;
import se.polago.deployconf.task.filter.FilterTask;
import se.polago.deployconf.task.properties.PropertiesTask;

/**
 * Class that know how to read a DeploymentConfig from an InputStream.
 */
public class DeploymentReader {

    @SuppressWarnings("unused")
    private static Logger logger = LoggerFactory
        .getLogger(DeploymentReader.class);

    private static final String ATTR_NAME = "name";

    private final InputStream inputStream;

    private final Map<String, Class<? extends Task>> handlerMapping;

    /**
     * Public Constructor.
     *
     * @param inputStream the stream to read from. The stream is not closed by
     * this class.
     */
    public DeploymentReader(InputStream inputStream) {
        if (inputStream == null) {
            throw new IllegalArgumentException("inputStream is null");
        }
        this.inputStream = inputStream;

        handlerMapping = new HashMap<String, Class<? extends Task>>();
        handlerMapping.put(PropertiesTask.DOM_ELEMENT_TASK,
            PropertiesTask.class);
        handlerMapping.put(FilterTask.DOM_ELEMENT_TASK, FilterTask.class);
    }

    /**
     * Parse the input stream into a DeploymentConfig instance.
     *
     * @return a DeploymentConfig
     * @throws Exception indicating failure
     */
    public DeploymentConfig parse() throws Exception {
        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(inputStream);
        String name = d.getRootElement().getAttributeValue(ATTR_NAME);
        List<Element> tasks = d.getRootElement().getChildren();
        DeploymentConfig result = new DeploymentConfig();

        if (name != null) {
            result.setName(name);
        }

        for (Element e : tasks) {
            Task t = getTaskFromElement(e);
            result.addTask(t);
        }
        return result;
    }

    /**
     * Create a Task from an Element.
     *
     * @param e the element to use
     * @return a Task instance
     * @throws Exception indicating failure
     */
    private Task getTaskFromElement(Element e) throws Exception {
        Class<? extends Task> cls = handlerMapping.get(e.getName());
        if (cls == null) {
            throw new IllegalStateException(
                "No Task Handler found for element: " + e.getName());
        }
        Task t = cls.newInstance();
        t.configure(e);

        return t;
    }
}
