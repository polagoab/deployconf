/**
 * Copyright (c) 2013-2017 Polago AB
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

package org.polago.deployconf.task.properties;

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
import org.polago.deployconf.TestInteractiveConfigurer;
import org.polago.deployconf.group.ConfigGroupManager;
import org.polago.deployconf.group.InMemoryConfigGroupManager;

/**
 * Tests the {@link PropertiesTask} class.
 */
public class PropertiesTaskTest {

    @Test
    public void testDeserialize() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("simple-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("properties".equals(e.getName())) {
                PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
                task.deserialize(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getProperties());
                assertEquals(1, task.getProperties().size());
            }
        }
    }

    @Test
    public void testDeserializeWithCondition() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("condition-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("properties".equals(e.getName())) {
                PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
                task.deserialize(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getProperties());
                assertEquals(1, task.getProperties().size());
                assertEquals("'test' == 'test'", task.getProperties().iterator().next().getCondition());
            }
        }
    }

    @Test
    public void testDeserializeWithGroup() throws Exception {
        String expected = "testserver";
        String name = "ldap.server";
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();
        groupManager.lookupGroup(group).setProperty(name, expected);

        InputStream is = getClass().getClassLoader().getResourceAsStream("testgroup-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("properties".equals(e.getName())) {
                PropertiesTask task = new PropertiesTask(groupManager);
                task.deserialize(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getProperties());
                assertEquals(1, task.getProperties().size());
                assertEquals(expected, task.getProperties().iterator().next().getValue());
                assertEquals(group, task.getProperties().iterator().next().getGroup());
            }
        }
    }

    @Test
    public void testDeserializeWithGroupAndNoGroupValue() throws Exception {
        String expected = "ldap://localhost";
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();

        InputStream is = getClass().getClassLoader().getResourceAsStream("testgroup-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("properties".equals(e.getName())) {
                PropertiesTask task = new PropertiesTask(groupManager);
                task.deserialize(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getProperties());
                assertEquals(1, task.getProperties().size());
                assertEquals(expected, task.getProperties().iterator().next().getValue());
                assertEquals(group, task.getProperties().iterator().next().getGroup());
            }
        }
    }

    @Test
    public void testIsConfigured() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", null, null, "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertTrue(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingNullValue() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", null, null, null);
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingEmptyValue() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", null, null, "");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testMergeWithNewProperty() throws Exception {
        String path = "test-path";
        PropertiesTask task1 = new PropertiesTask(new InMemoryConfigGroupManager());
        task1.setPath(path);
        Property p1 = new Property("test-property", "test-descr", "test-default", null);
        p1.setGroup("test-group");
        p1.setCondition("test-condition");
        HashSet<Property> list1 = new HashSet<Property>();
        list1.add(p1);
        task1.setProperties(list1);

        PropertiesTask task2 = new PropertiesTask(new InMemoryConfigGroupManager());
        task2.setPath(path);
        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        Property p = task2.getProperties().iterator().next();
        assertEquals("test-property", p.getName());
        assertEquals("test-descr", p.getDescription());
        assertEquals("test-default", p.getDefaultValue());
        assertNull(p.getValue());
        assertEquals("test-group", p.getGroup());
        assertEquals("test-condition", p.getCondition());
    }

    @Test
    public void testMergeWithSameProperty() throws Exception {
        String path = "test-path";
        PropertiesTask task1 = new PropertiesTask(new InMemoryConfigGroupManager());
        task1.setPath(path);
        Property p1 = new Property("test-property", "test1-descr", "test1-default", null);
        p1.setGroup("test1-group");
        p1.setCondition("test1-condition");
        HashSet<Property> list1 = new HashSet<Property>();
        list1.add(p1);
        task1.setProperties(list1);

        PropertiesTask task2 = new PropertiesTask(new InMemoryConfigGroupManager());
        task2.setPath(path);
        Property p2 = new Property("test-property", "test2-descr", "test2-default", "test-value");
        p2.setGroup("test-group2");
        p2.setCondition("test2-condition");
        HashSet<Property> list2 = new HashSet<Property>();
        list2.add(p2);
        task2.setProperties(list2);

        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        assertEquals(1, task2.getProperties().size());
        Property p = task2.getProperties().iterator().next();
        assertEquals("test-property", p.getName());
        assertEquals("test1-descr", p.getDescription());
        assertEquals("test1-default", p.getDefaultValue());
        assertEquals("test-value", p.getValue());
        assertEquals("test1-group", p.getGroup());
        assertEquals("test1-condition", p.getCondition());
    }

    @Test
    public void testMergeWithReplacingProperty() throws Exception {
        String path = "test-path";
        PropertiesTask task1 = new PropertiesTask(new InMemoryConfigGroupManager());
        task1.setPath(path);
        Property p1 = new Property("test1-property", "test1-descr", "test1-default", null);
        p1.setGroup("test1-group");
        p1.setCondition("test1-condition");
        HashSet<Property> list1 = new HashSet<Property>();
        list1.add(p1);
        task1.setProperties(list1);

        PropertiesTask task2 = new PropertiesTask(new InMemoryConfigGroupManager());
        task2.setPath(path);
        Property p2 = new Property("test2-property", "test2-descr", "test2-default", "test-value");
        p2.setGroup("test2-group");
        p2.setCondition("test2-condition");
        HashSet<Property> list2 = new HashSet<Property>();
        list2.add(p2);
        task2.setProperties(list2);

        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        assertEquals(1, task2.getProperties().size());
        Property p = task2.getProperties().iterator().next();
        assertEquals("test1-property", p.getName());
        assertEquals("test1-descr", p.getDescription());
        assertEquals("test1-default", p.getDefaultValue());
        assertNull(p.getValue());
        assertEquals("test1-group", p.getGroup());
        assertEquals("test1-condition", p.getCondition());
    }

    @Test
    public void testSerialize() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        task.setPath("test-path");
        Property p = new Property("test-name", "test-description", "test-default-value", "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        Element node = new Element("properties");
        task.serialize(node);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<properties path=\"test-path\">" + "<property><name>test-name</name>"
            + "<description><![CDATA[test-description]]></description>"
            + "<default>test-default-value</default><condition /><value>test-value</value>"
            + "</property></properties>", outputter.outputString(node).replaceAll("[\\n\\r]*", ""));
    }

    @Test
    public void testSerializeWithCondition() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        task.setPath("test-path");
        Property p = new Property("test-name", "test-description", "test-default-value", "test-value");
        p.setCondition("true");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        Element node = new Element("properties");
        task.serialize(node);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<properties path=\"test-path\">" + "<property><name>test-name</name>"
            + "<description><![CDATA[test-description]]></description>"
            + "<default>test-default-value</default><condition>true</condition><value>test-value</value>"
            + "</property></properties>", outputter.outputString(node).replaceAll("[\\n\\r]*", ""));
    }

    @Test
    public void testSerializeWithGroup() throws Exception {
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();

        PropertiesTask task = new PropertiesTask(groupManager);

        task.setPath("test-path");
        Property p = new Property("test-name", "test-description", "test-default-value", "test-value");
        p.setGroup(group);

        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);
        Element node = new Element("properties");
        task.serialize(node);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<properties path=\"test-path\">" + "<property group=\"" + group + "\"><name>test-name</name>"
            + "<description><![CDATA[test-description]]></description>" + "<default>test-default-value</default>"
            + "<condition /></property></properties>", outputter.outputString(node).replaceAll("[\\n\\r]*", ""));
    }

    @Test
    public void testApplyWithDescription() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", "test-description", null, "test-value");
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
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", null, null, "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(null, out);
        assertEquals("\ntest-property=test-value\n", out.toString());
    }

    @Test
    public void testApplyWithExpandingConfigGroupProperty() throws Exception {
        String group = "test-group";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();
        groupManager.lookupGroup(group).setProperty("test-name", "epxanded-test-value");

        PropertiesTask task = new PropertiesTask(groupManager);
        Property p = new Property("test-property", "test-description", null, "${test-name}/other");
        p.setGroup(group);

        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(null, out);
        assertTrue(out.toString().contains("test-property=epxanded-test-value/other"));
    }

    @Test
    public void testApplyWithCondition() throws Exception {
        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());
        Property p = new Property("test-property", null, null, "test-value");
        HashSet<Property> list = new HashSet<Property>();
        p.setCondition("false");
        list.add(p);
        task.setProperties(list);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(null, out);
        assertFalse(out.toString().contains("test-property=test-value"));
    }

    @Test
    public void testIncompleteInteractiveConfigure() throws Exception {
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());

        Property p = new Property("test-property", "test-description", "default-value", null);
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

        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());

        Property p = new Property("test-property", "test-description", "default-value", null);
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

        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());

        Property p = new Property("test-property", "test-description", "default-value", "test-value");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        boolean result = task.configureInteractively(configurer, true);

        assertTrue(result);
        assertTrue(configurer.isCalled);
        assertEquals(expected, p.getValue());
    }

    @Test
    public void testIgnoreInteractiveConfigure() throws Exception {
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        PropertiesTask task = new PropertiesTask(new InMemoryConfigGroupManager());

        Property p = new Property("test-property", "test-description", "default-value", null);
        p.setCondition("false");
        HashSet<Property> list = new HashSet<Property>();
        list.add(p);
        task.setProperties(list);

        boolean result = task.configureInteractively(configurer, false);

        assertTrue(result);
        assertFalse(configurer.isCalled);
    }
}
