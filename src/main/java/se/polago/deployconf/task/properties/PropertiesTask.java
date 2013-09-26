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

package se.polago.deployconf.task.properties;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;

import org.jdom2.Element;

import se.polago.deployconf.task.AbstractTask;

/**
 * Deployment Task for creating properties in a file.
 */
public class PropertiesTask extends AbstractTask {

    /**
     * Task element name in config file.
     */
    public static final String DOM_ELEMENT_TASK = "properties";

    private static final String DOM_ELEMENT_PROPERTY = "property";

    protected static final String DOM_ELEMENT_NAME = "name";

    // Properties files are always using ISO-8859-1
    private static final String ENCODING = "ISO-8859-1";

    private Set<Property> properties;

    /**
     * Public Constructor.
     */
    public PropertiesTask() {
        super();
        properties = new HashSet<Property>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Element node) {
        super.configure(node);
        for (Element e : node.getChildren()) {
            Property p =
                new Property(e.getChildText(DOM_ELEMENT_NAME),
                    e.getChildText(DOM_ELEMENT_DESCRIPTION),
                    e.getChildText(DOM_ELEMENT_DEFAULT),
                    e.getChildText(DOM_ELEMENT_VALUE));
            properties.add(p);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Element node) {
        super.serialize(node);
        for (Property p : properties) {
            Element e = createJDOMElement(DOM_ELEMENT_PROPERTY, null);
            e.addContent(createJDOMElement(DOM_ELEMENT_NAME, p.getName()));
            e.addContent(createJDOMElement(DOM_ELEMENT_DESCRIPTION,
                p.getDescription()));
            e.addContent(createJDOMElement(DOM_ELEMENT_DEFAULT,
                p.getDefaultValue()));
            e.addContent(createJDOMElement(DOM_ELEMENT_VALUE, p.getValue()));

            node.addContent(e);
        }
    }

    /**
     * Gets the properties property value.
     *
     * @return the current value of the properties property
     */
    public Set<Property> getProperties() {
        return properties;
    }

    /**
     * Sets the properties property.
     *
     * @param properties the new property value
     */
    void setProperties(Set<Property> properties) {
        this.properties = properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        for (Property p : properties) {
            if (p.getValue() == null || p.getValue().length() == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean configureInteractively() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializedName() {
        return DOM_ELEMENT_TASK;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void apply(InputStream source, OutputStream destination)
        throws Exception {

        OutputStreamWriter out = new OutputStreamWriter(destination, ENCODING);
        BufferedWriter writer = new BufferedWriter(out);

        for (Property p : getProperties()) {
            writer.newLine();
            String description = p.getDescription();
            if (description != null) {
                writer.append("#");
                writer.newLine();
                writer.append("# ");
                writer.append(description);
                writer.newLine();
                writer.append("#");
                writer.newLine();
            }
            writer.append(p.getName());
            writer.append("=");
            writer.append(p.getValue());
            writer.newLine();
        }
        writer.flush();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = getPath().hashCode();
        for (Property t : getProperties()) {
            result += t.hashCode();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof PropertiesTask) {
            PropertiesTask otherTask = (PropertiesTask) other;
            result =
                getPath().equals(otherTask.getPath())
                    && getProperties().equals(otherTask.getProperties());
        }

        return result;
    }

}
