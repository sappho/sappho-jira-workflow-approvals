package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class IssueApprovedCondition implements Condition {

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        boolean passes = true;
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String project = issue.getProjectObject().getKey();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();
        for (MutableIssue subTask : issue.getSubTaskObjects())
            if (pluginConfiguration.isIssueType(project, approvalType, subTask.getIssueTypeObject().getName()))
                if (pluginConfiguration.isNotApproved(project, subTask)) {
                    passes = false;
                    break;
                }
        return passes;
    }
}
