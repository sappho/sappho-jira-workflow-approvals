package uk.org.sappho.jira.workflow.approvals;

import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class AssignToApproverAction extends AbstractJiraFunctionProvider {

    private static final Logger log = Logger.getLogger(AssignToApproverAction.class);

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        User assignee = issue.getAssignee();
        String assigneeName = null;
        if (assignee != null)
            assigneeName = assignee.getName();
        if (assigneeName == null) {
            String issueType = issue.getIssueTypeObject().getName();
            ApprovalsConfigurationPlugin approvalsKey =
                    PluginConfiguration.getInstance().getApprovalsConfigurationPlugin(issue);
            List<String> approvers = approvalsKey.getAllowedApprovers(issueType);
            if (approvers == null)
                throw new WorkflowException("There is no configured assignee for approval type "
                        + issueType + " - check wiki page!");
            assigneeName = approvers.get(0);
            assignee = ComponentManager.getInstance().getUserUtil().getUser(assigneeName);
            if (assignee == null)
                throw new WorkflowException("Configured assignee " + assigneeName + " is invalid!");
            log.warn("Assigning " + issue.getKey() + " to " + assignee.getFullName()
                    + " because they are the lead approver");
            issue.setAssignee(assignee);
            issue.store();
        }
    }
}
