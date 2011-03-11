package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.user.User;

public class ApprovalsIssueListener implements IssueEventListener {

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
            // TODO: Remove this when it's really known to be not needed!
            //issueCommented(event);
            break;
        }
    }

    public void issueCreated(IssueEvent event) {

        try {
            ApprovalsConfiguration approvalsConfiguration = ApprovalsConfiguration.getInstance();
            MutableIssue issue = (MutableIssue) event.getIssue();
            String project = issue.getProjectObject().getKey();
            if (approvalsConfiguration.isConfiguredProject(project)) {
                boolean isChanged = false;
                String issueType = issue.getIssueTypeObject().getName();
                if (approvalsConfiguration.isIssueType(project, "auto.assign.reporter", issueType)) {
                    User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
                    issue.setAssignee(user);
                    isChanged = true;
                    log.warn("Assigned issue " + issue.getKey() + " to " + user.getFullName());
                }
                if (isChanged)
                    issue.store();
            }
        } catch (Throwable t) {
            log.error("Unable to completely process an issue creation event!", t);
        }
    }

    @SuppressWarnings("unchecked")
    public void init(Map params) {
    }

    public String[] getAcceptedParams() {

        return new String[] {};
    }

    public String getDescription() {

        return "Approvals Issue Listener";
    }

    public boolean isInternal() {

        return false; // this listener can be deleted
    }

    public boolean isUnique() {

        return true; // this listener can only be a singleton
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

    public void issueCommented(IssueEvent event) {
    }

    public void issueWorkLogged(IssueEvent event) {
    }
}
