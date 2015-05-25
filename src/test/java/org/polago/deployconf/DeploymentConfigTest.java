/**
 * Copyright (c) 2013-2014 Polago AB
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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
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
        assertFalse(task.merged);
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
        assertTrue(task.merged);
        assertEquals(1, config.getTasks().size());
        assertEquals(task, config.getTasks().get(0));
    }

    @Test
    public void testMergeConfigAndTemplateWithEqualTasks() {
        DeploymentConfig config = new DeploymentConfig();
        DeploymentConfig template = new DeploymentConfig();
        TestTask task = new TestTask();
        task.configured = true;

        TestTask task2 = new TestTask();
        task2.configured = false;

        config.addTask(task);
        template.addTask(task2);

        assertTrue(config.merge(template));
        assertTrue(task.merged);
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
        assertTrue(task.merged);
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
        assertFalse(task.merged);
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
        Path file = Files.createTempFile("test", ".xml");
        try {
            OutputStream os = Files.newOutputStream(file);
            config.save(os);
            assertTrue(Files.size(file) > 0);
        } finally {
            Files.delete(file);
        }
    }

    @Test
    public void testApplyMatchingPath() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        String zipPath = "deploy.properties";
        task.path = zipPath;
        config.addTask(task);
        Path srcFile = Files.createTempFile("input", ".zip");
        Path destFile = Files.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
            InputStream is = getClass().getClassLoader().getResourceAsStream("simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            os.close();
            InputStream src = Files.newInputStream(srcFile);
            OutputStream dest = Files.newOutputStream(destFile);
            config.apply(src, dest, null);
            assertTrue(task.applied);
            ZipFile destZipFile = new ZipFile(destFile.toFile());
            assertNotNull(destZipFile.getEntry(zipPath));
            destZipFile.close();
        } finally {
            Files.delete(srcFile);
            Files.delete(destFile);
        }
    }

    @Test
    public void testApplyNoMatchingPath() throws Exception {
        DeploymentConfig config = new DeploymentConfig();
        TestTask task = new TestTask();
        String zipPath = "deploy.properties";
        task.path = "nodeploy.properties";
        config.addTask(task);
        Path srcFile = Files.createTempFile("input", ".zip");
        Path destFile = Files.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
            InputStream is = getClass().getClassLoader().getResourceAsStream("simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            os.close();
            InputStream src = Files.newInputStream(srcFile);
            OutputStream dest = Files.newOutputStream(destFile);
            config.apply(src, dest, null);
            assertFalse(task.applied);
            ZipFile destZipFile = new ZipFile(destFile.toFile());
            assertNotNull(destZipFile.getEntry(zipPath));
            destZipFile.close();
        } finally {
            Files.delete(srcFile);
            Files.delete(destFile);
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
        Path srcFile = Files.createTempFile("input", ".zip");
        Path destFile = Files.createTempFile("output", ".zip");

        try {
            TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
            InputStream is = getClass().getClassLoader().getResourceAsStream("simple-test/" + zipPath);
            assertNotNull(is);
            os.addStream(is, zipPath);
            is = getClass().getClassLoader().getResourceAsStream("simple-test/" + ignorePath);
            os.addStream(is, ignorePath);
            os.close();
            InputStream src = Files.newInputStream(srcFile);
            OutputStream dest = Files.newOutputStream(destFile);
            config.apply(src, dest, zipPath);
            assertFalse(task.applied);
            ZipFile destZipFile = new ZipFile(destFile.toFile());
            assertNull(destZipFile.getEntry(zipPath));
            destZipFile.close();
        } finally {
            Files.delete(srcFile);
            Files.delete(destFile);
        }
    }

}
