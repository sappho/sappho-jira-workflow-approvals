package uk.org.sappho.jira.workflow.approvals;

public class Reconsider extends ApprovalAction {

    protected String getAction() {
        return "is reconsidering";
    }

    protected String getFlag() {
        return "waiting";
    }
}
