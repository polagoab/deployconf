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

import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the {@link FileSystemConfigGroupManager} class.
 */
public class FileSystemConfigGroupManagerTest {

    private static class TestFileSystemConfigGroupManager extends FileSystemConfigGroupManager {

        Map<String, Integer> groups;

        /**
         * Public Constructor.
         *
         * @param dir
         */
        TestFileSystemConfigGroupManager(Path dir) {
            super(dir);
            groups = new HashMap<String, Integer>();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected ConfigGroup newGroup(String name) {
            Integer instances = groups.get(name);
            if (instances == null) {
                groups.put(name, 1);
            }
            return new InMemoryConfigGroup();
        }

    }

    private TestFileSystemConfigGroupManager manager;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Before
    public void setUp() throws IOException {
        manager = new TestFileSystemConfigGroupManager(folder.newFolder().toPath());
    }

    @Test
    public void testLookupGroup() throws IOException {
        String group = "test";
        assertNotNull(manager.lookupGroup(group));
        assertEquals(Integer.valueOf(1), manager.groups.get(group));
        assertEquals(1, manager.groups.size());
    }

    @Test
    public void testLookupTwoGroup() throws IOException {
        String group1 = "test1";
        assertNotNull(manager.lookupGroup(group1));
        assertEquals(Integer.valueOf(1), manager.groups.get(group1));

        String group2 = "test2";
        assertNotNull(manager.lookupGroup(group2));
        assertEquals(Integer.valueOf(1), manager.groups.get(group2));

        assertEquals(2, manager.groups.size());
    }

    @Test
    public void testLookupGroupSameGroup() throws IOException {
        String group = "test";
        assertNotNull(manager.lookupGroup(group));
        assertNotNull(manager.lookupGroup(group));
        assertEquals(Integer.valueOf(1), manager.groups.get(group));
        assertEquals(1, manager.groups.size());
    }

}
