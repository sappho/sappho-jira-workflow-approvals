package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ApprovalsConfigurationValidCondition implements Condition {

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        try {
            // only interested in if this instantiates meaning that configuration is possible
            PluginConfiguration.getInstance().getApprovalsConfigurationPlugin((Issue) transientVars.get("issue"));
        } catch (Throwable t) {
            return false;
        }
        return true;
    }
}
