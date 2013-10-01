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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Main Runner class.
 */
public class DeployConfRunner {

    private static Logger logger = LoggerFactory
        .getLogger(DeployConfRunner.class);

    private final boolean interactive;

    private String deploymentTemplate = "META-INF/deployment-template.xml";

    private File deploymentConfig = new File("deployment-config.xml");

    /**
     * Public Constructor.
     *
     * @param interactive determine if the program should be running in
     * interactive mode or not
     */
    public DeployConfRunner(boolean interactive) {
        this.interactive = interactive;
    }

    /**
     * Main entry point.
     *
     * @param args the runtime program arguments
     */
    public static void main(String[] args) {
        Options options = new Options();

        Option help =
            new Option("h", "help", false, "Display usage information");
        options.addOption(help);

        Option version =
            new Option("v", "version", false,
                "Display version information and exit");
        options.addOption(version);

        Option interactive =
            new Option("i", "interactive", false, "Run in interactive mode");
        options.addOption(interactive);

        Option debug =
            new Option("d", "debug", false, "Print Debug Information");
        options.addOption(debug);

        boolean debugEnabled = false;

        CommandLineParser parser = new GnuParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            ProjectProperties projectProperties = getProjectProperties();

            if (cmd.hasOption(version.getOpt())) {
                System.out.print(projectProperties.getName());
                System.out.print(" version ");
                System.out.println(projectProperties.getVersion());
                System.out.println(projectProperties.getCopyrightMessage());
                System.exit(0);
            }

            if (cmd.hasOption(help.getOpt())) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(projectProperties.getName()
                    + " [OPTION]... <INPUT> <OUTPUT>",
                    projectProperties.getHelpHeader(), options, "");
                System.exit(0);
            }

            if (cmd.hasOption(debug.getOpt())) {
                logger.info("Activating Debug Logging");
                debugEnabled = true;
                LoggerContext loggerContext =
                    (LoggerContext) LoggerFactory.getILoggerFactory();

                try {
                    JoranConfigurator configurator = new JoranConfigurator();
                    configurator.setContext(loggerContext);
                    loggerContext.reset();
                    configurator.doConfigure(Thread.currentThread()
                        .getContextClassLoader()
                        .getResource("logback-debug.xml"));
                } catch (JoranException e) {
                    logger.warn("Error activating debug logging", e);
                }
                StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
            }

            DeployConfRunner instance =
                new DeployConfRunner(cmd.hasOption(interactive.getOpt()));
            @SuppressWarnings("unchecked")
            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                System.out.println("usage: " + projectProperties.getName()
                    + " <INPUT> <OUTPUT>");
                System.exit(1);
            }
            System.exit(instance.run(argList.get(0), argList.get(1)));
        } catch (ParseException e) {
            logger.error("Command Line Parse Error: " + e.getMessage(), e);
        } catch (Exception e) {
            String msg = "Internal Error: " + e.toString();
            if (!debugEnabled) {
                msg += "\n(use the -d option to print stacktraces)";
            }
            logger.error(msg, e);
        }
    }

    /**
     * Gets the Project Properteis for this program.
     *
     * @return the Project Properteis for this program
     * @throws IOException indicating failure to load properties
     */
    private static ProjectProperties getProjectProperties() throws IOException {
        return ProjectProperties.instance();
    }

    /**
     * Run this program.
     *
     * @param source the input file
     * @param destination the destination file
     * @return the exit status
     * @throws Exception indicating processing error
     */
    public int run(String source, String destination) throws Exception {
        int result = 0;
        DeploymentConfig template =
            getDeploymentConfig(getInputStreamFromZipFile(new File(source),
                deploymentTemplate));
        DeploymentConfig config = null;
        if (deploymentConfig.exists()) {
            logger.info("Loading Deployment Config from: "
                + deploymentConfig.getPath());
            config =
                getDeploymentConfig(getInputStreamFromFile(deploymentConfig));
        } else {
            logger.info("Creating new Deployment Config: "
                + deploymentConfig.getPath());
            config = new DeploymentConfig();
        }

        if (config.merge(template)) {
            apply(config, source, destination);
        } else {
            // Needs manual merge
            if (interactive && config.interactiveMerge()) {
                save(config);
                apply(config, source, destination);
            } else {
                System.err.println("Template configuration " + "has changed.");
                System.err.println("Edit '" + deploymentConfig
                    + "' and make sure that each "
                    + "deployment property has a valid value");
                save(config);
                result = 2;
            }
        }

        return result;
    }

    /**
     * Gets the deploymentTemplate property value.
     *
     * @return the current value of the deploymentTemplate property
     */
    public String getDeploymentTemplate() {
        return deploymentTemplate;
    }

    /**
     * Sets the deploymentTemplate property.
     *
     * @param deploymentTemplate the new property value
     */
    public void setDeploymentTemplate(String deploymentTemplate) {
        this.deploymentTemplate = deploymentTemplate;
    }

    /**
     * Gets the deploymentConfig property value.
     *
     * @return the current value of the deploymentConfig property
     */
    public File getDeploymentConfig() {
        return deploymentConfig;
    }

    /**
     * Sets the deploymentConfig property.
     *
     * @param deploymentConfig the new property value
     */
    public void setDeploymentConfig(File deploymentConfig) {
        this.deploymentConfig = deploymentConfig;
    }

    /**
     * Apply the given DeploymentConfig to the source and create the
     * destination.
     *
     * @param config the DeploymentConfig to apply
     * @param source the input file
     * @param destination the destination file
     * @throws Exception indicating processing error
     */
    private void apply(DeploymentConfig config, String source,
        String destination) throws Exception {

        File sourceFile = new File(source);
        File destFile = new File(destination);

        FileInputStream srcStream = null;
        FileOutputStream destStream = null;

        try {
            srcStream = new FileInputStream(sourceFile);
            destStream = new FileOutputStream(destFile);
            config.apply(srcStream, destStream, getDeploymentTemplate());
        } finally {
            if (srcStream != null) {
                srcStream.close();
            }

            if (destStream != null) {
                destStream.close();
            }
        }
    }

    /**
     * Save the DeploymentConfig to the configured persistent storage.
     *
     * @param config the DeploymentConfig to save
     * @throws IOException indicating IO error
     */
    private void save(DeploymentConfig config) throws IOException {
        FileOutputStream os = new FileOutputStream(deploymentConfig);
        config.save(os);
        os.close();
    }

    /**
     * Gets a InputStream instance from a plain file.
     *
     * @param file the path to the file
     * @return InputStream representing the file
     * @throws IOException indicating IO error
     */
    private InputStream getInputStreamFromFile(File file) throws IOException {

        return new FileInputStream(file);
    }

    /**
     * Gets a InputStream for a path in a Zip Archive.
     *
     * @param file the Zip Archive to use
     * @param path the path to the file in the Zip Archive
     * @return InputStream representing the path
     * @throws IOException indicating IO error
     */
    private InputStream getInputStreamFromZipFile(File file, String path)
        throws IOException {

        ZipFile zipFile = new ZipFile(file);
        ZipEntry entry = zipFile.getEntry(path);

        return zipFile.getInputStream(entry);
    }

    /**
     * Gets a DeploymentConfig instance from a InputStream.
     * <p>
     * Note that the InputStream is closed after successful invocation.
     *
     * @param is the InpoutStream to use
     * @return a DeploymentConfig representation of the InputStream
     * @throws Exception indicating error
     */
    private DeploymentConfig getDeploymentConfig(InputStream is)
        throws Exception {

        DeploymentReader reader = new DeploymentReader(is);
        DeploymentConfig result = reader.parse();
        is.close();

        return result;
    }
}
