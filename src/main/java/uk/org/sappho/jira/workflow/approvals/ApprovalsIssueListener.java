package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.opensymphony.user.User;

public class ApprovalsIssueListener implements IssueEventListener {

    private final Map<String, String> issueTypes = new HashMap<String, String>();
    private final Map<String, String> projects = new HashMap<String, String>();
    private ApprovalsConfiguration approvalsConfiguration;
    private CustomField serviceTypeField;
    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(ApprovalsIssueListener.class);

    public void workflowEvent(IssueEvent event) {

        // check if this is an issue create - event type id will be 1
        if (serviceTypeField != null && event.getEventTypeId() == 1) {
            MutableIssue issue = (MutableIssue) event.getIssue();
            String issueType = issue.getIssueTypeObject().getName();
            String project = issue.getProjectObject().getKey();
            // check that the new issue is in a relevant project
            if (projects.get(project) != null) {
                // is it an approval issue?
                if (approvalsConfiguration.isApprovalIssueType(issueType)) {
                    try {
                        // approvals sub-tasks are assigned to the approval type team lead by default
                        String approver = approvalsConfiguration.getApprover(project, issueType);
                        if (approver != null) {
                            User user = componentManager.getUserUtil().getUser(approver);
                            if (user != null) {
                                issue.setAssignee(user);
                                // store the change - will not persist otherwise
                                issue.store();
                                log.warn("Assigned approval issue " + issue.getKey() + " to " + user.getFullName());
                            }
                        }
                    } catch (Throwable t) {
                    }
                } else if (issueTypes.get(issueType) != null) {
                    // new non-approvals issues will always default to being assigned to the person raising the issue
                    User user = componentManager.getJiraAuthenticationContext().getUser();
                    issue.setAssignee(user);
                    // store the change - will not persist otherwise
                    issue.store();
                    log.warn("Assigned issue " + issue.getKey() + " to logged in reporter " + user.getFullName());
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void init(Map params) {

        log.warn("Initialising Approvals Issue Listener parameters");
        getParam(params, "issue.types", "Issue type", issueTypes);
        getParam(params, "projects", "Project", projects);
        serviceTypeField = componentManager.getCustomFieldManager().getCustomFieldObjectByName("Service / Type");
        if (serviceTypeField == null)
            log.warn("Service / Type custom field is not configured!");
        approvalsConfiguration = new ApprovalsConfiguration(getParam(params, "wiki.url"), getParam(params,
                "wiki.username"), getParam(params, "wiki.password"), getParam(params, "wiki.space"), getParam(params,
                "wiki.page.prefix"), getParam(params, "wiki.page.suffix"),
                getParam(params, "approval.issue.type.regex"));
    }

    @SuppressWarnings("unchecked")
    private void getParam(Map params, String paramName, String description, Map<String, String> list) {

        for (String item : getParam(params, paramName).split(",")) {
            item = item.trim();
            log.warn(description + ": " + item);
            list.put(item, item);
        }
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

        return new String[] { "issue.types", "projects", "wiki.url", "wiki.username", "wiki.password", "wiki.space",
                "wiki.page.prefix", "wiki.page.suffix", "approval.issue.type.regex" };
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
