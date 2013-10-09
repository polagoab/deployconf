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

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Interactive Configurer that ask the user for the value.
 */
public interface InteractiveConfigurer {

    /**
     * Configure an item.
     *
     * @param name the name of the item to be configured
     * @param description the item description
     * @param defaultValue the item default value
     * @return the configured value or null indicating that the item wasn't
     * configured
     * @throws IOException indicating IO failure
     */
    String configure(String name, String description, String defaultValue)
        throws IOException;

    /**
     * Gets the PrintWriter to use when interacting with a user.
     *
     * @return a PrintWriter instance
     */
    PrintWriter getWriter();
}
