package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ApprovalsConfigurationValidCondition implements Condition {

    private static final Logger log = Logger.getLogger(ApprovalsConfigurationValidCondition.class);

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        Issue issue = (Issue) transientVars.get("issue");
        String issueKey = issue.getKey();
        log.warn("Checking if approvals configuration is valid for " + issueKey);
        try {
            // if this instantiates then the configuration is okay
            PluginConfiguration.getInstance().getApprovalsConfigurationPlugin(issue);
        } catch (Throwable t) {
            log.warn("Approvals configuration is NOT valid for " + issueKey);
            return false;
        }
        log.warn("Approvals configuration is valid for " + issueKey);
        return true;
    }
}
