package uk.org.sappho.jira.workflow.approvals;

public class IssueNeedsApprovalCondition extends IssueApprovalCondition {

    @Override
    protected boolean isApprovedCondition() {

        return false;
    }
}
