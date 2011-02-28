package uk.org.sappho.jira.workflow.approvals;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.user.User;
import com.opensymphony.workflow.WorkflowException;

public class SeekApprovalAction extends AbstractJiraFunctionProvider {

    private static final Logger log = Logger.getLogger(SeekApprovalAction.class);
    private static final Pattern tableRegex = Pattern.compile("^ *| +([:\\-A-Za-z][ :\\-A-Za-z]) +|(.+|)$");

    @SuppressWarnings("unchecked")
    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {

        try {
            MutableIssue issueToBeApproved = (MutableIssue) transientVars.get("issue");
            log.warn(issueToBeApproved.getKey() + " needs approval");
            Collection<GenericValue> components = issueToBeApproved.getComponents();
            for (GenericValue component : components) {
                String componentName = component.getString("name");
                log.warn("Component: " + componentName);
            }
            ComponentManager componentManager = ComponentManager.getInstance();
            User user = componentManager.getJiraAuthenticationContext().getUser();

            /**
            ConfluenceSoapService confluenceSoapService = new ConfluenceSoapService("http://wiki.catlin.com",
                    "build_dev", "Build_D3v");
            String configPage = confluenceSoapService.getService().getPage(confluenceSoapService.getToken(), "SYSCON",
                    "CRM Approvals").getContent();
            log.warn("Config page:\n" + configPage);
            **/

            MutableIssue approvalTask = componentManager.getIssueFactory().getIssue();
            approvalTask.setProjectId(issueToBeApproved.getProjectObject().getId());
            approvalTask.setIssueTypeId("32");
            approvalTask.setAssignee(user);
            approvalTask.setReporter(user);
            approvalTask.setSummary("Get approval");
            approvalTask.setDescription("Test 123");
            approvalTask.setParentId(issueToBeApproved.getParentId());

            GenericValue createdIssue = componentManager.getIssueManager().createIssue(user, approvalTask);
            createdIssue.store();
            componentManager.getSubTaskManager().createSubTaskIssueLink(issueToBeApproved.getGenericValue(),
                    createdIssue, user);
            issueToBeApproved.store();
        } catch (Throwable t) {
            throw new WorkflowException("Unable to create approvals tasks", t);
        }
    }
}
