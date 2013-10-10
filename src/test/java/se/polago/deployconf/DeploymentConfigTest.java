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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipFile;

import org.junit.Test;

/**
 * Tests the {@link DeploymentConfig} class.
 */
public class DeploymentConfigTest {

    @Test
    public void testMergeEmptyConfigAndTemplateWithNewTasks() {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestTask task = new TestTask();
        template.addTask(task);
        assertFalse(config.merge(template));
        assertEquals(1, config.getTasks().size());
        assertEquals(task, config.getTasks().get(0));
    }

    @Test
    public void testMergeConfigAndTemplateWithSameConfiguredTasks() {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestTask task = new TestTask();
        task.configured = true;

        config.addTask(task);
        template.addTask(task);

        assertTrue(config.merge(template));
        assertEquals(1, config.getTasks().size());
        assertEquals(task, config.getTasks().get(0));
    }

    @Test
    public void testMergeConfigAndTemplateWithSameUnConfiguredTasks() {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestTask task = new TestTask();

        config.addTask(task);
        template.addTask(task);

        assertFalse(config.merge(template));
        assertEquals(1, config.getTasks().size());
        assertEquals(task, config.getTasks().get(0));
    }

    @Test
    public void testMergeConfigAndTemplateWithUnConfiguredTasks() {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestTask ununsedTask = new TestTask();
        TestTask task = new TestTask();

        config.addTask(ununsedTask);
        template.addTask(task);

        assertFalse(config.merge(template));
        assertEquals(1, config.getTasks().size());
        assertEquals(task, config.getTasks().get(0));
    }

    @Test
    public void testSuccessfulInteractiveMerge() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();
        TestTask task = new TestTask();
        task.interactive = true;
        template.addTask(task);
        assertFalse(config.merge(template));
        assertTrue(config.interactiveMerge(configurer, false));
        assertTrue(task.isconfigureInteractivelyCalled);
    }

    @Test
    public void testUnsuccessfulInteractiveMerge() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();
        TestTask task = new TestTask();
        template.addTask(task);
        assertFalse(config.merge(template));
        assertFalse(config.interactiveMerge(configurer, false));
        assertTrue(task.isconfigureInteractivelyCalled);
    }

    @Test
    public void testSuccessfulForceInteractiveMerge() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestInteractiveConfigurer configurer = new TestInteractiveConfigurer();
        TestTask task = new TestTask();
        task.interactive = true;
        task.configured = true;
        template.addTask(task);
        assertTrue(config.merge(template));
        assertTrue(config.interactiveMerge(configurer, true));
        assertTrue(task.isconfigureInteractivelyCalled);

    }

    @Test
    public void testSave() throws IOException {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        config.addTask(task);
        File file = File.createTempFile("test", ".xml");
        try {
            OutputStream os = new FileOutputStream(file);
            config.save(os);
            assertTrue(file.length() > 0);
        } finally {
            file.delete();
        }
    }

    @Test
    public void testApplyMatchingPath() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        String zipPath = "deploy.properties";
        task.path = zipPath;
        config.addTask(task);
        File srcFile = File.createTempFile("input", ".zip");
        File destFile = File.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os =
                new TestZipOutputStream(new FileOutputStream(srcFile));
            InputStream is =
                getClass().getClassLoader().getResourceAsStream(
                    "simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            os.close();
            InputStream src = new FileInputStream(srcFile);
            FileOutputStream dest = new FileOutputStream(destFile);
            config.apply(src, dest, null);
            assertTrue(task.applied);
            ZipFile destZipFile = new ZipFile(destFile);
            assertNotNull(destZipFile.getEntry(zipPath));
        } finally {
            srcFile.delete();
            destFile.delete();
        }
    }

    @Test
    public void testApplyNoMatchingPath() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        String zipPath = "deploy.properties";
        task.path = "nodeploy.properties";
        config.addTask(task);
        File srcFile = File.createTempFile("input", ".zip");
        File destFile = File.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os =
                new TestZipOutputStream(new FileOutputStream(srcFile));
            InputStream is =
                getClass().getClassLoader().getResourceAsStream(
                    "simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            os.close();
            InputStream src = new FileInputStream(srcFile);
            FileOutputStream dest = new FileOutputStream(destFile);
            config.apply(src, dest, null);
            assertFalse(task.applied);
            ZipFile destZipFile = new ZipFile(destFile);
            assertNotNull(destZipFile.getEntry(zipPath));
        } finally {
            srcFile.delete();
            destFile.delete();
        }
    }

    @Test
    public void testApplyAndIgnoringPath() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        String zipPath = "deploy.properties";
        String ignorePath = "logging.xml";
        task.path = "nodeploy.properties";
        config.addTask(task);
        File srcFile = File.createTempFile("input", ".zip");
        File destFile = File.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os =
                new TestZipOutputStream(new FileOutputStream(srcFile));
            InputStream is =
                getClass().getClassLoader().getResourceAsStream(
                    "simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            is =
                getClass().getClassLoader().getResourceAsStream(
                    "simple-test/" + ignorePath);
            os.addStream(is, ignorePath);
            os.close();
            InputStream src = new FileInputStream(srcFile);
            FileOutputStream dest = new FileOutputStream(destFile);
            config.apply(src, dest, zipPath);
            assertFalse(task.applied);
            ZipFile destZipFile = new ZipFile(destFile);
            assertNull(destZipFile.getEntry(zipPath));
        } finally {
            srcFile.delete();
            destFile.delete();
        }
    }

}
