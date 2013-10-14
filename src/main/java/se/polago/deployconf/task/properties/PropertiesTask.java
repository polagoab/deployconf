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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.polago.deployconf.InteractiveConfigurer;
import se.polago.deployconf.task.AbstractTask;

/**
 * Deployment Task for creating properties in a file.
 */
public class PropertiesTask extends AbstractTask {

    private static Logger logger = LoggerFactory
        .getLogger(PropertiesTask.class);

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
        properties = new LinkedHashSet<Property>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deserialize(Element node) {
        super.deserialize(node);
        for (Element e : node.getChildren()) {
            String name = e.getChildText(DOM_ELEMENT_NAME);
            if (name == null) {
                throw new IllegalStateException(
                    "Property name element does not exists");
            }
            String description = e.getChildText(DOM_ELEMENT_DESCRIPTION);
            if (description == null) {
                throw new IllegalStateException(
                    "Property description element does not exists");
            }
            String defaultValue = e.getChildText(DOM_ELEMENT_DEFAULT);
            String value = e.getChildText(DOM_ELEMENT_VALUE);

            Property p = new Property(name, description, defaultValue, value);
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
    public boolean configureInteractively(InteractiveConfigurer configurer,
        boolean force) throws Exception {

        boolean result = true;

        for (Property p : properties) {
            if (force || p.getValue() == null || p.getValue().length() == 0) {
                result = configurePropertyInteractively(p, configurer);
                if (result == false) {
                    return result;
                }
            }
        }

        return result;
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

    /**
     * Configure a Property by asking the user.
     *
     * @param p the Property to configure
     * @param configurer the InteractiveConfigurer to use
     * @return true if the property was configured
     * @throws IOException indicating IO failure
     */
    private boolean configurePropertyInteractively(Property p,
        InteractiveConfigurer configurer) throws IOException {

        boolean result = false;

        logger.debug("Configure interactively: {}", p.getName());

        String defaultValue = p.getValue();
        if (defaultValue == null || defaultValue.length() == 0) {
            defaultValue = p.getDefaultValue();
        }

        String value =
            configurer
                .configure(p.getName(), p.getDescription(), defaultValue);

        logger.debug("Configure interactively result for '{}': {}",
            p.getName(), value);

        if (value != null) {
            p.setValue(value);
            result = true;
        }

        return result;
    }

}
