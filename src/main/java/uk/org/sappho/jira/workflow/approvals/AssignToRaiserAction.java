package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class AssignToRaiserAction extends AbstractJiraFunctionProvider {

    private static final Logger log = Logger.getLogger(AssignToRaiserAction.class);

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        User assignee = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        log.warn("Assigning " + issue.getKey() + " to " + assignee.getFullName() + " because they raised it");
        issue.setAssignee(assignee);
        issue.store();
    }
}
