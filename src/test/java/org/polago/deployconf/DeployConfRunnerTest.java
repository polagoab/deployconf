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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.polago.deployconf.DeployConfRunner.RunMode;
import org.polago.deployconf.group.FileSystemConfigGroupManager;

/**
 * Tests the {@link DeployConfRunner} class.
 */
public class DeployConfRunnerTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void testRunWithIdenticalDeploymentConfig() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        Path srcFile = folder.newFile("input.zip").toPath();
        Path destFile = folder.newFile("output.zip").toPath();
        Path configFile = folder.newFile("config.xml").toPath();

        runner.setDeploymentConfigPath(configFile);

        Files.delete(destFile);
        Files.delete(configFile);

        Files.copy(getClass().getClassLoader().getResourceAsStream("simple-test-expected/deployment-config.xml"),
            configFile);

        TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
        String zipPrefix = "simple-test/";
        String zipExpectedPrefix = "simple-test-expected/";
        String[] zipFiles =
            {"deploy.properties", "logging.xml", "plain.properties", "META-INF/deployment-template.xml"};

        try {
            for (String r : zipFiles) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipPrefix + r);
                assertNotNull("Unable to load resource: " + zipPrefix + r, is);
                os.addStream(is, r);
            }
        } finally {
            os.close();
        }

        int status = runner.run(srcFile.toString(), destFile.toString());
        assertEquals(0, status);
        assertTrue(Files.exists(destFile));

        ZipFile zipDest = new ZipFile(destFile.toString());
        try {
            for (String r : Arrays.copyOf(zipFiles, zipFiles.length - 1)) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipExpectedPrefix + r);
                assertNotNull(zipExpectedPrefix + r, is);
                assertEqualStreamContent(zipExpectedPrefix + r, is, zipDest.getInputStream(new ZipEntry(r)));
            }
            assertNull(zipDest.getEntry(zipFiles[3]));
        } finally {
            zipDest.close();
        }
    }

    @Test
    public void testRunWithoutExistingDeploymentConfig() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        Path srcFile = folder.newFile("input.zip").toPath();
        Path destFile = folder.newFile("output.zip").toPath();
        Path configFile = folder.newFile("config.xml").toPath();

        runner.setDeploymentConfigPath(configFile);

        Files.delete(configFile);
        Files.delete(destFile);

        TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
        String zipPrefix = "simple-test/";
        String zipExpectedPrefix = "simple-test-expected/";
        String[] zipFiles = {"deploy.properties", "logging.xml", "plain.properties", "META-INF/deployment-template.xml",
            "META-INF/MANIFEST.MF"};
        try {
            for (String r : zipFiles) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipPrefix + r);
                assertNotNull("Unable to load resource: " + zipPrefix + r, is);
                os.addStream(is, r);
            }
        } finally {
            os.close();
        }

        int status = runner.run(srcFile.toString(), destFile.toString());
        assertEquals(2, status);
        assertFalse(Files.exists(destFile));
        assertTrue(Files.exists(configFile));
        InputStream destConfigStream = Files.newInputStream(configFile);
        InputStream srcConfigStream = getClass().getClassLoader().getResourceAsStream(zipPrefix + zipFiles[3]);

        try {
            assertEqualStreamContent(zipExpectedPrefix + zipFiles[3], srcConfigStream, destConfigStream);
        } finally {
            destConfigStream.close();
            srcConfigStream.close();
        }
    }

    @Test
    public void testRunWithCompleteNonExistingDeploymentConfig() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        Path srcFile = folder.newFile("input.zip").toPath();
        Path destFile = folder.newFile("output.zip").toPath();
        Path configFile = folder.newFile("config.xml").toPath();

        runner.setDeploymentConfigPath(configFile);

        Files.delete(configFile);
        Files.delete(destFile);

        TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
        String zipPrefix = "simple-test/";
        String zipExpectedPrefix = "simple-test-expected/";
        String deploymentTemplatePath = "complete-deployment-config.xml";
        String[] zipFiles = {"deploy.properties", "logging.xml", "plain.properties", "META-INF/MANIFEST.MF"};
        try {
            for (String r : zipFiles) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipPrefix + r);
                assertNotNull("Unable to load resource: " + zipPrefix + r, is);
                os.addStream(is, r);
            }
            InputStream is = getClass().getClassLoader().getResourceAsStream(deploymentTemplatePath);
            assertNotNull("Unable to load resource: " + deploymentTemplatePath, is);
            os.addStream(is, deploymentTemplatePath);
        } finally {
            os.close();
        }

        runner.setDeploymentTemplatePath(deploymentTemplatePath);
        int status = runner.run(srcFile.toString(), destFile.toString());
        assertEquals(0, status);
        assertTrue(Files.exists(destFile));
        assertTrue(Files.exists(configFile));
        InputStream destConfigStream = Files.newInputStream(configFile);
        InputStream srcConfigStream = getClass().getClassLoader().getResourceAsStream(deploymentTemplatePath);

        try {
            assertEqualStreamContent(deploymentTemplatePath, srcConfigStream, destConfigStream);
        } finally {
            destConfigStream.close();
            srcConfigStream.close();
        }
    }

    @Test
    public void testDeploymentConfigFileWithNoConfigNameAndNoRepo() throws Exception {

        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();
        assertEquals(DeployConfRunner.DEPLOYMENT_CONFIG_SUFFIX,
            runner.getDeploymentConfigPath(config.getName()).toString());
    }

    @Test
    public void testDeploymentConfigFileWithNoConfigNameAndExplicitDeploymentConfig() throws Exception {

        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();
        FileSystem fs = FileSystems.getDefault();
        Path expected = fs.getPath("test.xml");
        runner.setDeploymentConfigPath(expected);
        assertEquals(expected, runner.getDeploymentConfigPath(config.getName()));
    }

    @Test
    public void testDeploymentConfigFileWithNoConfigNameAndExistingRepoDir() throws Exception {

        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();
        Path repoDir = folder.newFolder("repodir").toPath();

        runner.setRepositoryDirectory(repoDir.toString());

        Path expected = repoDir.resolve(FileSystems.getDefault().getPath(DeployConfRunner.DEPLOYMENT_CONFIG_SUFFIX));

        assertEquals(expected, runner.getDeploymentConfigPath(config.getName()));
    }

    @Test
    public void testDeploymentConfigFileWithConfigNameAndNoRepo() throws Exception {

        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();

        String name = "test";
        config.setName(name);

        assertEquals(FileSystems.getDefault().getPath(name + "-" + DeployConfRunner.DEPLOYMENT_CONFIG_SUFFIX),
            runner.getDeploymentConfigPath(config.getName()));
    }

    @Test
    public void testDeploymentConfigFileWithConfigNameAndExplicitDeploymentConfigFile() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();

        String name = "test";
        config.setName(name);
        Path expected = FileSystems.getDefault().getPath("test.xml");
        runner.setDeploymentConfigPath(expected);

        assertEquals(expected, runner.getDeploymentConfigPath(config.getName()));
    }

    @Test
    public void testRepoFileWithConfigNameAndExistingRepoDir() throws Exception {

        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        DeploymentConfig config = new DeploymentConfig();
        String name = "test";
        config.setName(name);
        Path repoDir = folder.newFolder("repodir").toPath();

        runner.setRepositoryDirectory(repoDir.toString());
        Path expected = FileSystems.getDefault().getPath(repoDir.toString(),
            name + "-" + DeployConfRunner.DEPLOYMENT_CONFIG_SUFFIX);

        assertEquals(expected, runner.getDeploymentConfigPath(config.getName()));
    }

    @Test
    public void testRunWithConfigGroup() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(RunMode.NON_INTERACTIVE);
        Path srcFile = folder.newFile("input.zip").toPath();
        Path destFile = folder.newFile("output.zip").toPath();
        Path configFile = folder.newFile("config.xml").toPath();
        Path repoDir = folder.newFolder("repodir").toPath();

        runner.setDeploymentConfigPath(configFile);
        runner.setRepositoryDirectory(repoDir.toString());
        runner.setGroupManager(new FileSystemConfigGroupManager(repoDir));

        Files.delete(configFile);
        Files.delete(destFile);

        Files.copy(
            getClass().getClassLoader().getResourceAsStream("config-group-test/META-INF/deployment-template.xml"),
            configFile);
        Files.copy(
            getClass().getClassLoader().getResourceAsStream("config-group-test/testgroup-config-group.properties"),
            repoDir.resolve("testgroup-config-group.properties"));

        TestZipOutputStream os = new TestZipOutputStream(Files.newOutputStream(srcFile));
        String zipPrefix = "config-group-test/";
        String zipExpectedPrefix = "config-group-test/expected/";
        String[] zipFiles =
            {"deploy.properties", "logging.xml", "plain.properties", "META-INF/deployment-template.xml"};

        try {
            for (String r : zipFiles) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipPrefix + r);
                assertNotNull("Unable to load resource: " + zipPrefix + r, is);
                os.addStream(is, r);
            }
        } finally {
            os.close();
        }

        int status = runner.run(srcFile.toString(), destFile.toString());
        assertEquals(0, status);
        assertTrue(Files.exists(destFile));

        ZipFile zipDest = new ZipFile(destFile.toString());
        try {
            for (String r : Arrays.copyOf(zipFiles, zipFiles.length - 1)) {
                InputStream is = getClass().getClassLoader().getResourceAsStream(zipExpectedPrefix + r);
                assertNotNull(zipExpectedPrefix + r, is);
                assertEqualStreamContent(zipExpectedPrefix + r, is, zipDest.getInputStream(new ZipEntry(r)));
            }
            assertNull(zipDest.getEntry(zipFiles[3]));
        } finally {
            zipDest.close();
        }
    }

    private void assertEqualStreamContent(String msg, InputStream is1, InputStream is2) throws IOException {

        InputStreamReader r1 = new InputStreamReader(is1, "UTF-8");
        StringWriter w1 = new StringWriter();
        InputStreamReader r2 = new InputStreamReader(is2, "UTF-8");
        StringWriter w2 = new StringWriter();

        int c = r1.read();
        while (c != -1) {
            w1.write(c);
            c = r1.read();
        }

        c = r2.read();
        while (c != -1) {
            w2.write(c);
            c = r2.read();
        }

        assertEquals(msg, w1.getBuffer().toString(), w2.getBuffer().toString());
    }

}
