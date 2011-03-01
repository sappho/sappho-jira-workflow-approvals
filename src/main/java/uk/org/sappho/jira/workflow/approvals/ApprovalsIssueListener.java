package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.issue.MutableIssue;
import com.opensymphony.user.User;

public class ApprovalsIssueListener implements IssueEventListener {

    private final Map<String, String> issueTypes = new HashMap<String, String>();
    private final Map<String, String> projects = new HashMap<String, String>();
    private static final Logger log = Logger.getLogger(ApprovalsIssueListener.class);

    public void workflowEvent(IssueEvent event) {

        // check if this is an issue create - event type id will be 1
        if (event.getEventTypeId() == 1) {
            MutableIssue issue = (MutableIssue) event.getIssue();
            String issueType = issue.getIssueTypeObject().getName();
            String project = issue.getProjectObject().getKey();
            // check that the new issue is in a relevant project
            if (issueTypes.get(issueType) != null && projects.get(project) != null) {
                User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
                issue.setAssignee(user);
                issue.store();
                log.warn("Assigned issue " + issue.getKey() + " to logged in reporter " + user.getFullName());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void init(Map params) {

        log.warn("Initialising Approvals Issue Listener parameters");
        for (String project : getParam(params, "projects").split(",")) {
            project = project.trim();
            log.warn("Project: " + project);
            projects.put(project, project);
        }
        for (String issueType : getParam(params, "issue.types").split(",")) {
            issueType = issueType.trim();
            log.warn("Issue type: " + issueType);
            issueTypes.put(issueType, issueType);
        }
        /*
        wikiURL = getParam(params, "wiki.url");
        wikiUsername = getParam(params, "wiki.username");
        wikiPassword = getParam(params, "wiki.password");
        wikiSpace = getParam(params, "wiki.space");
        wikiPagePrefix = getParam(params, "wiki.page.prefix");
        wikiPageSuffix = getParam(params, "wiki.page.suffix");
        log.warn("Wiki URL: " + wikiURL);
        log.warn("Wiki username: " + wikiUsername);
        log.warn("Wiki password: " + wikiPassword);
        log.warn("Wiki space: " + wikiSpace);
        log.warn("Wiki page suffix: " + wikiPageSuffix);
        if (wikiPagePrefix.length() > 0)
            wikiPagePrefix += " ";
        if (wikiPageSuffix.length() > 0)
            wikiPageSuffix = " " + wikiPageSuffix;
        */
    }

    @SuppressWarnings("unchecked")
    private String getParam(Map params, String paramName) {

        String value = "";
        Object valueObj = params.get(paramName);
        if (valueObj != null && valueObj instanceof String)
            value = ((String) valueObj);
        else
            log.warn("Missing " + paramName + " configuration parameter!");
        return value;
    }

    public String[] getAcceptedParams() {

        return new String[] { "issue.types", "projects" };
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

    public void issueCommented(IssueEvent event) {
    }

    public void issueCreated(IssueEvent event) {
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
