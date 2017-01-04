package org.jenkinsci.plugins.jobparametersummary;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BooleanParameterDefinition;
import hudson.model.BooleanParameterValue;
import hudson.model.ChoiceParameterDefinition;
import hudson.model.InvisibleAction;
import hudson.model.ParameterDefinition;
import hudson.model.ParameterValue;
import hudson.model.ParametersDefinitionProperty;
import hudson.model.PasswordParameterDefinition;
import hudson.model.PasswordParameterValue;
import hudson.model.StringParameterDefinition;
import hudson.model.StringParameterValue;
import hudson.model.TransientProjectActionFactory;
import hudson.util.Secret;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Summary extends InvisibleAction {

    private static final String PASSWORD_MASK = "********";
    private static final String QUOTE_STRING = "\"";

    final private AbstractProject<?, ?> project;

    public Summary(@SuppressWarnings("rawtypes") AbstractProject project) {

        this.project = project;
    }

    public List<ParameterDefinition> getParameters() {

        return definitionProperty(project).getParameterDefinitions();
    }

    @Override
    public String toString() {

        return "Job parameter summary for " + project.toString();
    }

    private static ParametersDefinitionProperty definitionProperty(
            final AbstractProject<?, ?> project
    ) {

        return project.getProperty(ParametersDefinitionProperty.class);
    }

    private static String quote(String s) {
        return QUOTE_STRING + s + QUOTE_STRING;
    }

    /**
     * Get default value for {@link ParameterDefinition} that has any and it can be displayed
     */
    public String getDefault(final ParameterDefinition d) {

        final ParameterValue v = d.getDefaultParameterValue();

        String res = null;
        if (d instanceof BooleanParameterDefinition) {
            res = new Boolean(((BooleanParameterValue) v).value).toString();
        } else if (d instanceof StringParameterDefinition) {
            res = quote(((StringParameterValue) v).value);
        } else if (d instanceof PasswordParameterDefinition) {
            // check whether we have a default value and return a printable mask if we do
            String password = Secret.toString(((PasswordParameterValue) v).getValue());
            if (!password.isEmpty()) {
                res = quote(PASSWORD_MASK);
            }
        } else if (d instanceof ChoiceParameterDefinition) {
            res = quote(((StringParameterValue) v).value);
        }

        return res;
    }

    @Extension
    public static class SummaryFactory extends TransientProjectActionFactory {

        /**
         * For matrix projects parameter actions are attached to the MatrixProject
         */
        @Override
        public Collection<? extends Action> createFor(
                @SuppressWarnings("rawtypes") AbstractProject target
        ) {

            try {
                Class<?> mc = Class.forName("hudson.matrix.MatrixConfiguration");
                if (mc.isInstance(target)) {
                    target = (AbstractProject) target.getParent();
                }
            } catch (ClassNotFoundException e) {
                // matrix-project not installed
            }

            if (!isParameterized(target)) {
                return Collections.emptyList();
            }

            return Arrays.asList(new Summary(target));
        }

        private boolean isParameterized(final AbstractProject<?, ?> project) {

            return definitionProperty(project) != null;
        }
    }
}
