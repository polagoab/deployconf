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

package se.polago.deployconf.task.filter;

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

import se.polago.deployconf.TestInteractiveConfigurer;

/**
 * Tests the {@link FilterTask} class.
 */
public class FilterTaskTest {

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
            if ("filter".equals(e.getName())) {
                FilterTask task = new FilterTask();
                task.configure(e);
                assertNotNull(task.getPath());
                assertNotNull(task.getTokens());
                assertEquals(1, task.getTokens().size());
            }

        }
    }

    @Test
    public void testIsConfigured() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t =
            new FilterToken("test-name", "test-token", null, null,
                "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertTrue(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingNullValue() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t =
            new FilterToken("test-name", "test-token", null, null, null);
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testIsNotConfiguredUsingEmptyValue() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t =
            new FilterToken("test-name", "test-token", null, null, "");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        assertFalse(task.isConfigured());
    }

    @Test
    public void testSerialize() throws Exception {
        FilterTask task = new FilterTask();
        task.setPath("test-path");
        FilterToken t =
            new FilterToken("test-name", "test-regex", "test-description",
                "test-default-value", "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(t);
        task.setTokens(list);
        Element node = new Element("filter");
        task.serialize(node);
        XMLOutputter outputter = new XMLOutputter();
        assertEquals("<filter path=\"test-path\" encoding=\"UTF-8\">"
            + "<token><name>test-name</name><regex>test-regex</regex>"
            + "<description>test-description</description>"
            + "<default>test-default-value</default><value>test-value</value>"
            + "</token></filter>", outputter.outputString(node));
    }

    @Test
    public void testApply() throws Exception {
        FilterTask task = new FilterTask();
        FilterToken t =
            new FilterToken("test-name", "d..a", null, null, "value");
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

        FilterToken p =
            new FilterToken("test-name", "test-regex", "test-description",
                "default-value", null);
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

        FilterToken p =
            new FilterToken("test-name", "test-regex", "test-description",
                "default-value", null);
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

        FilterToken p =
            new FilterToken("test-name", "test-regex", "test-description",
                "default-value", "test-value");
        HashSet<FilterToken> list = new HashSet<FilterToken>();
        list.add(p);
        task.setTokens(list);

        boolean result = task.configureInteractively(configurer, true);

        assertTrue(result);
        assertTrue(configurer.isCalled);
        assertEquals(expected, p.getValue());
    }
}
