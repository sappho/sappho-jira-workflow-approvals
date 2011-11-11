package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class CleanupApprovalsAction extends AbstractJiraFunctionProvider {

    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(CleanupApprovalsAction.class);

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet propertySet) throws WorkflowException {

        MutableIssue mainIssue = (MutableIssue) transientVars.get("issue");

        User user = componentManager.getJiraAuthenticationContext().getUser();
        log.warn(user.getFullName() + " has triggered the death of child zombies of " + mainIssue.getKey());

        for (Issue subTask : mainIssue.getSubTaskObjects()) {
            if (subTask.getStatusObject().getName().equals("Approval Sought")) {
                int transitionActionId = 51;
                log.warn("Running workflow transition action id " + transitionActionId + " on " + subTask.getKey());
                boolean wasIndexing = ImportUtils.isIndexIssues();
                ImportUtils.setIndexIssues(true);
                WorkflowTransitionUtil workflowTransitionUtil = JiraUtils
                        .loadComponent(WorkflowTransitionUtilImpl.class);
                workflowTransitionUtil.setIssue((MutableIssue) subTask);
                workflowTransitionUtil.setUsername(user.getName());
                workflowTransitionUtil.setAction(transitionActionId);
                ErrorCollection errors = workflowTransitionUtil.validate();
                if (errors.hasAnyErrors())
                    throw new WorkflowException("Unable to validate transition " + subTask.getKey()
                            + "! Caused by "
                            + errors);
                errors = workflowTransitionUtil.progress();
                ImportUtils.setIndexIssues(wasIndexing);
                if (errors.hasAnyErrors())
                    throw new WorkflowException("Unable to progress transition " + subTask.getKey()
                            + "! Caused by "
                            + errors);
            }
        }
    }
}
