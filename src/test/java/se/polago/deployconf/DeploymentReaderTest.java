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

package se.polago.deployconf;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;

/**
 *  Tests the {@link DeploymentReader} class.
 */
public class DeploymentReaderTest {

    @Test
    public void testNullStream() {
        try {
            new DeploymentReader(null);
            fail();
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void testInvalidStream() {
        ByteArrayInputStream is = new ByteArrayInputStream("".getBytes());
        DeploymentReader reader = new DeploymentReader(is);
        try {
            reader.parse();
            fail();
        } catch (Exception e) {
            // OK
        }
    }

    @Test
    public void testEmptyStream() throws Exception {
        InputStream is =
            getClass().getClassLoader().getResourceAsStream(
                "empty-deployment-config.xml");
        assertNotNull(is);

        DeploymentReader reader = new DeploymentReader(is);
        DeploymentConfig config = reader.parse();
        assertNotNull(config);
        assertTrue(config.isEmpty());
    }


    @Test
    public void testSimpleStream() throws Exception {
        InputStream is =
            getClass().getClassLoader().getResourceAsStream(
                "simple-deployment-config.xml");
        assertNotNull(is);

        DeploymentReader reader = new DeploymentReader(is);
        DeploymentConfig config = reader.parse();
        assertNotNull(config);
        assertTrue(!config.isEmpty());
    }
}
