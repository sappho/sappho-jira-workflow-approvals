package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class SeekApprovalAction extends AbstractJiraFunctionProvider {

    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(SeekApprovalAction.class);

    @SuppressWarnings("rawtypes")
    public void execute(Map transientVars, Map params, PropertySet propertySet) throws WorkflowException {

        MutableIssue issueToBeApproved = (MutableIssue) transientVars.get("issue");

        // Find out what approvals are needed
        PluginConfiguration pluginConfiguration = PluginConfiguration.getInstance();
        ApprovalsConfigurationPlugin approvalsConfigurationPlugin =
                pluginConfiguration.getApprovalsConfigurationPlugin(issueToBeApproved);

        String project = issueToBeApproved.getProjectObject().getKey();
        String summary = issueToBeApproved.getSummary();
        User user = componentManager.getJiraAuthenticationContext().getUser();
        String approvalType = (String) params.get(ApprovalTypeFactory.approvalTypeKey);
        log.warn(user.getFullName() + " has sought " + approvalType + " approval on " + issueToBeApproved.getKey());

        // Find out what approvals have already been sought
        Map<String, String> existingSubtaskIssueTypes = new HashMap<String, String>();
        for (Issue subTask : issueToBeApproved.getSubTaskObjects()) {
            String approvalIssueType = subTask.getIssueTypeObject().getName();
            existingSubtaskIssueTypes.put(approvalIssueType, approvalIssueType);
        }

        // Map all available issue types to their id 
        Map<String, String> typeIds = new HashMap<String, String>();
        for (IssueType issueType : componentManager.getConstantsManager().getAllIssueTypeObjects())
            typeIds.put(issueType.getName(), issueType.getId());

        // Create the approvals sub-tasks
        for (String approvalIssueType : approvalsConfigurationPlugin.getRequiredApprovalTypes())
            if (pluginConfiguration.isIssueType(project, approvalType, approvalIssueType))
                if (existingSubtaskIssueTypes.get(approvalIssueType) == null) {
                    List<String> approvers = approvalsConfigurationPlugin.getAllowedApprovers(approvalIssueType);
                    if (approvers == null)
                        throw new WorkflowException(
                                "Unable to create approval subtask because there are no configured approvers!");
                    log.warn("Creating " + approvalIssueType + " sub-task");
                    MutableIssue approvalTask = componentManager.getIssueFactory().getIssue();
                    approvalTask.setProjectId(issueToBeApproved.getProjectObject().getId());
                    approvalTask.setIssueTypeId(typeIds.get(approvalIssueType));
                    approvalTask.setReporter(user);
                    approvalTask.setSummary(approvalIssueType + " / " + summary);
                    approvalTask.setParentId(issueToBeApproved.getParentId());
                    try {
                        ImportUtils.setIndexIssues(true);
                        GenericValue createdIssue = componentManager.getIssueManager().createIssue(user,
                                approvalTask);
                        createdIssue.store();
                        componentManager.getSubTaskManager().createSubTaskIssueLink(
                                issueToBeApproved.getGenericValue(),
                                createdIssue, user);
                        issueToBeApproved.store();
                        ImportUtils.setIndexIssues(false);
                        existingSubtaskIssueTypes.put(approvalIssueType, approvalIssueType);
                    } catch (Exception e) {
                        throw new WorkflowException("Unable to create approval subtasks!", e);
                    }
                }
    }
}
