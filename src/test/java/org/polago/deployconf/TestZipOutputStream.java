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

package org.polago.deployconf;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ZipOutputStream tailored for testing.
 */
public class TestZipOutputStream extends ZipOutputStream {

    public TestZipOutputStream(OutputStream outputStream) {
        super(outputStream);
    }

    public void addStream(InputStream stream, String zipPath)
        throws IOException {
        ZipEntry e = new ZipEntry(zipPath);
        putNextEntry(e);
        writeStream(stream);
        closeEntry();
    }

    public void addFile(String fsPath, String zipPath) throws IOException {
        ZipEntry e = new ZipEntry(zipPath);
        putNextEntry(e);
        writeFile(fsPath);
        closeEntry();
    }

    private void writeStream(InputStream stream) throws IOException {
        byte[] buf = new byte[1024];
        int i = stream.read(buf);
        while (i != -1) {
            write(buf, 0, i);
            i = stream.read(buf);
        }
    }

    private void writeFile(String fsPath) throws IOException {
        FileInputStream is = new FileInputStream(fsPath);
        try {
            writeStream(is);
        } finally {
            is.close();
        }
    }
}
