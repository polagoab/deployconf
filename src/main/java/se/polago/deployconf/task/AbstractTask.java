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

package se.polago.deployconf.task;

import org.jdom2.Element;

/**
 * Common implementation of a deployment Task.
 */
public abstract class AbstractTask implements Task {

    private static final String PATH = "path";

    protected static final String DOM_ELEMENT_DESCRIPTION = "description";

    protected static final String DOM_ELEMENT_DEFAULT = "default";

    protected static final String DOM_ELEMENT_VALUE = "value";

    private String path;

    /**
     * Gets the path property value.
     *
     * @return the current value of the path property
     */
    @Override
    public String getPath() {
        return path;
    }

    /**
     * Sets the path property.
     *
     * @param path the new property value
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deserialize(Element root) {
        String attribute = root.getAttributeValue(PATH);
        if (attribute == null) {
            throw new IllegalStateException("path attribute is required");
        }
        setPath(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Element node) {
        node.setAttribute(PATH, getPath());
    }

    /**
     * Create a JDOM Element with the given name and possibly text.
     *
     * @param name the element name
     * @param text the element text or null
     * @return a JDOM Element instance
     */
    protected Element createJDOMElement(String name, String text) {
        Element result = new Element(name);
        if (text != null) {
            result.setText(text);
        }

        return result;
    }
}
