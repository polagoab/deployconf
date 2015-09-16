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

package org.polago.deployconf.task;

import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.junit.Test;
import org.polago.deployconf.InteractiveConfigurer;

/**
 * Tests the {@link AbstractTask} class.
 */
public class AbstractTaskTest {

    class TestAbstractTask extends AbstractTask {

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

        TestAbstractTask task = new TestAbstractTask();
        task.deserialize(tasks.get(0), null);
        assertNotNull(task.getPath());
    }

}
