package uk.org.sappho.jira.workflow.approvals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.opensymphony.workflow.WorkflowException;

public class ServiceTypeRegion implements ApprovalsKey {

    private String project = null;
    private String service = null;
    private String type = null;
    private String region = null;
    private List<String> requiredApprovalsTypes = null;
    private final Map<String, List<String>> approvers = new HashMap<String, List<String>>();
    private static final Pattern tableRegex = Pattern.compile("^ *\\| +([^\\|]+?) +(\\|.*)$");
    private final ComponentManager componentManager = ComponentManager.getInstance();

    public void init(Issue issue) throws WorkflowException {

        // Check service / type custom field has been configured
        CustomField serviceTypeField =
                componentManager.getCustomFieldManager().getCustomFieldObjectByName("Service / Type");
        if (serviceTypeField == null)
            throw new WorkflowException("Service/Type custom field is not configured!");

        // Get service / type from custom field
        Object serviceAndTypeObj = issue.getCustomFieldValue(serviceTypeField);
        if (serviceAndTypeObj != null && serviceAndTypeObj instanceof CustomFieldParams) {
            // Get service and type values which are the main key
            CustomFieldParams serviceAndType = (CustomFieldParams) serviceAndTypeObj;
            Object serviceObj = serviceAndType.getFirstValueForNullKey();
            Object typeObj = serviceAndType.getFirstValueForKey("1");
            if (serviceObj != null && serviceObj instanceof Option)
                service = ((Option) serviceObj).getValue();
            if (typeObj != null && typeObj instanceof Option)
                type = ((Option) typeObj).getValue();
            if (service == null || type == null)
                throw new WorkflowException("Invalid Service/Type field value!");
            // Get region from reporting user to add to key
            region = issue.getReporter().getPropertySet().getString("region");
            if (region == null || region.length() < 1)
                region = "All";
        }

        // Get this issue's project key
        project = issue.getProjectObject().getKey();

        // Get approvals configuration page from wiki
        ApprovalsConfiguration config = ApprovalsConfiguration.getInstance();
        String configPage = config.getWikiPage(project, "wiki.approvals.config");

        // Get all valid issue type names to ensure subtasks can be raised properly
        Iterable<IssueType> allIssueTypes =
                componentManager.getConstantsManager().getAllIssueTypeObjects();
        List<String> allIssueTypeNames = new ArrayList<String>();
        for (IssueType issueType : allIssueTypes)
            allIssueTypeNames.add(issueType.getName());

        // Find corresponding approvals by parsing the wiki page
        Map<String, List<String>> possibleApprovals = new HashMap<String, List<String>>();
        for (String configLine : configPage.split("\n")) {
            Matcher matcher = tableRegex.matcher(configLine);
            if (matcher.matches()) {
                String firstColumn = matcher.group(1);
                if (config.isIssueType(project, ApprovalsConfiguration.allApprovalsIssueTypes, firstColumn)) {
                    // Handle personnel list for approval type
                    if (!allIssueTypeNames.contains(firstColumn))
                        throw new WorkflowException("Approval type " + firstColumn + " is not a valid issue type!");
                    List<String> approversList = new ArrayList<String>();
                    boolean collecting = true;
                    while (collecting) {
                        matcher = tableRegex.matcher(matcher.group(2));
                        collecting = matcher.matches();
                        if (collecting) {
                            String username = matcher.group(1).trim().toLowerCase();
                            if (username.length() > 0)
                                approversList.add(username);
                        }
                    }
                    if (approversList.size() < 1)
                        throw new WorkflowException("Approval type " + firstColumn + " has no approvers!");
                    approvers.put(firstColumn, approversList);
                } else if (service != null && firstColumn.equals(service)) {
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
        // but only do this if the issue has a service and type
        if (service != null) {
            requiredApprovalsTypes = possibleApprovals.get(region);
            if (requiredApprovalsTypes == null)
                requiredApprovalsTypes = possibleApprovals.get("All");
            if (requiredApprovalsTypes == null)
                throw new WorkflowException("No approvals have been configured for " + project + "/" + service + "/"
                        + type + "/" + region + "! Select a valid service/type combination.");
        }
    }

    public List<String> getRequiredApprovalTypes() {

        return requiredApprovalsTypes;
    }

    public String getPrimaryApprover(String requiredApprovalType) {

        return approvers.get(requiredApprovalType).get(0);
    }

    public List<String> getAllowedApprovers(String requiredApprovalType) {

        return approvers.get(requiredApprovalType);
    }
}
