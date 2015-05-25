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

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;

import jline.console.ConsoleReader;

/**
 * Interactive Configurer that ask the user for the value.
 */
public class ConsoleInteractiveConfigurer implements InteractiveConfigurer {

    private final ConsoleReader reader;

    private final PrintWriter writer;

    /**
     * Public Constructor.
     *
     * @throws IOException indicating processing failure
     */
    public ConsoleInteractiveConfigurer() throws IOException {
        reader = new ConsoleReader();
        reader.setBellEnabled(false);
        writer = new PrintWriter(new OutputStreamWriter(System.out, Charset.defaultCharset()), true);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IOException indicating processing failure
     */
    @Override
    public String configure(String name, String description, String defaultValue) throws IOException {

        String value = null;

        writer.println();
        writer.println(description.replaceAll("(?m)^[\\s&&[^\\r\\n]]+", ""));
        writer.println();

        StringBuilder prompt = new StringBuilder(name);
        if (defaultValue != null) {
            prompt.append("[");
            prompt.append(defaultValue);
            prompt.append("]");
        }
        prompt.append("=");
        while (value == null || value.length() == 0) {
            value = reader.readLine(prompt.toString());
            if (value == null || value.trim().length() == 0) {
                value = defaultValue;
            } else {
                value = value.trim();
            }

            if (value != null && value.length() > 0) {
                writer.println();
                writer.print("==> '");
                writer.print(value);
                writer.println("'");
                writer.println();
            }
        }
        return value;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PrintWriter getWriter() {
        return writer;
    }

}
