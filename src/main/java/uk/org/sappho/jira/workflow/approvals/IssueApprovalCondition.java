package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

abstract public class IssueApprovalCondition implements Condition {

    @SuppressWarnings("rawtypes")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        boolean passes = isApprovedCondition();
        Issue issue = (Issue) transientVars.get("issue");
        String project = issue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();
        for (Issue subTask : issue.getSubTaskObjects())
            if (pluginConfiguration.isIssueType(project, approvalType, subTask.getIssueTypeObject().getName()))
                if (pluginConfiguration.isNotApproved(project, subTask)) {
                    passes = !isApprovedCondition();
                    break;
                }
        return passes;
    }

    abstract protected boolean isApprovedCondition();
}
