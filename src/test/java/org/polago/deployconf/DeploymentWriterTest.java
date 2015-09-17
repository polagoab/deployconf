/**
 * Copyright (c) 2013-2015 Polago AB
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

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.jdom2.Document;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;

/**
 * Tests the {@link DeploymentWriter} class.
 */
public class DeploymentWriterTest {

    @Test
    public void testPersistEmptyConfig() throws Exception {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        TestTask task = new TestTask();
        deploymentConfig.addTask(task);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DeploymentWriter writer = new DeploymentWriter(os, null);

        writer.persist(deploymentConfig);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray().clone());
        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);

        assertEquals(DeploymentWriter.DOM_ROOT, d.getRootElement().getName());
        assertNotNull(d.getRootElement().getChild(task.getSerializedName()));
    }

    @Test
    public void testPersistEmptyConfigWithName() throws Exception {
        DeploymentConfig deploymentConfig = new DeploymentConfig();
        TestTask task = new TestTask();
        deploymentConfig.addTask(task);
        String name = "test";
        deploymentConfig.setName(name);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        DeploymentWriter writer = new DeploymentWriter(os, null);

        writer.persist(deploymentConfig);

        ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray().clone());
        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);

        assertEquals(DeploymentWriter.DOM_ROOT, d.getRootElement().getName());
        assertEquals(name, d.getRootElement().getAttributeValue(DeploymentWriter.ATTR_NAME));
        assertNotNull(d.getRootElement().getChild(task.getSerializedName()));
    }

}
