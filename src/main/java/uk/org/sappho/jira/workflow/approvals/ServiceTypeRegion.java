package uk.org.sappho.jira.workflow.approvals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.opensymphony.workflow.WorkflowException;

public class ServiceTypeRegion implements ApprovalsKey {

    private String project;
    private String service;
    private String type;
    private String region;
    private List<String> requiredApprovalsTypes = null;
    private final Map<String, List<String>> approvers = new HashMap<String, List<String>>();
    private static final Pattern tableRegex = Pattern.compile("^ *\\| +([^\\|]+?) +(\\|.*)$");
    private final ComponentManager componentManager = ComponentManager.getInstance();
    private static final Logger log = Logger.getLogger(ServiceTypeRegion.class);

    public void init(Issue issue) throws WorkflowException {

        // Get project
        project = issue.getProjectObject().getKey();

        // Get service and type from custom field
        CustomField serviceTypeField =
                componentManager.getCustomFieldManager().getCustomFieldObjectByName("Service / Type");
        if (serviceTypeField == null)
            throw new WorkflowException("Service/Type custom field is not configured for this issue!");
        Object serviceAndTypeObj = issue.getCustomFieldValue(serviceTypeField);
        CustomFieldParams serviceAndType = null;
        if (serviceAndTypeObj != null && serviceAndTypeObj instanceof CustomFieldParams)
            serviceAndType = (CustomFieldParams) serviceAndTypeObj;
        if (serviceAndType == null)
            throw new WorkflowException("Invalid Service/Type field value!");
        Object serviceObj = serviceAndType.getFirstValueForNullKey();
        Object typeObj = serviceAndType.getFirstValueForKey("1");
        service = null;
        type = null;
        if (serviceObj != null && serviceObj instanceof Option)
            service = ((Option) serviceObj).getValue();
        if (typeObj != null && typeObj instanceof Option)
            type = ((Option) typeObj).getValue();
        if (service == null || type == null)
            throw new WorkflowException("Invalid Service/Type field value!");
        if (service.length() < 1)
            service = "None";
        if (type.length() < 1)
            type = "None";

        // Get region from reporting user
        region = issue.getReporter().getPropertySet().getString("region");
        if (region == null || region.length() < 1)
            region = "All";

        // Get approvals configuration page from wiki
        ApprovalsConfiguration config = ApprovalsConfiguration.getInstance();
        String configPage = config.getWikiPage(project, "wiki.approvals.config");

        // Find corresponding approvals
        Map<String, List<String>> possibleApprovals = new HashMap<String, List<String>>();
        for (String configLine : configPage.split("\n")) {
            Matcher matcher = tableRegex.matcher(configLine);
            if (matcher.matches()) {
                String firstColumn = matcher.group(1);
                if (config.isIssueType(project, ApprovalsConfiguration.allApprovalsIssueTypes, firstColumn)) {
                    // Handle personnel list for approval type
                    List<String> approversList = new ArrayList<String>();
                    boolean collecting = true;
                    while (collecting) {
                        matcher = tableRegex.matcher(matcher.group(2));
                        collecting = matcher.matches();
                        if (collecting)
                            approversList.add(matcher.group(1).toLowerCase());
                    }
                    if (approversList.size() > 0)
                        approvers.put(firstColumn, approversList);
                } else if (firstColumn.equals(service)) {
                    matcher = tableRegex.matcher(matcher.group(2));
                    if (matcher.matches() && matcher.group(1).equals(type)) {
                        // Handle service/type configurations for all region combinations
                        matcher = tableRegex.matcher(matcher.group(2));
                        if (matcher.matches()) {
                            String configurationRegion = matcher.group(1);
                            List<String> approvalsList = new ArrayList<String>();
                            boolean collecting = true;
                            while (collecting) {
                                matcher = tableRegex.matcher(matcher.group(2));
                                collecting = matcher.matches();
                                if (collecting) {
                                    String approval = matcher.group(1);
                                    if (config.isIssueType(project, ApprovalsConfiguration.allApprovalsIssueTypes,
                                            approval))
                                        approvalsList.add(approval);
                                }
                            }
                            if (approvalsList.size() > 0)
                                possibleApprovals.put(configurationRegion, approvalsList);
                        }
                    }
                }
            }
        }

        // Get approvals for specific region, falling back on All if not configured or available
        requiredApprovalsTypes = possibleApprovals.get(region);
        if (requiredApprovalsTypes == null)
            requiredApprovalsTypes = possibleApprovals.get("All");

        // Break the workflow if nothing configured
        if (requiredApprovalsTypes == null)
            throw new WorkflowException("No approvals have been configured for " + project + "/" + service + "/" + type
                    + "/" + region + "! Select a valid service/type combination.");

        // Get all valid issue type names to ensure subtasks can be raised properly
        Iterable<IssueType> allIssueTypes =
                componentManager.getConstantsManager().getAllIssueTypeObjects();
        List<String> allIssueTypeNames = new ArrayList<String>();
        for (IssueType issueType : allIssueTypes)
            allIssueTypeNames.add(issueType.getName());

        // Dump configuration to logs and check approvers
        log.warn("Approval of " + project + "/" + service + "/" + type + "/" + region + ":");
        for (String requiredApprovalType : requiredApprovalsTypes) {
            log.warn("  Requires " + requiredApprovalType);
            if (approvers.get(requiredApprovalType).size() < 1)
                throw new WorkflowException("Approval type " + requiredApprovalType
                        + " has no configured approvers! Select a valid service/type combination.");
            for (String approver : approvers.get(requiredApprovalType))
                log.warn("    By " + approver);
            if (!allIssueTypeNames.contains(requiredApprovalType))
                throw new WorkflowException("Approval type " + requiredApprovalType
                        + " is not a valid issue type! Select a valid service/type combination.");
        }
    }

    public List<String> getRequiredApprovalTypes() {

        return requiredApprovalsTypes;
    }

    public String getPrimaryApprover(String requiredApprovalType) {

        return approvers.get(requiredApprovalType).get(0);
    }

    public List<String> getAllApprovers(String requiredApprovalType) {

        return approvers.get(requiredApprovalType);
    }
}
