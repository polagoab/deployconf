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

package se.polago.deployconf.task.filter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.polago.deployconf.InteractiveConfigurer;
import se.polago.deployconf.task.AbstractTask;

/**
 * Deployment Task for filtering tokens in a file.
 */
public class FilterTask extends AbstractTask {

    private static Logger logger = LoggerFactory.getLogger(FilterTask.class);

    /**
     * Task element name in config file.
     */
    public static final String DOM_ELEMENT_TASK = "filter";

    private static final String DOM_ELEMENT_NAME = "name";

    private static final String DOM_ELEMENT_REGEX = "regex";

    private static final String DOM_ELEMENT_TOKEN = "token";

    private static final String ATTRIBUTE_ENCODING = "encoding";

    private Set<FilterToken> tokens;

    private String encoding = "UTF-8";

    /**
     * Public Constructor.
     */
    public FilterTask() {
        super();
        tokens = new HashSet<FilterToken>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deserialize(Element root) {
        super.deserialize(root);
        String enc = root.getAttributeValue(ATTRIBUTE_ENCODING);
        if (enc != null) {
            encoding = enc;
        }
        for (Element e : root.getChildren()) {
            String name = e.getChildText(DOM_ELEMENT_NAME);
            if (name == null) {
                throw new IllegalStateException(
                    "Filter name element does not exists");
            }
            String regex = e.getChildText(DOM_ELEMENT_REGEX);
            if (regex == null) {
                throw new IllegalStateException(
                    "Filter regex element does not exists");
            }
            String description = e.getChildText(DOM_ELEMENT_DESCRIPTION);
            if (description == null) {
                throw new IllegalStateException(
                    "Filter description element does not exists");
            }
            String defaultValue = e.getChildText(DOM_ELEMENT_DEFAULT);
            String value = e.getChildText(DOM_ELEMENT_VALUE);

            FilterToken t =
                new FilterToken(name, regex, description, defaultValue, value);
            tokens.add(t);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void serialize(Element node) {
        super.serialize(node);
        node.setAttribute(ATTRIBUTE_ENCODING, getEncoding());
        for (FilterToken t : tokens) {
            Element e = createJDOMElement(DOM_ELEMENT_TOKEN, null);
            e.addContent(createJDOMElement(DOM_ELEMENT_NAME, t.getName()));
            e.addContent(createJDOMElement(DOM_ELEMENT_REGEX, t.getRegex()
                .toString()));
            e.addContent(createJDOMElement(DOM_ELEMENT_DESCRIPTION,
                t.getDescription()));
            e.addContent(createJDOMElement(DOM_ELEMENT_DEFAULT,
                t.getDefaultValue()));
            e.addContent(createJDOMElement(DOM_ELEMENT_VALUE, t.getValue()));

            node.addContent(e);
        }
    }

    /**
     * Gets the tokens property value.
     *
     * @return the current value of the tokens property
     */
    public Set<FilterToken> getTokens() {
        return tokens;
    }

    /**
     * Sets the tokens property.
     *
     * @param tokens the new property value
     */
    void setTokens(Set<FilterToken> tokens) {
        this.tokens = tokens;
    }

    /**
     * Gets the encoding to use when filtering files.
     *
     * @return the encoding to use
     */
    private String getEncoding() {
        return encoding;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConfigured() {
        for (FilterToken t : tokens) {
            if (t.getValue() == null || t.getValue().length() == 0) {
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

        for (FilterToken t : tokens) {
            if (force || t.getValue() == null || t.getValue().length() == 0) {
                result = configureTokenInteractively(t, configurer);
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

        InputStreamReader in = new InputStreamReader(source, getEncoding());
        BufferedReader reader = new BufferedReader(in);

        OutputStreamWriter out =
            new OutputStreamWriter(destination, getEncoding());
        BufferedWriter writer = new BufferedWriter(out);

        String line = reader.readLine();
        while (line != null) {
            line = filterLine(line);
            writer.write(line);
            line = reader.readLine();
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
        for (FilterToken t : getTokens()) {
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
        if (other instanceof FilterTask) {
            FilterTask otherTask = (FilterTask) other;
            result =
                getPath().equals(otherTask.getPath())
                    && getTokens().equals(otherTask.getTokens());
        }

        return result;
    }

    /**
     * Filter the given line.
     *
     * @param line the line to process
     * @return the filtered line
     */
    private String filterLine(String line) {
        for (FilterToken token : getTokens()) {
            Matcher matcher = token.getRegex().matcher(line);
            line = matcher.replaceAll(token.getValue());
        }

        return line;
    }

    /**
     * Configure a FilterToken by asking the user.
     *
     * @param t the FilterToken to configure
     * @param configurer the InteractiveConfigurer to use
     * @return true if the token was configured
     * @throws IOException indicating IO failure
     */
    private boolean configureTokenInteractively(FilterToken t,
        InteractiveConfigurer configurer) throws IOException {

        boolean result = false;

        logger.debug("Configure interactively: {}", t.getRegex().toString());

        String defaultValue = t.getValue();
        if (defaultValue == null || defaultValue.length() == 0) {
            defaultValue = t.getDefaultValue();
        }

        String value =
            configurer
                .configure(t.getName(), t.getDescription(), defaultValue);
        logger.debug("Configure interactively result for '{}({})': {}",
            t.getName(), t.getRegex().toString(), value);
        if (value != null) {
            t.setValue(value);
            result = true;
        }

        return result;
    }

}
