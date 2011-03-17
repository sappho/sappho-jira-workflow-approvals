package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.Condition;
import com.opensymphony.workflow.WorkflowException;

public class ParentStatusCondition implements Condition {

    @SuppressWarnings("unchecked")
    public boolean passesCondition(Map transientVars, Map params, PropertySet ps) throws WorkflowException {

        boolean passes = false;
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        if (issue.isSubTask()) {
            Issue parentIssue = issue.getParentObject();
            String project = issue.getProjectObject().getKey();
            String statusKey = (String) params.get(StatusKeyFactory.statusKey);
            ApprovalsConfiguration approvalsConfiguration = ApprovalsConfiguration.getInstance();
            passes = approvalsConfiguration.isRegexMatch(project, "statuses." + statusKey, parentIssue
                    .getStatusObject().getName());
        }
        return passes;
    }
}
