package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.workflow.WorkflowTransitionUtil;
import com.atlassian.jira.workflow.WorkflowTransitionUtilImpl;
import com.opensymphony.user.User;

public class ApprovalsIssueListener implements IssueEventListener {

    private final Map<String, String> issueTypes = new HashMap<String, String>();
    private final Map<String, String> projects = new HashMap<String, String>();
    private final Map<String, Integer> autoTransitionStates = new HashMap<String, Integer>();
    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(ApprovalsIssueListener.class);

    public void workflowEvent(IssueEvent event) {

        int eventId = event.getEventTypeId().intValue();
        log.warn("Issue event ID = " + eventId);
        // all issue events are handled here
        switch (eventId) {
        case 1:
            // track issue creates to auto-assign them
            issueCreated(event);
            break;
        case 6:
            // track issue comments to trigger issue auto-transition after approval
            issueCommented(event);
            break;
        }
    }

    public void issueCreated(IssueEvent event) {

        MutableIssue issue = (MutableIssue) event.getIssue();
        String project = issue.getProjectObject().getKey();
        // check that the new issue is in a relevant project
        if (projects.get(project) != null) {
            String issueType = issue.getIssueTypeObject().getName();
            if (issueTypes.get(issueType) != null)
                // new non-approvals issues will always default to being assigned to the person raising the issue
                setAssignee(issue, componentManager.getJiraAuthenticationContext().getUser());
        }
    }

    private void setAssignee(MutableIssue issue, User user) {

        issue.setAssignee(user);
        issue.store();
        log.warn("Assigned issue " + issue.getKey() + " to " + user.getFullName());
    }

    public void issueCommented(IssueEvent event) {

        MutableIssue issue = (MutableIssue) event.getIssue();
        String project = issue.getProjectObject().getKey();
        // check that the new issue is in a relevant project and isn't a sub-task
        if (projects.get(project) != null && issue.getParentObject() == null) {
            String status = issue.getStatusObject().getName();
            Integer actionId = autoTransitionStates.get(status);
            if (actionId != null) {
                log.warn("Attempting to transition " + issue.getKey() + " to seeking management approval");
                WorkflowTransitionUtil workflowTransitionUtil = JiraUtils
                        .loadComponent(WorkflowTransitionUtilImpl.class);
                workflowTransitionUtil.setIssue(issue);
                workflowTransitionUtil.setUsername(componentManager.getJiraAuthenticationContext().getUser().getName());
                workflowTransitionUtil.setAction(actionId);
                ErrorCollection errors = workflowTransitionUtil.validate();
                if (errors.hasAnyErrors())
                    log.error("Unable to transition " + issue.getKey() + " caused by " + errors);
                else {
                    errors = workflowTransitionUtil.progress();
                    if (errors.hasAnyErrors())
                        log.error("Unable to transition " + issue.getKey() + " caused by " + errors);
                    else
                        log.warn("Transitioned " + issue.getKey() + " to seeking management approval");
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void init(Map params) {

        log.warn("Initialising Approvals Issue Listener parameters");
        try {
            ApprovalsConfiguration approvalsConfiguration = ApprovalsConfiguration.getInstance();
            for (String project : approvalsConfiguration.getPropertyList("approval.projects")) {
                projects.put(project, project);
                log.warn("Project: " + project);
            }
            for (String issueType : approvalsConfiguration.getPropertyList("approval.issue.types.main")) {
                issueTypes.put(issueType, issueType);
                log.warn("Issue type: " + issueType);
            }
            for (String transition : approvalsConfiguration.getPropertyList("approval.auto.transitions")) {
                String[] transitionBits = transition.split("-");
                if (transitionBits.length == 2) {
                    // the text name (not id) of 
                    String from = transitionBits[0].trim();
                    int to = Integer.parseInt(transitionBits[1].trim());
                    log.warn("Auto-transition: " + from + " --> action id " + to);
                    autoTransitionStates.put(from, to);
                } else
                    log.warn("Invalid auto-transition state " + transition + "!");
            }
        } catch (Exception e) {
            log.error("Unable to load listener configuration!", e);
        }
    }

    public String[] getAcceptedParams() {

        return new String[] {};
    }

    public String getDescription() {

        return "Approvals Issue Listener";
    }

    public boolean isInternal() {

        return false;
    }

    public boolean isUnique() {

        return false;
    }

    public void customEvent(IssueEvent event) {
    }

    public void issueAssigned(IssueEvent event) {
    }

    public void issueClosed(IssueEvent event) {
    }

    public void issueDeleted(IssueEvent event) {
    }

    public void issueGenericEvent(IssueEvent event) {
    }

    public void issueMoved(IssueEvent event) {
    }

    public void issueReopened(IssueEvent event) {
    }

    public void issueResolved(IssueEvent event) {
    }

    public void issueStarted(IssueEvent event) {
    }

    public void issueStopped(IssueEvent event) {
    }

    public void issueUpdated(IssueEvent event) {
    }

    public void issueWorkLogged(IssueEvent event) {
    }
}
