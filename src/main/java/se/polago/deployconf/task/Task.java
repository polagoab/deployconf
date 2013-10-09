package se.polago.deployconf.task;

import java.io.InputStream;
import java.io.OutputStream;

import org.jdom2.Element;

/**
 * Describes a deployment task.
 */
public interface Task {

    /**
     * Gets the Task Path.
     *
     * @return the Task path
     */
    String getPath();

    /**
     * Gets the serialized Task Name.
     *
     * @return the serialized name of this task
     */
    String getSerializedName();

    /**
     * Configure the task from a list of JDOM Elements.
     *
     * @param node the JDOM Element to use for configuring this instance
     */
    void configure(Element node);

    /**
     * Determine if the Task is completely configured.
     *
     * @return true if the Task is completely configured
     */
    boolean isConfigured();

    /**
     * Configure the task by asking the user for inputs.
     *
     * @return true if the task was configured
     * @throws Exception indicating processing failure
     */
    boolean configureInteractively() throws Exception;

    /**
     * Serialize this Task to a JDOM Element.
     *
     * @param node the JDOM Element to serialize to
     */
    void serialize(Element node);

    /**
     * Apply this Task by copying source to destination.
     *
     * @param source the Input stream
     * @param destination the Output stream
     * @throws Exception indicating processing failure
     */
    void apply(InputStream source, OutputStream destination) throws Exception;

}
