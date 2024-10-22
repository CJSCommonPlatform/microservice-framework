package uk.gov.justice.services.jmx.api.command;

import java.io.Serializable;

public interface SystemCommand extends Serializable {

    String getName();
    String getDescription();

    default boolean requiresCommandRuntimeId() {
        return false;
    }
    default String commandRuntimeIdType() {
        return "Not required";
    }
    default boolean requiresCommandRuntimeString() {
        return false;
    }
    default String commandRuntimeStringType() {
        return "Not required";
    }
}
