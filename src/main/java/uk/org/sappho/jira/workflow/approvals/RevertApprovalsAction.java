package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class RevertApprovalsAction extends BulkApprovalsAction {

    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(RevertApprovalsAction.class);

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet propertySet) throws WorkflowException {

        Issue mainIssue = (Issue) transientVars.get("issue");

        User user = componentManager.getJiraAuthenticationContext().getUser();
        log.warn(user.getFullName() + " has triggered a reconsideration of all approvals of " + mainIssue.getKey());

        for (Issue subTask : mainIssue.getSubTaskObjects()) {
            if (subTask.getStatusObject().getName().equals("Approved")) {
                transitionIssueWithAssignment(subTask, 31, user);
            }
            if (subTask.getStatusObject().getName().equals("Rejected")) {
                transitionIssueWithAssignment(subTask, 41, user);
            }
        }
    }
}
