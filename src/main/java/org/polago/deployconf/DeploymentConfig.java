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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.polago.deployconf.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes a DeploymentConfiguration loaded from an InputStream.
 */
public class DeploymentConfig {

    private static Logger logger = LoggerFactory
        .getLogger(DeploymentConfig.class);

    private static final int BUF_SIZE = 1024;

    private final List<Task> tasks;

    private String name;

    /**
     * Public Constructor.
     */
    public DeploymentConfig() {
        tasks = new ArrayList<Task>();
    }

    /**
     * Gets the name property value.
     *
     * @return the current value of the name property
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name property.
     *
     * @param name the new property value
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Perform an interactive post merge operation.
     * <p>
     * For each non-configured Task, ask the user to configure the task.
     *
     * @param configurer the IntercativeConfigurer to use
     * @param forceInteractive if true, all tasks will be considered not
     * configured
     * @return true if the user successfully configured all non-configured
     * Tasks
     * @throws Exception indicating processing failure
     */
    public boolean interactiveMerge(InteractiveConfigurer configurer,
        boolean forceInteractive) throws Exception {

        if (forceInteractive) {
            logger.debug("Configure all Tasks interactively");
        } else {
            logger.debug("Configure Tasks interactively");
        }

        boolean result = true;

        printIntercativePreamble(configurer.getWriter());

        for (Task t : tasks) {
            if (forceInteractive || !t.isConfigured()) {
                boolean tr =
                    t.configureInteractively(configurer, forceInteractive);
                if (tr == false) {
                    result = false;
                }
            }
        }

        return result;
    }

    /**
     * Print a preamble to prepare the user to configure interactively.
     *
     * @param writer the PrintWriter to use
     */
    private void printIntercativePreamble(PrintWriter writer) {
        writer.println();
        writer.println("One or more configuration properties needs a value");
        writer.println();
    }

    /**
     * Merge the template configuration into this instance.
     *
     * @param template the template configuration to merge
     * @return true if the merge was successful, ie all configurations has a
     * value.
     */
    public boolean merge(DeploymentConfig template) {

        setName(template.getName());

        // Remove all tasks that isn't available in the template since they are
        // not used anymore
        tasks.retainAll(template.tasks);

        // Add any new task from the template
        for (Task t : template.tasks) {
            if (!tasks.contains(t)) {
                tasks.add(t);
            }
        }

        // Determine if any task lacks a configuration value
        for (Task t : tasks) {
            if (!t.isConfigured()) {
                return false;
            }
        }

        return true;
    }

    /**
     * Gets the tasks property value.
     *
     * @return the current value of the tasks property
     */
    public List<Task> getTasks() {
        return tasks;
    }

    /**
     * Add a task to this DeploymentConfig.
     *
     * @param task the Task to add
     */
    public void addTask(Task task) {
        tasks.add(task);
    }

    /**
     * Determine if this DeploymentConfig has no Tasks.
     *
     * @return true if this DeploymentConfig doent'y have any Tasks
     */
    public boolean isEmpty() {
        return tasks.isEmpty();
    }

    /**
     * Save this DeploymentConfig to persistent storage.
     *
     * @param outputStream the stream to save this DeploymentConfig into
     * @throws IOException indicating failure
     */
    public void save(OutputStream outputStream) throws IOException {
        DeploymentWriter writer = new DeploymentWriter(outputStream);
        writer.persist(this);
    }

    /**
     * Apply this DeploymentConfig to the destination using source as input.
     *
     * @param srcStream the InputStream file to use
     * @param destStream the OutputStream file to use
     * @param ignorePath a zip path to ignore
     * @throws Exception indicating IO error
     */
    public void apply(InputStream srcStream, OutputStream destStream,
        String ignorePath) throws Exception {

        ZipInputStream srcZipStream = new ZipInputStream(srcStream);
        ZipOutputStream destZipStream = new ZipOutputStream(destStream);

        ZipEntry e = srcZipStream.getNextEntry();

        Map<String, List<Task>> taskMap = getTaskMap();
        boolean entryWritten = false;
        while (e != null) {
            if (e.getName().equals(ignorePath)) {
                logger.debug("Ignoring Zip Entry: " + e);
                e = srcZipStream.getNextEntry();
                continue;
            }
            destZipStream.putNextEntry(createZipEntry(e));
            List<Task> taskList = taskMap.get(e.getName());
            if (taskList != null) {
                applyZipEntry(e, taskList, srcZipStream, destZipStream);
            } else {
                copyZipEntry(e, srcZipStream, destZipStream);
            }
            e = srcZipStream.getNextEntry();
            entryWritten = true;
        }
        if (entryWritten) {
            destZipStream.closeEntry();
        }
        destZipStream.finish();
    }

    /**
     * Create a new ZipEntry preserving relevant fields.
     * <p>
     * Just reusing the original entry seems to produce a corrupt zip file
     *
     * @param e the ZipEntry to use
     * @return a new ZipEntry based on e
     */
    private ZipEntry createZipEntry(ZipEntry e) {
        ZipEntry result = new ZipEntry(e.getName());
        result.setComment(e.getComment());
        byte[] extra = e.getExtra();
        if (extra != null) {
            result.setExtra(extra.clone());
        }
        result.setTime(e.getTime());

        return result;
    }

    /**
     * Copy the given ZipEntry from src to dest.
     *
     * @param e the ZipEnrty to copy
     * @param src the source ZipInputStream
     * @param dest the destination ZipOutputStream
     * @throws IOException indicating IO error
     */
    private void copyZipEntry(ZipEntry e, ZipInputStream src,
        ZipOutputStream dest) throws IOException {

        logger.debug("Copying Zip Entry: " + e);

        byte[] buf = new byte[BUF_SIZE];
        int i = src.read(buf);
        while (i != -1) {
            dest.write(buf, 0, i);
            i = src.read(buf);
        }
    }

    /**
     * Apply given Tasks to a ZipEntry.
     *
     * @param e the ZipEntry to use
     * @param taskList the list of task to apply to the ZipEntry
     * @param zipSrc the ZipInputStream file to use
     * @param zipDest the ZipOutputStream file to use
     * @throws Exception indicating processing error
     */
    private void applyZipEntry(ZipEntry e, List<Task> taskList,
        ZipInputStream zipSrc, ZipOutputStream zipDest) throws Exception {

        logger.info("Applying deployment config to Zip Entry: " + e);
        for (Task t : taskList) {
            t.apply(zipSrc, zipDest);
        }
    }

    /**
     * Create a Map with the path as key and a list of Tasks as value from the
     * list of Tasks.
     *
     * @return a Map of available tasks for each path
     */
    private Map<String, List<Task>> getTaskMap() {
        Map<String, List<Task>> result = new HashMap<String, List<Task>>();

        for (Task t : getTasks()) {
            String path = t.getPath();
            List<Task> taskList = result.get(path);
            if (taskList == null) {
                taskList = new ArrayList<Task>();
                result.put(path, taskList);
            }
            taskList.add(t);
        }

        return result;
    }

}
