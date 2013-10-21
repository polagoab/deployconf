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

    /**
     * Available RunModes.
     */
    enum RunMode {
        // Never prompt the user
        NON_INTERACTIVE,
        // Prompt the user for non-configured tasks
        INTERACTIVE,
        // Prompt the user for all tasks
        FORCE_INTERACTIVE
    };

    /**
     * The Environment Variable used to set the local repository for storing
     * config files. This may be overridden by command line options.
     */
    private static final String ENV_DEPLOYCONF_REPO = "DEPLOYCONF_REPO";

    /**
     * The default deployment template path to use.
     */
    private static final String DEFAULT_TEMPLATE_PATH =
        "META-INF/deployment-template.xml";

    /**
     * The deployment config file suffix to use when creating deploymentConfig
     * paths.
     */
    protected static final String DEPLOYMENT_CONFIG_SUFFIX =
        "deployment-config.xml";

    /**
     * The RunMode to use.
     */
    private final RunMode runMode;

    /**
     * The Zip Path to the deployment template.
     */
    private String deploymentTemplatePath = DEFAULT_TEMPLATE_PATH;

    /**
     * The explicit deployment config file to use. This is normally null.
     */
    private File deploymentConfigFile = null;

    /**
     * The local repository for storing deployment config files. Null means
     * current directory.
     */
    private String repositoryDirectory = null;

    /**
     * Public Constructor.
     *
     * @param runMode how the program should interact with the user
     */
    public DeployConfRunner(RunMode runMode) {
        this.runMode = runMode;
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

        Option forceInteractive =
            new Option("I", "force-interactive", false,
                "Run in interactive mode and configure all tasks");
        options.addOption(forceInteractive);

        Option debug =
            new Option("d", "debug", false, "Print Debug Information");
        options.addOption(debug);

        boolean debugEnabled = false;

        Option repoDir =
            new Option("r", "repo", true,
                "Repository directory to use for storing deployment configs");
        options.addOption(repoDir);

        Option configFile =
            new Option("f", "deployment-config-file", true,
                "File to use for storing the deployment config");
        options.addOption(configFile);

        Option templatePath =
            new Option("t", "deployment-template-path", true,
                "Path to use for locating the deployment template in the "
                    + "<INPUT> file. Default is '" + DEFAULT_TEMPLATE_PATH
                    + "'");
        options.addOption(templatePath);

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

            RunMode mode = RunMode.NON_INTERACTIVE;
            if (cmd.hasOption(forceInteractive.getOpt())) {
                mode = RunMode.FORCE_INTERACTIVE;
            } else if (cmd.hasOption(interactive.getOpt())) {
                mode = RunMode.INTERACTIVE;
            }

            DeployConfRunner instance = new DeployConfRunner(mode);

            String envRepoDir =
                instance.getRepositoryDirectoryFromEnvironment();

            if (cmd.hasOption(repoDir.getOpt())) {
                String rd = cmd.getOptionValue(repoDir.getOpt());
                logger.debug("Using repository directory: {}", rd);
                instance.setRepositoryDirectory(rd);
            } else if (envRepoDir != null) {
                logger.debug(
                    "Using repository directory from environment {}: {}",
                    ENV_DEPLOYCONF_REPO, envRepoDir);
                instance.setRepositoryDirectory(envRepoDir);
            } else {
                logger.debug("Using current working directory as repository");
            }

            if (cmd.hasOption(configFile.getOpt())) {
                String f = cmd.getOptionValue(configFile.getOpt());
                logger.debug("Using explicit deployment file: {}", f);
                instance.setDeploymentConfigFile(new File(f));
            }

            if (cmd.hasOption(templatePath.getOpt())) {
                String path = cmd.getOptionValue(templatePath.getOpt());
                logger.debug("Using deployment template path: {}", path);
                instance.setDeploymentTemplatePath(path);
            }

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
                deploymentTemplatePath));
        DeploymentConfig config = null;
        File repoFile = getDeploymentConfigFile(template.getName());
        if (repoFile.exists()) {
            logger.info("Loading Deployment Configuration from: "
                + repoFile.getPath());
            config = getDeploymentConfig(getInputStreamFromFile(repoFile));
        } else {
            logger.info("Creating new Deployment Config: "
                + repoFile.getPath());
            config = new DeploymentConfig();
        }

        logger.debug("Running in mode: {}", runMode);

        if (config.merge(template) && !(runMode == RunMode.FORCE_INTERACTIVE)) {
            apply(config, source, destination);
        } else {
            // Needs manual merge
            boolean interactive =
                runMode == RunMode.INTERACTIVE
                    || runMode == RunMode.FORCE_INTERACTIVE;
            if (interactive
                && config.interactiveMerge(newInteractiveConfigurer(),
                    runMode == RunMode.FORCE_INTERACTIVE)) {
                save(config);
                apply(config, source, destination);
            } else {
                save(config);
                System.err.println("Deployment Configuration is incomplete");
                System.err.println("Rerun in interactive mode "
                    + "by using the '-i' option");
                System.err.println(" or");
                System.err.println("Edit '" + repoFile
                    + "' and make sure that each "
                    + "deployment property has a valid value");
                result = 2;
            }
        }

        return result;
    }

    /**
     * Gets the deploymentTemplatePath property value.
     *
     * @return the current value of the deploymentTemplatePath property
     */
    public String getDeploymentTemplatePath() {
        return deploymentTemplatePath;
    }

    /**
     * Sets the deploymentTemplatePath property.
     *
     * @param deploymentTemplatePath the new property value
     */
    public void setDeploymentTemplatePath(String deploymentTemplatePath) {
        this.deploymentTemplatePath = deploymentTemplatePath;
    }

    /**
     * Sets the deploymentConfigFile property.
     *
     * @param deploymentConfigFile the new property value
     */
    public void setDeploymentConfigFile(File deploymentConfigFile) {
        this.deploymentConfigFile = deploymentConfigFile;
    }

    /**
     * Gets the File to use for storing the DeploymentConfig.
     * <p>
     * Unless an explicit deployment config File is set, the File is created in
     * the configured repository and based on the config name. If no repository
     * is set, the current working directory is used.
     *
     * @param name the DeploymentConfig name to use
     * @return the current value of the deploymentConfig property
     */
    public File getDeploymentConfigFile(String name) {
        File result = deploymentConfigFile;

        if (deploymentConfigFile != null) {
            result = deploymentConfigFile;
        } else {
            if (name != null) {
                result =
                    new File(getRepositoryDirectory(), name + "-"
                        + DEPLOYMENT_CONFIG_SUFFIX);
            } else {
                result =
                    new File(getRepositoryDirectory(),
                        DEPLOYMENT_CONFIG_SUFFIX);
            }
        }

        return result;
    }

    /**
     * Gets the repositoryDirectory property value.
     *
     * @return the current value of the repositoryDirectory property
     */
    public String getRepositoryDirectory() {
        return repositoryDirectory;
    }

    /**
     * Sets the repositoryDirectory property.
     *
     * @param repositoryDirectory the new property value
     */
    public void setRepositoryDirectory(String repositoryDirectory) {
        this.repositoryDirectory = repositoryDirectory;
    }

    /**
     * Create a InteractiveConfigurer instance.
     *
     * @return a InteractiveConfigurer instance.
     * @throws IOException indicating failure
     */
    protected InteractiveConfigurer newInteractiveConfigurer()
        throws IOException {
        return new ConsoleInteractiveConfigurer();
    }

    /**
     * Gets the repository directory from the Environment, if possible.
     *
     * @return a repository directory or null indicating not set
     */
    protected String getRepositoryDirectoryFromEnvironment() {
        return System.getenv(ENV_DEPLOYCONF_REPO);
    }

    /**
     * Gets the Project Properties for this program.
     *
     * @return the Project Properteis for this program
     * @throws IOException indicating failure to load properties
     */
    private static ProjectProperties getProjectProperties() throws IOException {
        return ProjectProperties.instance();
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
            config.apply(srcStream, destStream, getDeploymentTemplatePath());
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
        File file = getDeploymentConfigFile(config.getName());
        logger.info("Saving Deployment Configuration to '" + file.getPath()
            + "'");
        FileOutputStream os = new FileOutputStream(file);
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
        if (entry == null) {
            throw new IllegalArgumentException(
                "No deployment template file found in file '" + file.getPath()
                    + "': " + path);
        }
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
