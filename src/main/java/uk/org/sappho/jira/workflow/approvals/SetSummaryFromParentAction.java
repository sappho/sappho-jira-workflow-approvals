package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

public class SetSummaryFromParentAction extends AbstractJiraFunctionProvider {

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String issueType = issue.getIssueTypeObject().getName();
        String summary = issue.getSummary().trim();
        String parentSummary = issue.getParentObject().getSummary().trim();
        if (!summary.equals(parentSummary))
            summary = parentSummary + " / " + (summary.length() > 1 ? summary : "additional");
        issue.setSummary(issueType + " / " + summary);
        issue.store();
    }
}
