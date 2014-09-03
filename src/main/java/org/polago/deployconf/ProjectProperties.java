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
import java.io.InputStream;
import java.util.Properties;

/**
 * Provide Project Properties for this project.
 */
public final class ProjectProperties {

    private static final String RESOURCE = "Project.properties";

    private static final String KEY_NAME = "project.name";

    private static final String KEY_VERSION = "project.version";

    private static final String KEY_COPYRIGHT_MESSAGE =
        "project.copyright.message";

    private static final String KEY_HELP_HEADER = "project.help.header";

    private final Properties properties;

    private static ProjectProperties instance;

    /**
     * Private Constructor.
     *
     * @throws IOException indicating IO Error
     */
    private ProjectProperties() throws IOException {
        InputStream in =
            Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(RESOURCE);
        properties = new Properties();
        properties.load(in);
        in.close();
    }

    /**
     * Singleton access.
     *
     * @return a ProjectProperties instance
     * @throws IOException indicating IO error
     */
    public static ProjectProperties instance() throws IOException {
        if (instance == null) {
            instance = new ProjectProperties();
        }

        return instance;
    }

    /**
     * Gets the name property.
     *
     * @return the value of the name property
     */
    public String getName() {
        return properties.getProperty(KEY_NAME);
    }

    /**
     * Gets the version property.
     *
     * @return the value of the version property
     */
    public String getVersion() {
        return properties.getProperty(KEY_VERSION);
    }

    /**
     * Gets the copyright property.
     *
     * @return the value of the copyright property
     */
    public String getCopyrightMessage() {
        return properties.getProperty(KEY_COPYRIGHT_MESSAGE);
    }

    /**
     * Gets the help header property.
     *
     * @return the value of the help header property
     */
    public String getHelpHeader() {
        return properties.getProperty(KEY_HELP_HEADER);
    }

}
