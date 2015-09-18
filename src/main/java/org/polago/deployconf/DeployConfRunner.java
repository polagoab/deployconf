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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
import org.polago.deployconf.group.ConfigGroupManager;
import org.polago.deployconf.group.FileSystemConfigGroupManager;
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

    private static Logger logger = LoggerFactory.getLogger(DeployConfRunner.class);

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
     * The Environment Variable used to set the local repository for storing config files. This may be overridden by
     * command line options.
     */
    private static final String ENV_DEPLOYCONF_REPO = "DEPLOYCONF_REPO";

    /**
     * The default local repository relative the user's HOME directory for storing config files. This may be overridden
     * by command line options.
     */
    private static final String DEFAULT_DEPLOYCONF_REPO = "/.deployconf_repo";

    /**
     * The default deployment template path to use.
     */
    private static final String DEFAULT_TEMPLATE_PATH = "META-INF/deployment-template.xml";

    /**
     * The deployment config file suffix to use when creating deploymentConfig paths.
     */
    protected static final String DEPLOYMENT_CONFIG_SUFFIX = "deployment-config.xml";

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
    private Path deploymentConfigFile = null;

    /**
     * The local repository for storing deployment config files. Null means current directory.
     */
    private String repositoryDirectory = null;

    /**
     * The Configuration Group Manager to use.
     */
    private ConfigGroupManager groupManager;

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

        Option help = new Option("h", "help", false, "Display usage information");
        options.addOption(help);

        Option version = new Option("v", "version", false, "Display version information and exit");
        options.addOption(version);

        Option interactive = new Option("i", "interactive", false, "Run in interactive mode");
        options.addOption(interactive);

        Option forceInteractive =
            new Option("I", "force-interactive", false, "Run in interactive mode and configure all tasks");
        options.addOption(forceInteractive);

        Option quiet = new Option("q", "quiet", false, "Suppress most messages");
        options.addOption(quiet);

        Option debug = new Option("d", "debug", false, "Print Debug Information");
        options.addOption(debug);

        boolean debugEnabled = false;

        Option repoDir = new Option("r", "repo", true, "Repository directory to use for storing deployment configs");
        options.addOption(repoDir);

        Option configFile =
            new Option("f", "deployment-config-file", true, "File to use for storing the deployment config");
        options.addOption(configFile);

        Option templatePath =
            new Option("t", "deployment-template-path", true, "Path to use for locating the deployment template in the "
                + "<INPUT> file. Default is '" + DEFAULT_TEMPLATE_PATH + "'");
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
                formatter.printHelp(projectProperties.getName() + " [OPTION]... <INPUT> <OUTPUT>",
                    projectProperties.getHelpHeader(), options, "");
                System.exit(0);
            }

            if (cmd.hasOption(debug.getOpt())) {
                logger.info("Activating Debug Logging");
                debugEnabled = true;
                setLogConfig("logback-debug.xml");
            } else if (cmd.hasOption(quiet.getOpt())) {
                setLogConfig("logback-quiet.xml");
            }

            RunMode mode = RunMode.NON_INTERACTIVE;
            if (cmd.hasOption(forceInteractive.getOpt())) {
                mode = RunMode.FORCE_INTERACTIVE;
            } else if (cmd.hasOption(interactive.getOpt())) {
                mode = RunMode.INTERACTIVE;
            }

            DeployConfRunner instance = new DeployConfRunner(mode);

            String envRepoDir = instance.getRepositoryDirectoryFromEnvironment();

            if (cmd.hasOption(repoDir.getOpt())) {
                String rd = cmd.getOptionValue(repoDir.getOpt());
                logger.debug("Using repository directory: {}", rd);
                instance.setRepositoryDirectory(rd);
            } else if (envRepoDir != null) {
                logger.debug("Using repository directory from environment {}: {}", ENV_DEPLOYCONF_REPO, envRepoDir);
                instance.setRepositoryDirectory(envRepoDir);
            } else {
                String rd = getDefaultRepository();
                instance.setRepositoryDirectory(rd);
                logger.debug("Using default repository directory: {}", rd);
            }
            Path repo = FileSystems.getDefault().getPath(instance.getRepositoryDirectory());
            if (!Files.exists(repo)) {
                Files.createDirectories(repo);
            } else if (!Files.isDirectory(repo)) {
                logger.error("Specified repository is not a directory: {}", repo);
                System.exit(1);
            }

            instance.setGroupManager(new FileSystemConfigGroupManager(Paths.get(instance.getRepositoryDirectory())));

            if (cmd.hasOption(configFile.getOpt())) {
                String f = cmd.getOptionValue(configFile.getOpt());
                logger.debug("Using explicit deployment file: {}", f);
                instance.setDeploymentConfigPath(FileSystems.getDefault().getPath(f));
            }

            if (cmd.hasOption(templatePath.getOpt())) {
                String path = cmd.getOptionValue(templatePath.getOpt());
                logger.debug("Using deployment template path: {}", path);
                instance.setDeploymentTemplatePath(path);
            }

            @SuppressWarnings("unchecked")
            List<String> argList = cmd.getArgList();
            if (argList.size() != 2) {
                System.out.println("usage: " + projectProperties.getName() + " <INPUT> <OUTPUT>");
                System.exit(1);
            }
            System.exit(instance.run(argList.get(0), argList.get(1)));
        } catch (ParseException e) {
            logger.error("Command Line Parse Error: " + e.getMessage(), e);
            System.exit(1);
        } catch (Exception e) {
            String msg = "Internal Error: " + e.toString();
            if (!debugEnabled) {
                msg += "\n(use the -d option to print stacktraces)";
            }
            logger.error(msg, e);
            System.exit(2);
        }
    }

    /**
     * Gets the default repository directory based on the users home directory.
     *
     * @return the default repository for the user
     */
    private static String getDefaultRepository() {
        return System.getProperty("user.home") + DEFAULT_DEPLOYCONF_REPO;
    }

    /**
     * Sets the log configuration to use.
     *
     * @param config the log configuration to use
     */
    private static void setLogConfig(String config) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();

        try {
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(loggerContext);
            loggerContext.reset();
            configurator.doConfigure(Thread.currentThread().getContextClassLoader().getResource(config));
        } catch (JoranException e) {
            logger.warn("Error setting log config: " + config, e);
        }
        StatusPrinter.printInCaseOfErrorsOrWarnings(loggerContext);
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
        DeploymentConfig template = getDeploymentConfigFromZip(source);
        DeploymentConfig config = null;
        Path repoFile = getDeploymentConfigPath(template.getName());
        if (Files.exists(repoFile)) {
            logger.info("Loading Deployment Configuration from: " + repoFile);
            config = getDeploymentConfigFromPath(repoFile);
        } else {
            logger.info("Creating new Deployment Config: " + repoFile);
            config = new DeploymentConfig();
            config.setGroupManager(groupManager);
        }

        logger.debug("Running in mode: {}", runMode);

        if (config.merge(template) && !(runMode == RunMode.FORCE_INTERACTIVE)) {
            apply(config, source, destination);
        } else {
            // Needs manual merge
            boolean interactive = runMode == RunMode.INTERACTIVE || runMode == RunMode.FORCE_INTERACTIVE;
            if (interactive
                && config.interactiveMerge(newInteractiveConfigurer(), runMode == RunMode.FORCE_INTERACTIVE)) {
                save(config);
                apply(config, source, destination);
            } else {
                save(config);
                System.err.println("Deployment Configuration is incomplete");
                System.err.println("Rerun in interactive mode " + "by using the '-i' option");
                System.err.println(" or");
                System.err.println(
                    "Edit '" + repoFile + "' and make sure that each " + "deployment property has a valid value");
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
     * @param deploymentConfigPath the new property value
     */
    public void setDeploymentConfigPath(Path deploymentConfigPath) {
        this.deploymentConfigFile = deploymentConfigPath;
    }

    /**
     * Gets the Path to use for storing the DeploymentConfig.
     * <p>
     * Unless an explicit deployment config File is set, the File is created in the configured repository and based on
     * the config name. If no repository is set, the current working directory is used.
     *
     * @param name the DeploymentConfig name to use
     * @return the Path to the DeploymentConfig
     */
    public Path getDeploymentConfigPath(String name) {
        Path result = deploymentConfigFile;
        String dir = getRepositoryDirectory();
        if (dir == null) {
            dir = "";
        }
        if (deploymentConfigFile != null) {
            result = deploymentConfigFile;
        } else {
            FileSystem fs = FileSystems.getDefault();
            if (name != null) {
                result = fs.getPath(dir, name + "-" + DEPLOYMENT_CONFIG_SUFFIX);
            } else {
                result = fs.getPath(dir, DEPLOYMENT_CONFIG_SUFFIX);
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
     * Gets the groupManager property value.
     *
     * @return the current value of the groupManager property
     */
    public ConfigGroupManager getGroupManager() {
        return groupManager;
    }

    /**
     * Sets the groupManager property.
     *
     * @param groupManager the new property value
     */
    public void setGroupManager(ConfigGroupManager groupManager) {
        this.groupManager = groupManager;
    }

    /**
     * Create a InteractiveConfigurer instance.
     *
     * @return a InteractiveConfigurer instance.
     * @throws IOException indicating failure
     */
    protected InteractiveConfigurer newInteractiveConfigurer() throws IOException {
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
     * Apply the given DeploymentConfig to the source and create the destination.
     *
     * @param config the DeploymentConfig to apply
     * @param source the input file
     * @param destination the destination file
     * @throws Exception indicating processing error
     */
    private void apply(DeploymentConfig config, String source, String destination) throws Exception {

        FileSystem fs = FileSystems.getDefault();
        Path sourceFile = fs.getPath(source);
        Path destFile = fs.getPath(destination);

        InputStream srcStream = null;
        OutputStream destStream = null;

        try {
            srcStream = Files.newInputStream(sourceFile);
            destStream = Files.newOutputStream(destFile);
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
        Path file = getDeploymentConfigPath(config.getName());
        logger.info("Saving Deployment Configuration to '" + file + "'");
        FileOutputStream os = new FileOutputStream(file.toFile());
        try {
            config.save(os);
        } finally {
            os.close();
        }
    }

    /**
     * Gets a DeploymentConfig instance from a Zip file.
     *
     * @param source the ZipFile to use
     * @return a DeploymentConfig representation of the ReadableByteChannel
     * @throws Exception indicating error
     */
    private DeploymentConfig getDeploymentConfigFromZip(String source) throws Exception {

        ZipFile zipFile = new ZipFile(source);
        ZipEntry entry = zipFile.getEntry(deploymentTemplatePath);
        if (entry == null) {
            zipFile.close();
            throw new IllegalArgumentException(
                "No deployment template file found in file '" + source + "': " + deploymentTemplatePath);
        }
        InputStream is = zipFile.getInputStream(entry);

        DeploymentReader reader = new DeploymentReader(is, groupManager);
        DeploymentConfig result = reader.parse();
        is.close();
        zipFile.close();

        return result;
    }

    /**
     * Gets a DeploymentConfig instance from a Path.
     *
     * @param path the file to use
     * @return a DeploymentConfig representation of the ReadableByteChannel
     * @throws Exception indicating error
     */
    private DeploymentConfig getDeploymentConfigFromPath(Path path) throws Exception {

        ReadableByteChannel ch = FileChannel.open(path, StandardOpenOption.READ);
        InputStream is = Channels.newInputStream(ch);

        DeploymentReader reader = new DeploymentReader(is, getGroupManager());
        DeploymentConfig result = reader.parse();
        ch.close();

        return result;
    }
}
