/**
* Copyright (c) 2015 Polago AB
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

package org.polago.deployconf.group;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * ConfigGroup that uses the file system for storing.
 */
public class FileSystemConfigGroup implements ConfigGroup {

    private static final String SUFFIX_CONFIG_GROUP = "-config-group.properties";

    private final Path path;

    private final Properties properties;

    /**
     * Public Constructor.
     *
     * @param name the config group name
     * @param dir the repository where to store config groups
     * @throws IOException indicating IO error
     */
    public FileSystemConfigGroup(String name, Path dir) throws IOException {
        path = dir.resolve(name + SUFFIX_CONFIG_GROUP);
        properties = new Properties();

        if (Files.exists(path)) {
            InputStream is = Files.newInputStream(path);
            try {
                properties.load(is);
            } finally {
                is.close();
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProperty(String name, String value) throws IOException {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.setProperty(name, value);
            OutputStream os = Files.newOutputStream(path, StandardOpenOption.CREATE);
            try {
                properties.store(os, null);
            } finally {
                os.close();
            }
        }
    }

}
