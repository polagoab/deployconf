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

package se.polago.deployconf.task.properties;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.junit.Test;

import se.polago.deployconf.TestInteractiveConfigurer;

/**
 * Tests the {@link PropertiesTask} class.
 */
public class PropertiesTaskTest {

    @Test
    public void testConfigure() throws Exception {
        InputStream is =
            getClass().getClassLoader().getResourceAsStream(
                "simple-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("properties".equals(e.getName())) {
                PropertiesTask task = new PropertiesTask();
                task.configure(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getProperties());
                assertEquals(1, task.getProperties().size());
            }

        }
    }

    @Test
    public void testIsConfigured() throws Exception {
        PropertiesTask task = new PropertiesTask();
        Property p = new Property("test-property", null, null, "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertTrue(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingNullValue() throws Exception {
        PropertiesTask task = new PropertiesTask();
        Property p = new Property("test-property", null, null, null);
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingEmptyValue() throws Exception {
        PropertiesTask task = new PropertiesTask();
        Property p = new Property("test-property", null, null, "");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testSerialize() throws Exception {
        PropertiesTask task = new PropertiesTask();
        task.setPath("test-path");
        Property p =
            new Property("test-name", "test-description",
                "test-default-value", "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        Element node = new Element("properties");
        task.serialize(node);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<properties path=\"test-path\">"
            + "<property><name>test-name</name>"
            + "<description>test-description</description>"
            + "<default>test-default-value</default><value>test-value</value>"
            + "</property></properties>", outputter.outputString(node));
    }

    @Test
    public void testApplyWithDescription() throws Exception {
        PropertiesTask task = new PropertiesTask();
        Property p =
            new Property("test-property", "test-description", null,
                "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(null, out);
        assertTrue(out.toString().contains("test-property=test-value"));
        assertTrue(out.toString().contains("# test-description"));
    }

    @Test
    public void testApplyWithoutDescription() throws Exception {
        PropertiesTask task = new PropertiesTask();
        Property p = new Property("test-property", null, null, "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(null, out);
        assertEquals("\ntest-property=test-value\n", out.toString());
    }

    @Test
    public void testIncompleteInteractiveConfigure() throws Exception {
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        PropertiesTask task = new PropertiesTask();

        Property p =
            new Property("test-property", "test-description", "default-value",
                null);
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        boolean result = task.configureInteractively(configurer, false);

        assertFalse(result);
        assertTrue(configurer.isCalled);
        assertNull(p.getValue());
    }

    @Test
    public void testCompletedInteractiveConfigure() throws Exception {
        String expected = "interactive-value";

        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        configurer.value = expected;

        PropertiesTask task = new PropertiesTask();

        Property p =
            new Property("test-property", "test-description", "default-value",
                null);
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        boolean result = task.configureInteractively(configurer, false);

        assertTrue(result);
        assertTrue(configurer.isCalled);
        assertEquals(expected, p.getValue());
    }

    @Test
    public void testForceInteractiveConfigure() throws Exception {
        String expected = "interactive-value";

        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        configurer.value = expected;

        PropertiesTask task = new PropertiesTask();

        Property p =
            new Property("test-property", "test-description", "default-value",
                "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        boolean result = task.configureInteractively(configurer, true);

        assertTrue(result);
        assertTrue(configurer.isCalled);
        assertEquals(expected, p.getValue());
    }
}
