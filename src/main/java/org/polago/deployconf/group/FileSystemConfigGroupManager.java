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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * ConfigGroupManager that stores config groups in a file system.
 */
public class FileSystemConfigGroupManager implements ConfigGroupManager {

    private final Path dir;

    private final Map<String, ConfigGroup> groups;

    /**
     * Public Constructor.
     *
     * @param dir the directory use for storing config groups.
     */
    public FileSystemConfigGroupManager(Path dir) {
        if (dir == null) {
            throw new IllegalArgumentException("dir is null");
        }

        if (!Files.isDirectory(dir)) {
            throw new IllegalArgumentException("dir does not exists: " + dir);
        }

        this.dir = dir;
        groups = new HashMap<String, ConfigGroup>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConfigGroup lookupGroup(String name) throws IOException {
        ConfigGroup result = groups.get(name);
        if (result == null) {
            result = newGroup(name);
            groups.put(name, result);
        }

        return result;

    }

    /**
     * Create a new ConfigGroup.
     *
     * @param name the group name
     * @return a ConfigGroup instance
     * @throws IOException indicating failure
     */
    protected ConfigGroup newGroup(String name) throws IOException {
        return new FileSystemConfigGroup(name, dir);
    }

}
