package uk.org.sappho.jira.workflow.approvals;

public class IssueApprovedCondition extends IssueApprovalCondition {

    @Override
    protected boolean isApprovedCondition() {

        return true;
    }
}
