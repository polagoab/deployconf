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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.jdom2.CDATA;
import org.jdom2.Element;
import org.jdom2.Text;
import org.polago.deployconf.group.ConfigGroup;
import org.polago.deployconf.group.ConfigGroupManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common implementation of a deployment Task.
 */
public abstract class AbstractTask implements Task {

    private static Logger logger = LoggerFactory.getLogger(AbstractTask.class);

    // Matches a property expression like ${propertyName}
    private static final Pattern EXPANSION_PATTERN = Pattern.compile("(\\$\\{([^}]+?)\\})");

    protected static final String DOM_ATTRIBUTE_PATH = "path";

    protected static final String DOM_ATTRIBUTE_GROUP = "group";

    protected static final String DOM_ATTRIBUTE_IF = "if";

    protected static final String DOM_ELEMENT_DESCRIPTION = "description";

    protected static final String DOM_ELEMENT_DEFAULT = "default";

    protected static final String DOM_ELEMENT_VALUE = "value";

    protected static final String DOM_ELEMENT_CONDITION = "condition";

    private String path;

    private final ScriptEngine scriptEngine;

    /**
     * Public Constructor.
     */
    public AbstractTask() {
        super();

        ScriptEngineManager factory = new ScriptEngineManager();
        scriptEngine = factory.getEngineByName("JavaScript");
    }

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
    public void deserialize(Element root, ConfigGroupManager groupManager) throws IOException {
        String attribute = root.getAttributeValue(DOM_ATTRIBUTE_PATH);
        if (attribute == null) {
            throw new IllegalStateException("path attribute is required");
        }
        setPath(attribute);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Element node, ConfigGroupManager groupManager) throws IOException {
        node.setAttribute(DOM_ATTRIBUTE_PATH, getPath());
    }

    /**
     * Replace matching properties from the given groupManager in the given text.
     *
     * @param text the text to expand
     * @param group the ConfigGroup to use as source for expanding property expressions
     * @return the expanded value, if possible
     */
    protected String expandPropertyExpression(String text, ConfigGroup group) {
        String result = text;

        if (text != null) {
            Matcher matcher = EXPANSION_PATTERN.matcher(text);
            StringBuffer expanded = new StringBuffer(text.length());
            while (matcher.find()) {
                String name = matcher.group(2);
                String value = group.getProperty(name);
                if (value == null) {
                    value = matcher.group(0);
                }
                matcher.appendReplacement(expanded, Matcher.quoteReplacement(value));
            }
            matcher.appendTail(expanded);
            result = expanded.toString();
        }

        return result;
    }

    /**
     * Evaluate the task condition.
     *
     * @param text the condition to evaluate
     * @param group the ConfigGroup to use as source for expanding property expressions before evaluation
     * @return the evaluation result
     */
    public boolean evaluateCondition(String text, ConfigGroup group) {
        boolean result = true;
        try {
            if (text != null) {
                Object resultObject = scriptEngine.eval(expandPropertyExpression(text, group));
                if (resultObject instanceof Boolean) {
                    Boolean boolResult = (Boolean) resultObject;
                    return boolResult.booleanValue();
                }
            }
        } catch (ScriptException e) {
            logger.info("Unable to evaluate condition: " + text, e);
        }

        return result;
    }

    /**
     * Create a JDOM Element with the given name.
     *
     * @param name the element name
     * @return a JDOM Element instance
     */
    protected Element createJDOMElement(String name) {
        Element result = new Element(name);

        return result;
    }

    /**
     * Create a JDOM Text Element with the given name and possibly text.
     *
     * @param name the element name
     * @param text the element text or null
     * @return a JDOM Element instance
     */
    protected Element createJDOMTextElement(String name, String text) {
        Element result = new Element(name);
        if (text != null) {
            result.setContent(new Text(text));
        }

        return result;
    }

    /**
     * Create a JDOM CDATA Element with the given name and possibly text.
     *
     * @param name the element name
     * @param text the element text or null
     * @return a JDOM Element instance
     */
    protected Element createJDOMCDATAElement(String name, String text) {
        Element result = new Element(name);
        if (text != null) {
            result.addContent(new CDATA(text));
        }

        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof AbstractTask) {
            AbstractTask otherTask = (AbstractTask) other;
            result = getPath().equals(otherTask.getPath());
        }

        return result;
    }
}
