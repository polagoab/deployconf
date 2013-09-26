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
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.junit.Test;

/**
 * Tests the {@link DeployConfRunner} class.
 */
public class DeployConfRunnerTest {

    @Test
    public void testRunWithIdenticalDeploymentConfig() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(false);
        File srcFile = File.createTempFile("input", ".zip");
        File destFile = File.createTempFile("output", ".zip");
        File configFile = File.createTempFile("config", ".xml");
        runner.setDeploymentConfig(configFile);
        destFile.delete();

        try {
            copyStream(
                getClass().getClassLoader().getResourceAsStream(
                    "simple-test-expected/deployment-config.xml"), configFile);

            TestZipOutputStream os =
                new TestZipOutputStream(new FileOutputStream(srcFile));
            String zipPrefix = "simple-test/";
            String zipExpectedPrefix = "simple-test-expected/";
            String[] zipFiles =
                {"deploy.properties", "logging.xml", "plain.properties",
                    "META-INF/deployment-template.xml"};
            for (String r : zipFiles) {
                InputStream is =
                    getClass().getClassLoader().getResourceAsStream(
                        zipPrefix + r);
                assertNotNull(is);
                os.addStream(is, r);
            }

            os.close();
            int status = runner.run(srcFile.getPath(), destFile.getPath());
            assertEquals(0, status);
            assertTrue(destFile.exists());

            ZipFile zipDest = new ZipFile(destFile);
            for (String r : Arrays.copyOf(zipFiles, zipFiles.length - 1)) {
                InputStream is =
                    getClass().getClassLoader().getResourceAsStream(
                        zipExpectedPrefix + r);
                assertNotNull(zipExpectedPrefix + r, is);
                assertEqualStreamContent(zipExpectedPrefix + r, is,
                    zipDest.getInputStream(new ZipEntry(r)));
            }
            assertNull(zipDest.getEntry(zipFiles[3]));
        } finally {
            srcFile.delete();
            destFile.delete();
            configFile.delete();
        }
    }

    @Test
    public void testRunWithoutExistingDeploymentConfig() throws Exception {
        DeployConfRunner runner = new DeployConfRunner(false);
        File srcFile = File.createTempFile("input", ".zip");
        File destFile = File.createTempFile("output", ".zip");
        File configFile = File.createTempFile("config", ".xml");
        runner.setDeploymentConfig(configFile);
        configFile.delete();
        destFile.delete();
        assertFalse(configFile.exists());

        try {
            TestZipOutputStream os =
                new TestZipOutputStream(new FileOutputStream(srcFile));
            String zipPrefix = "simple-test/";
            String zipExpectedPrefix = "simple-test-expected/";
            String[] zipFiles =
                {"deploy.properties", "logging.xml", "plain.properties",
                    "META-INF/deployment-template.xml", "META-INF/MANIFEST.MF"};
            for (String r : zipFiles) {
                InputStream is =
                    getClass().getClassLoader().getResourceAsStream(
                        zipPrefix + r);
                assertNotNull(is);
                os.addStream(is, r);
            }

            os.close();
            int status = runner.run(srcFile.getPath(), destFile.getPath());
            assertEquals(2, status);
            assertFalse(destFile.exists());
            assertTrue(configFile.exists());
            InputStream destConfigStream = new FileInputStream(configFile);
            InputStream srcConfigStream =
                getClass().getClassLoader().getResourceAsStream(
                    zipPrefix + zipFiles[3]);
            assertEqualStreamContent(zipExpectedPrefix + zipFiles[3],
                srcConfigStream, destConfigStream);
        } finally {
            srcFile.delete();
            destFile.delete();
            configFile.delete();
        }
    }

    private void assertEqualStreamContent(String msg, InputStream is1,
        InputStream is2) throws IOException {

        int i1 = is1.read();
        int i2 = is2.read();
        assertEquals(msg, i1, i2);
        while (i1 != -1 && i2 != -1) {
            i1 = is1.read();
            i2 = is2.read();
            assertEquals(msg, i1, i2);
        }
        assertEquals(msg, i1, i2);
    }

    private void copyStream(InputStream stream, File configFile)
        throws IOException {
        FileOutputStream out = new FileOutputStream(configFile);
        try {
            byte[] buf = new byte[1024];
            int i = stream.read(buf);
            while (i != -1) {
                out.write(buf, 0, i);
                i = stream.read(buf);
            }
        } finally {
            out.close();
        }
    }
}
