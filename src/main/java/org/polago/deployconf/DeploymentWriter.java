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

package org.polago.deployconf;

import java.io.IOException;
import java.io.OutputStream;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;
import org.polago.deployconf.task.Task;

/**
 * Class that know how to persist a DeploymentConfig to an OutputStream.
 */
public class DeploymentWriter {

    protected static final String DOM_ROOT = "deployconf";

    protected static final String ATTR_NAME = "name";

    private final OutputStream outputStream;

    /**
     * Public Constructor.
     *
     * @param outputStream the OutputStream to use. The stream is not closed by this class.
     */
    public DeploymentWriter(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * Write the given DeploymentConfig to persistent storage.
     *
     * @param deploymentConfig the DeploymentConfig to persist
     * @throws IOException indicating IO problems
     */
    public void persist(DeploymentConfig deploymentConfig) throws IOException {
        Format format = Format.getPrettyFormat();
        format.setLineSeparator(LineSeparator.UNIX);
        format.setExpandEmptyElements(true);
        XMLOutputter outputter = new XMLOutputter(format);

        Element root = new Element(DOM_ROOT);
        String name = deploymentConfig.getName();
        if (name != null) {
            root.setAttribute(ATTR_NAME, name);
        }
        Document document = new Document();
        document.setRootElement(root);

        for (Task task : deploymentConfig.getTasks()) {
            Element node = new Element(task.getSerializedName());
            root.addContent(node);
            task.serialize(node);
        }

        outputter.output(document, outputStream);
    }

}
