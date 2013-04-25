package org.jenkinsci.plugins.jobparametersummary;

import hudson.Extension;
import hudson.model.Action;
import hudson.model.InvisibleAction;
import hudson.model.TransientProjectActionFactory;
import hudson.model.AbstractProject;
import hudson.model.ParameterDefinition;
import hudson.model.ParametersDefinitionProperty;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class Summary extends InvisibleAction {

	final private AbstractProject<?, ?> project;

	public Summary(@SuppressWarnings("rawtypes") AbstractProject project) {

	    this.project = project;
	}

	public List<ParameterDefinition> getParameters() {

	    final ParametersDefinitionProperty params = project.getProperty(
	            ParametersDefinitionProperty.class
	    );

	    return params.getParameterDefinitions();
	}

    @Extension
    public static class SummaryFactory extends TransientProjectActionFactory {

        /**
         * {@inheritDoc}}
         */
        @Override
        public Collection<? extends Action> createFor(
                @SuppressWarnings("rawtypes") AbstractProject target
        ) {

            return Arrays.asList(new Summary(target));
        }
    }
}
