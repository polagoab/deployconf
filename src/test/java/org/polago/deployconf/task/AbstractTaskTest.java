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

package org.polago.deployconf.task;

import static org.junit.Assert.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.polago.deployconf.InteractiveConfigurer;
import org.polago.deployconf.group.ConfigGroup;
import org.polago.deployconf.group.ConfigGroupManager;
import org.polago.deployconf.group.InMemoryConfigGroup;
import org.polago.deployconf.group.InMemoryConfigGroupManager;

/**
 * Tests the {@link AbstractTask} class.
 */
public class AbstractTaskTest {

    class TestAbstractTask extends AbstractTask {

        public TestAbstractTask(ConfigGroupManager groupManager) {
            super(groupManager);
        }

        @Override
        public void merge(Task other) {

        }

        @Override
        public boolean isConfigured() {
            return true;
        }

        @Override
        public boolean configureInteractively(InteractiveConfigurer configurer, boolean force) {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getSerializedName() {
            return "test-task";
        }

        @Override
        public void apply(InputStream source, OutputStream destination) {

        }

    };

    @Test
    public void testDeserialize() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("simple-deployment-config.xml");
        assertNotNull(is);

        SAXBuilder builder = new SAXBuilder();
        Document d = builder.build(is);
        List<Element> tasks = d.getRootElement().getChildren();

        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        task.deserialize(tasks.get(0));
        assertNotNull(task.getPath());
    }

    @Test
    public void testExpandNull() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertNull(task.expandPropertyExpression(null, null));
    }

    @Test
    public void testExpandWithNoExpression() {
        String expected = "text";
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertEquals(expected, task.expandPropertyExpression(expected, null));
    }

    @Test
    public void testExpandWithNonExistingExpression() throws IOException {
        String expected = "prefix-${text}-suffix";
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        ConfigGroup group = new InMemoryConfigGroup();
        group.setProperty("othertext", "expanded-test-value");

        assertEquals(expected, task.expandPropertyExpression(expected, group));
    }

    @Test
    public void testExpandWithExistingExpression() throws IOException {
        String expected = "prefix-${text}-suffix";
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        ConfigGroup group = new InMemoryConfigGroup();
        group.setProperty("text", "expanded-test-value");

        assertEquals("prefix-expanded-test-value-suffix", task.expandPropertyExpression(expected, group));
    }

    @Test
    public void testConditionNull() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertTrue(task.evaluateCondition(null, null));
    }

    @Test
    public void testConditionFalse() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertFalse(task.evaluateCondition("false", null));
    }

    @Test
    public void testConditionTrue() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertTrue(task.evaluateCondition("true", null));
    }

    @Test
    public void testConditionEqualStrings() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertTrue(task.evaluateCondition("'v' == 'v'", null));
    }

    @Test
    public void testConditionNonEqualStrings() {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        assertFalse(task.evaluateCondition("'v' == 's'", null));
    }

    @Test
    public void testConditionEqualExpandedExpression() throws IOException {
        TestAbstractTask task = new TestAbstractTask(new InMemoryConfigGroupManager());
        ConfigGroup group = new InMemoryConfigGroup();
        group.setProperty("text", "expanded-test-value");
        assertTrue(task.evaluateCondition("'expanded-test-value' == '${text}'", group));
    }

}
