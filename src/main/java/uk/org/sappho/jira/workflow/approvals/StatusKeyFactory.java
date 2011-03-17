package uk.org.sappho.jira.workflow.approvals;

@SuppressWarnings("unchecked")
public class StatusKeyFactory extends SingleStringFactory {

    public static final String statusKey = "statusKey";

    public StatusKeyFactory() {

        super(statusKey);
    }
}
