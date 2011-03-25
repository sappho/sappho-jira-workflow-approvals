package uk.org.sappho.jira.workflow.approvals;

import java.util.List;
import java.util.Map;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class AssignToApproverAction extends AbstractJiraFunctionProvider {

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        String issueType = issue.getIssueTypeObject().getName();
        ApprovalsKey approvalsKey = new ServiceTypeRegion();
        approvalsKey.init(issue);
        List<String> approvers = approvalsKey.getAllowedApprovers(issueType);
        if (approvers == null)
            throw new WorkflowException("There is no configured assignee for approval type "
                    + issueType + " - check wiki page!");
        String assigneeName = approvers.get(0);
        User assignee = ComponentManager.getInstance().getUserUtil().getUser(assigneeName);
        if (assignee == null)
            throw new WorkflowException("Configured assignee " + assigneeName + " is invalid!");
        issue.setAssignee(assignee);
        issue.store();
    }
}
