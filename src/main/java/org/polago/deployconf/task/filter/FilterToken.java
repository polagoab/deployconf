/**
 * Copyright (c) 2013-2014 Polago AB
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

package org.polago.deployconf.task.filter;

import java.util.regex.Pattern;

/**
 * A single Token in a Filter Task.
 */
public class FilterToken {

    private String name;

    private Pattern regex;

    private String description;

    private String value;

    private String defaultValue;

    /**
     * Public Constructor.
     *
     * @param name the name of this token
     * @param regex the token Pattern
     * @param description the token description
     * @param defaultValue the token default value
     * @param value the token value
     */
    public FilterToken(String name, String regex, String description,
        String defaultValue, String value) {

        this.name = name;
        this.regex = Pattern.compile(regex);
        this.description = description;
        this.defaultValue = defaultValue;
        this.value = value;
    }

    /**
     * Gets the name property value.
     *
     * @return the current value of the name property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name property.
     *
     * @param name the new property value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the regex property value.
     *
     * @return the current value of the regex property
     */
    public Pattern getRegex() {
        return regex;
    }

    /**
     * Sets the regex property.
     *
     * @param regex the new property value
     */
    public void setRegex(Pattern regex) {
        this.regex = regex;
    }

    /**
     * Gets the description property value.
     *
     * @return the current value of the description property
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description property.
     *
     * @param description the new property value
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the value property value.
     *
     * @return the current value of the value property
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value property.
     *
     * @param value the new property value
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the defaultValue property value.
     *
     * @return the current value of the defaultValue property
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the defaultValue property.
     *
     * @param defaultValue the new property value
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        boolean result = false;

        if (other instanceof FilterToken) {
            FilterToken otherToken = (FilterToken) other;
            result = getName().equals(otherToken.getName());
        }

        return result;
    }

}
