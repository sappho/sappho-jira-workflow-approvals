package uk.org.sappho.jira.workflow.approvals;

import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.workflow.WorkflowException;

public interface ApprovalsConfigurationPlugin {

    public void init(Issue issue) throws WorkflowException;

    public List<String> getRequiredApprovalTypes();

    public List<String> getAllowedApprovers(String requiredApprovalType);
}
