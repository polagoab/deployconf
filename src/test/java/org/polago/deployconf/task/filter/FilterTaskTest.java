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

package org.polago.deployconf.task.filter;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
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
 * Tests the {@link FilterTask} class.
 */
public class FilterTaskTest {

    @Test
    public void testDeserialize() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("simple-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("filter".equals(e.getName())) {
                FilterTask task = new FilterTask();
                task.deserialize(e, null);
                assertNotNull(task.getPath());
                assertNotNull(task.getTokens());
                assertEquals(1, task.getTokens().size());
            }

        }
    }

    @Test
    public void testDeserializeWithGroup() throws Exception {
        String expected = "testdir";
        String name = "LogDir";
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();
        groupManager.lookupGroup(group).setProperty(name, expected);

        InputStream is = getClass().getClassLoader().getResourceAsStream("testgroup-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("filter".equals(e.getName())) {
                FilterTask task = new FilterTask();
                task.deserialize(e, groupManager);
                assertNotNull(task.getPath());
                assertNotNull(task.getTokens());
                assertEquals(1, task.getTokens().size());
                assertEquals(expected, task.getTokens().iterator().next().getValue());
                assertEquals(group, task.getTokens().iterator().next().getGroup());
            }
        }
    }

    @Test
    public void testDeserializeWithGroupAndNoGroupValue() throws Exception {
        String expected = "/var/log";
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();

        InputStream is = getClass().getClassLoader().getResourceAsStream("testgroup-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        for (Element e : tasks) {
            if ("filter".equals(e.getName())) {
                FilterTask task = new FilterTask();
                task.deserialize(e, groupManager);
                assertNotNull(task.getPath());
                assertNotNull(task.getTokens());
                assertEquals(1, task.getTokens().size());
                assertEquals(expected, task.getTokens().iterator().next().getValue());
                assertEquals(group, task.getTokens().iterator().next().getGroup());
            }
        }
    }


    @Test
    public void testIsConfigured() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t = new FilterToken("test-name", "test-token", null, null, "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertTrue(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingNullValue() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t = new FilterToken("test-name", "test-token", null, null, null);
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingEmptyValue() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t = new FilterToken("test-name", "test-token", null, null, "");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testMergeWithNewFilterToken() throws Exception {
        String path = "test-path";
        FilterTask task1 = new FilterTask();
        task1.setPath(path);
        FilterToken t1 = new FilterToken("test-token", "test-regex", "test-descr", "test-default", null);
        HashSet<FilterToken> list1 = new HashSet<FilterToken>();
        list1.add(t1);
        task1.setTokens(list1);

        FilterTask task2 = new FilterTask();
        task2.setPath(path);
        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        assertEquals(task1.getTokens(), task2.getTokens());
    }

    @Test
    public void testMergeWithSameFilterToken() throws Exception {
        String path = "test-path";
        FilterTask task1 = new FilterTask();
        task1.setPath(path);
        FilterToken t1 = new FilterToken("test-token", "test1-regex", "test1-descr", "test1-default", null);
        HashSet<FilterToken> list1 = new HashSet<FilterToken>();
        list1.add(t1);
        task1.setTokens(list1);

        FilterTask task2 = new FilterTask();
        task2.setPath(path);
        FilterToken t2 = new FilterToken("test-token", "test2-regex", "test2-descr", "test2-default", "test-value");
        HashSet<FilterToken> list2 = new HashSet<FilterToken>();
        list2.add(t2);
        task2.setTokens(list2);

        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        assertEquals(1, task2.getTokens().size());
        FilterToken t = task2.getTokens().iterator().next();
        assertEquals("test-token", t.getName());
        assertEquals("test1-regex", t.getRegex().toString());
        assertEquals("test1-descr", t.getDescription());
        assertEquals("test1-default", t.getDefaultValue());
        assertEquals("test-value", t.getValue());
    }

    @Test
    public void testMergeWithReplacingFilterToken() throws Exception {
        String path = "test-path";
        FilterTask task1 = new FilterTask();
        task1.setPath(path);
        FilterToken t1 = new FilterToken("test1-token", "test1-regex", "test1-descr", "test1-default", null);
        HashSet<FilterToken> list1 = new HashSet<FilterToken>();
        list1.add(t1);
        task1.setTokens(list1);

        FilterTask task2 = new FilterTask();
        task2.setPath(path);
        FilterToken t2 = new FilterToken("test2-token", "test2-regex", "test2-descr", "test2-default", "test-value");
        HashSet<FilterToken> list2 = new HashSet<FilterToken>();
        list2.add(t2);
        task2.setTokens(list2);

        task2.merge(task1);

        assertEquals(task1, task2); // compare paths
        assertEquals(1, task2.getTokens().size());
        FilterToken t = task2.getTokens().iterator().next();
        assertEquals("test1-token", t.getName());
        assertEquals("test1-regex", t.getRegex().toString());
        assertEquals("test1-descr", t.getDescription());
        assertEquals("test1-default", t.getDefaultValue());
        assertNull(t.getValue());
    }

    @Test
    public void testSerialize() throws Exception {
        FilterTask task = new FilterTask();
        task.setPath("test-path");
        FilterToken t =
            new FilterToken("test-name", "test-regex", "test-description", "test-default-value", "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        Element node = new Element("filter");
        task.serialize(node, null);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals(
            "<filter path=\"test-path\" encoding=\"UTF-8\">" + "<token><name>test-name</name><regex>test-regex</regex>"
                + "<description><![CDATA[test-description]]></description>"
                + "<default>test-default-value</default><value>test-value</value>" + "</token></filter>",
            outputter.outputString(node));
    }

    @Test
    public void testSerializeWithGroup() throws Exception {
        FilterTask task = new FilterTask();
        String group = "testgroup";
        ConfigGroupManager groupManager = new InMemoryConfigGroupManager();

        task.setPath("test-path");
        FilterToken t =
            new FilterToken("test-name", "test-regex", "test-description", "test-default-value", "test-value");
        t.setGroup(group);

        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        Element node = new Element("filter");
        task.serialize(node, groupManager);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<filter path=\"test-path\" encoding=\"UTF-8\">" + "<token group=\"" + group
            + "\"><name>test-name</name><regex>test-regex</regex>"
            + "<description><![CDATA[test-description]]></description>"
            + "<default>test-default-value</default></token></filter>", outputter.outputString(node));
    }

    @Test
    public void testApply() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t = new FilterToken("test-name", "d..a", null, null, "value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);

        String data = "test-data";
        ByteArrayInputStream in = new ByteArrayInputStream(data.getBytes());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        task.apply(in, out);

        assertEquals("test-value\n", out.toString());
    }

    @Test
    public void testIncompleteInteractiveConfigure() throws Exception {
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();

        FilterTask task = new FilterTask();

        FilterToken p = new FilterToken("test-name", "test-regex", "test-description", "default-value", null);
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(p);
        task.setTokens(list);

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

        FilterTask task = new FilterTask();

        FilterToken p = new FilterToken("test-name", "test-regex", "test-description", "default-value", null);
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(p);
        task.setTokens(list);

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

        FilterTask task = new FilterTask();

        FilterToken p = new FilterToken("test-name", "test-regex", "test-description", "default-value", "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(p);
        task.setTokens(list);

        boolean result = task.configureInteractively(configurer, true);

        assertTrue(result);
        assertTrue(configurer.isCalled);
        assertEquals(expected, p.getValue());
    }
}
