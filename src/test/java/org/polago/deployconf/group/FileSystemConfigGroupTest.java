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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/**
 * Tests the {@link FileSystemConfigGroup} class.
 */
public class FileSystemConfigGroupTest {

    private static final String NAME = "testgroup";

    private static final String EXISTING_NAME = "existingname";

    private static final String EXISTING_VALUE = "existingvalue";

    private static final String NON_EXISTING_NAME = "nonexistingname";

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private FileSystemConfigGroup group;

    @Before
    public void setUp() throws IOException {
        group = new FileSystemConfigGroup(NAME, folder.newFolder().toPath());
        group.setProperty(EXISTING_NAME, EXISTING_VALUE);
    }

    @Test
    public void testGetExistingProperty() {
        assertEquals(EXISTING_VALUE, group.getProperty(EXISTING_NAME));
    }

    @Test
    public void testGetNonExistingProperty() {
        assertNull(group.getProperty(NON_EXISTING_NAME));
    }

    @Test
    public void testSetNullValueProperty() throws IOException {
        group.setProperty(NON_EXISTING_NAME, null);
    }
}
