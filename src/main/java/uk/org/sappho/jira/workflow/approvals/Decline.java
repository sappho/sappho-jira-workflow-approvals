package uk.org.sappho.jira.workflow.approvals;

public class Decline extends ApprovalAction {

    protected String getAction() {
        return "declined";
    }

    protected String getFlag() {
        return "declined";
    }
}
