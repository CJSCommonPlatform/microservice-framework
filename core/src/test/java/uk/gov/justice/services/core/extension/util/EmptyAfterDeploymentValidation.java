package uk.gov.justice.services.core.extension.util;

import jakarta.enterprise.inject.spi.AfterDeploymentValidation;


public class EmptyAfterDeploymentValidation implements AfterDeploymentValidation {
    @Override
    public void addDeploymentProblem(Throwable throwable) {

    }
}
