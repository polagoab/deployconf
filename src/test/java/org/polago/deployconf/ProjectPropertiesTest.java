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

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.junit.Test;
import org.polago.deployconf.ProjectProperties;

/**
 * Tests the {@link ProjectProperties} class.
 */
public class ProjectPropertiesTest {

    @Test
    public void testPropertiesFileExists() throws IOException {
        ProjectProperties.instance();
    }

    @Test
    public void testProjectNameExists() throws IOException {
        assertNotNull(ProjectProperties.instance().getName());
    }

    @Test
    public void testProjectVersionExists() throws IOException {
        assertNotNull(ProjectProperties.instance().getVersion());
    }

    @Test
    public void testProjectCopyrightMessageExists() throws IOException {
        assertNotNull(ProjectProperties.instance().getCopyrightMessage());
    }

    @Test
    public void testProjectHelpHeaderExists() throws IOException {
        assertNotNull(ProjectProperties.instance().getHelpHeader());
    }

}
