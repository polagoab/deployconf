/**
 * Copyright (c) 2013-2015 Polago AB
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

package org.polago.deployconf.task.properties;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedHashSet;
import java.util.Set;

import org.jdom2.Element;
import org.polago.deployconf.InteractiveConfigurer;
import org.polago.deployconf.group.ConfigGroupManager;
import org.polago.deployconf.task.AbstractTask;
import org.polago.deployconf.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Deployment Task for creating properties in a file.
 */
public class PropertiesTask extends AbstractTask {

    private static Logger logger = LoggerFactory.getLogger(PropertiesTask.class);

    /**
     * Task element name in config file.
     */
    public static final String DOM_ELEMENT_TASK = "properties";

    private static final String DOM_ELEMENT_PROPERTY = "property";

    protected static final String DOM_ELEMENT_NAME = "name";

    // Properties files are always using ISO-8859-1
    private static final String ENCODING = "ISO-8859-1";

    private static final String PATH_IGNORE = PropertiesTask.class.getCanonicalName() + "_NO_PATH";;

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
    public void deserialize(Element node, ConfigGroupManager groupManager) throws IOException {
        String attribute = node.getAttributeValue(DOM_ATTRIBUTE_PATH);
        if (attribute == null) {
            attribute = PATH_IGNORE;
        }
        setPath(attribute);

        for (Element e : node.getChildren()) {
            String name = e.getChildTextTrim(DOM_ELEMENT_NAME);
            if (name.length() == 0) {
                throw new IllegalStateException("Property name element does not exists");
            }
            String description = e.getChildTextTrim(DOM_ELEMENT_DESCRIPTION);
            if (description.length() == 0) {
                throw new IllegalStateException("Property description element does not exists");
            }
            String defaultValue = e.getChildTextTrim(DOM_ELEMENT_DEFAULT);

            String group = e.getAttributeValue(DOM_ATTRIBUTE_GROUP);
            String value = null;

            if (group != null) {
                value = groupManager.lookupGroup(group).getProperty(name);
            }

            if (value == null) {
                value = e.getChildTextTrim(DOM_ELEMENT_VALUE);
            }

            Property p = new Property(name, description, defaultValue, value);

            if (group != null) {
                p.setGroup(group);
            }

            properties.add(p);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Element node, ConfigGroupManager groupManager) throws IOException {
        if (!PATH_IGNORE.equals(getPath())) {
            node.setAttribute(DOM_ATTRIBUTE_PATH, getPath());
        }

        for (Property p : properties) {
            Element e = createJDOMElement(DOM_ELEMENT_PROPERTY);
            e.addContent(createJDOMTextElement(DOM_ELEMENT_NAME, p.getName()));
            e.addContent(createJDOMCDATAElement(DOM_ELEMENT_DESCRIPTION, p.getDescription()));
            e.addContent(createJDOMTextElement(DOM_ELEMENT_DEFAULT, p.getDefaultValue()));

            String group = p.getGroup();
            if (group != null) {
                groupManager.lookupGroup(group).setProperty(p.getName(), p.getValue());
                e.setAttribute(DOM_ATTRIBUTE_GROUP, group);
            } else {
                e.addContent(createJDOMTextElement(DOM_ELEMENT_VALUE, p.getValue()));
            }

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
    public void merge(Task other) {
        if (other instanceof PropertiesTask) {
            PropertiesTask opt = (PropertiesTask) other;
            properties.retainAll(opt.getProperties());

            for (Property op : opt.getProperties()) {
                boolean exists = false;
                for (Property p : properties) {
                    if (p.equals(op)) {
                        exists = true;
                        p.setDescription(op.getDescription());
                        p.setDefaultValue(op.getDefaultValue());
                        p.setGroup(op.getGroup());
                        break;
                    }
                }
                if (!exists) {
                    properties.add(op);
                }
            }
        }
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
    public boolean configureInteractively(InteractiveConfigurer configurer, boolean force) throws Exception {

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
    public void apply(InputStream source, OutputStream destination, ConfigGroupManager groupManager) throws Exception {

        if (PATH_IGNORE.equals(getPath())) {
            // This task should never be applied
            return;
        }

        OutputStreamWriter out = new OutputStreamWriter(destination, ENCODING);
        BufferedWriter writer = new BufferedWriter(out);

        for (Property p : getProperties()) {
            writer.newLine();
            String description = p.getDescription();
            if (description != null) {
                writer.append("#");
                writer.newLine();
                String[] lines = description.split("[\\r\\n]+");
                for (String line : lines) {
                    writer.append("# ");
                    writer.append(line.trim());
                    writer.newLine();
                }
                writer.append("#");
                writer.newLine();
            }
            writer.append(p.getName());
            writer.append("=");

            String value = p.getValue();
            if (groupManager != null) {
                value = expandPropertyExpression(value, groupManager.lookupGroup(p.getGroup()));
            }

            writer.append(value);
            writer.newLine();
        }
        writer.flush();
    }

    /**
     * Configure a Property by asking the user.
     *
     * @param p the Property to configure
     * @param configurer the InteractiveConfigurer to use
     * @return true if the property was configured
     * @throws IOException indicating IO failure
     */
    private boolean configurePropertyInteractively(Property p, InteractiveConfigurer configurer) throws IOException {

        boolean result = false;

        logger.debug("Configure interactively: {}", p.getName());

        String defaultValue = p.getValue();
        if (defaultValue == null || defaultValue.length() == 0) {
            defaultValue = p.getDefaultValue();
        }

        String value = configurer.configure(p.getName(), p.getDescription(), defaultValue);

        logger.debug("Configure interactively result for '{}': {}", p.getName(), value);

        if (value != null) {
            p.setValue(value);
            result = true;
        }

        return result;
    }

}
