package uk.org.sappho.jira.workflow.approvals;

import java.util.Map;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class SeekApprovalAction extends AbstractJiraFunctionProvider {

    private CustomField serviceTypeField;
    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(SeekApprovalAction.class);

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map params, PropertySet propertySet) throws WorkflowException {

        MutableIssue issueToBeApproved = (MutableIssue) transientVars.get("issue");
        Project projectObj = issueToBeApproved.getProjectObject();
        String project = projectObj.getKey();
        String summary = issueToBeApproved.getSummary();
        log.warn(issueToBeApproved.getKey() + " needs approval");

        serviceTypeField = componentManager.getCustomFieldManager().getCustomFieldObjectByName("Service / Type");
        if (serviceTypeField == null)
            throw new WorkflowException("Service / Type custom field is not configured!");
        Object serviceAndTypeObj = issueToBeApproved.getCustomFieldValue(serviceTypeField);
        CustomFieldParams serviceAndType = null;
        if (serviceAndTypeObj != null && serviceAndTypeObj instanceof CustomFieldParams)
            serviceAndType = (CustomFieldParams) serviceAndTypeObj;
        if (serviceAndType == null)
            throw new WorkflowException("Invalid Service / Type field value!");
        Object serviceObj = serviceAndType.getFirstValueForNullKey();
        Object typeObj = serviceAndType.getFirstValueForKey("1");
        String service = null;
        String type = null;
        if (serviceObj != null && serviceObj instanceof Option)
            service = ((Option) serviceObj).getValue();
        if (typeObj != null && typeObj instanceof Option)
            type = ((Option) typeObj).getValue();
        if (service == null || type == null)
            throw new WorkflowException("Invalid Service / Type field value!");
        log.warn("service: " + service);
        log.warn("type: " + type);
        ApprovalsConfiguration approvalsConfiguration = null;
        Map<String, String> approvals = null;
        try {
            approvalsConfiguration = ApprovalsConfiguration.getInstance();
            approvals = approvalsConfiguration.getApprovalsAndApprovers(project, service, type);
        } catch (Exception e) {
            throw new WorkflowException("Unable to get approvals configuration!", e);
        }

        User user = componentManager.getJiraAuthenticationContext().getUser();
        Iterable<IssueType> allIssueTypes = componentManager.getConstantsManager().getAllIssueTypeObjects();

        String approvalType = (String) params.get(SeekApprovalActionFactory.approvalTypeKey);

        for (String approvalIssueType : approvals.keySet())
            if (approvalsConfiguration.isApprovalIssueType(approvalIssueType, "approvals.type.regex." + approvalType))
                for (IssueType potentialIssueType : allIssueTypes)
                    if (potentialIssueType.getName().equals(approvalIssueType)) {
                        MutableIssue approvalTask = componentManager.getIssueFactory().getIssue();
                        approvalTask.setProjectId(issueToBeApproved.getProjectObject().getId());
                        approvalTask.setIssueTypeId(potentialIssueType.getId());
                        approvalTask.setReporter(user);
                        approvalTask.setAssignee(componentManager.getUserUtil().getUser(
                                approvals.get(approvalIssueType)));
                        approvalTask.setSummary(approvalIssueType + " : " + summary);
                        approvalTask.setParentId(issueToBeApproved.getParentId());
                        try {
                            GenericValue createdIssue = componentManager.getIssueManager().createIssue(user,
                                    approvalTask);
                            createdIssue.store();
                            componentManager.getSubTaskManager().createSubTaskIssueLink(
                                    issueToBeApproved.getGenericValue(),
                                    createdIssue, user);
                            issueToBeApproved.store();
                        } catch (Exception e) {
                            throw new WorkflowException("Unable to create approval sub-tasks!", e);
                        }
                        break;
                    }
    }
}
