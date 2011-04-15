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

public class ServiceTypeRegionApprovalsConfiguration implements ApprovalsConfigurationPlugin {

    private List<String> requiredApprovalsTypes = null;
    private final Map<String, List<String>> approvers = new HashMap<String, List<String>>();
    private static final String regionApprovalTag = "region";
    private static final Pattern tableRegex = Pattern.compile("^ *\\| +([^\\|]+?) +(\\|.*)$");
    private static final Logger log = Logger.getLogger(ServiceTypeRegionApprovalsConfiguration.class);

    public void init(Issue issue) throws WorkflowException {

        // Check custom fields have been configured
        ComponentManager componentManager = ComponentManager.getInstance();
        CustomField serviceTypeField =
                componentManager.getCustomFieldManager().getCustomFieldObjectByName("Service / Type");
        if (serviceTypeField == null)
            throw new WorkflowException("Service/Type custom field is not configured!");
        CustomField IssueRegionField =
                componentManager.getCustomFieldManager().getCustomFieldObjectByName("Region");
        if (IssueRegionField == null)
            throw new WorkflowException("Region custom field is not configured!");

        // Get service / type from custom field only if this is a parent task
        // determined by checking for presence of a service / type field
        String service = null;
        String type = null;
        String issueRegion = null;
        String reporterRegion = null;
        Object serviceAndTypeObj = issue.getCustomFieldValue(serviceTypeField);
        if (serviceAndTypeObj != null && serviceAndTypeObj instanceof CustomFieldParams) {
            // Get service and type values
            CustomFieldParams serviceAndType = (CustomFieldParams) serviceAndTypeObj;
            Object serviceObj = serviceAndType.getFirstValueForNullKey();
            Object typeObj = serviceAndType.getFirstValueForKey("1");
            if (serviceObj != null && serviceObj instanceof Option)
                service = ((Option) serviceObj).getValue();
            if (typeObj != null && typeObj instanceof Option)
                type = ((Option) typeObj).getValue();
            if (service == null || type == null)
                throw new WorkflowException("Invalid Service/Type field value!");
            // get issue's region value
            Object issueRegionObj = issue.getCustomFieldValue(IssueRegionField);
            if (issueRegionObj == null || !(issueRegionObj instanceof String))
                throw new WorkflowException("Invalid Region field value!");
            issueRegion = (String) issueRegionObj;
            // Get region of reporting user from user properties
            reporterRegion = issue.getReporter().getPropertySet().getString("jira.meta.region");
            if (reporterRegion == null || reporterRegion.length() < 1)
                reporterRegion = "All";
            log.warn("Issue " + issue.getKey() + " has service / type / issueRegion / reporterRegion of " +
                    service + " / " + type + " / " + issueRegion + "/" + reporterRegion);
        }

        // Get this issue's project key
        String project = issue.getProjectObject().getKey();

        // Get approvals configuration page from wiki
        PluginConfiguration config = PluginConfiguration.getInstance();
        String configPage = config.getWikiPage(project, "wiki.approvals.config");

        // Get all valid issue type names to ensure subtasks can be raised properly
        Iterable<IssueType> allIssueTypes =
                componentManager.getConstantsManager().getAllIssueTypeObjects();
        List<String> allIssueTypeNames = new ArrayList<String>();
        for (IssueType issueType : allIssueTypes)
            allIssueTypeNames.add(issueType.getName());

        // Find corresponding approvals by parsing the wiki page
        Map<String, List<String>> possibleApprovals = new HashMap<String, List<String>>();
        List<String> regionApprovals = null;
        for (String configLine : configPage.split("\n")) {
            Matcher matcher = tableRegex.matcher(configLine);
            if (matcher.matches()) {
                String firstColumn = matcher.group(1);
                if (config.isIssueType(project, PluginConfiguration.allApprovalsIssueTypes, firstColumn)) {
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
                } else if (service != null) {
                    boolean isRegionApprovalTag = firstColumn.equals(regionApprovalTag);
                    if (isRegionApprovalTag || firstColumn.equals(service)) {
                        matcher = tableRegex.matcher(matcher.group(2));
                        if (matcher.matches() &&
                                ((isRegionApprovalTag && matcher.group(1).equals(regionApprovalTag)) ||
                                        matcher.group(1).equals(type))) {
                            // Handle service/type configurations for all reporter region combinations
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
                                        if (config.isIssueType(project, PluginConfiguration.allApprovalsIssueTypes,
                                                approval))
                                            approvalsList.add(approval);
                                    }
                                }
                                if (approvalsList.size() > 0)
                                    if (isRegionApprovalTag)
                                        regionApprovals = configurationRegion.equals(issueRegion) ?
                                                approvalsList : null;
                                    else
                                        possibleApprovals.put(configurationRegion, approvalsList);
                            }
                        }
                    }
                }
            }
        }

        // Get approvals for specific reporter region, falling back on All if not configured or available
        // but only do this if the issue has a service and type
        if (service != null) {
            requiredApprovalsTypes = possibleApprovals.get(reporterRegion);
            if (requiredApprovalsTypes == null)
                requiredApprovalsTypes = possibleApprovals.get("All");
            if (requiredApprovalsTypes == null)
                throw new WorkflowException("No approvals have been configured for " + project + "/" + service + "/"
                        + type + "/" + reporterRegion + "! Select a valid service/type combination.");
            if (regionApprovals != null) {
                requiredApprovalsTypes.removeAll(regionApprovals);
                requiredApprovalsTypes.addAll(regionApprovals);
            }
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
