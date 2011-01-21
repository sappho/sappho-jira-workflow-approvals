package uk.org.sappho.jira.workflow.approvals;

public class Approve extends ApprovalAction {

    protected String getAction() {
        return "granted";
    }

    protected String getFlag() {
        return "approved";
    }
}
