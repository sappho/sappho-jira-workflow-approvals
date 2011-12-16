package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class CleanupApprovalsAction extends BulkApprovalsAction {

    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(CleanupApprovalsAction.class);

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet propertySet) throws WorkflowException {

        MutableIssue mainIssue = (MutableIssue) transientVars.get("issue");

        User user = componentManager.getJiraAuthenticationContext().getUser();
        log.warn(user.getFullName() + " has triggered the death of child zombies of " + mainIssue.getKey());

        for (MutableIssue subTask : mainIssue.getSubTaskObjects()) {
            if (subTask.getStatusObject().getName().equals("Approval Sought")) {
                transitionIssue(subTask, 51, user);
            }
        }
    }
}
