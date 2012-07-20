package uk.org.sappho.jira.workflow.approvals;

import org.apache.log4j.Logger;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;

public class AllEventsListener {

    protected static final Logger log = Logger.getLogger(AllEventsListener.class);

    public AllEventsListener(EventPublisher eventPublisher) {

        // Note: See https://developer.atlassian.com/display/JIRADEV/Plugin+Tutorial+-+Writing+JIRA+event+listeners+with+the+atlassian-event+library
        // for more on this when this plugin moves to v2.
        eventPublisher.register(this);
    }

    @EventListener
    public void onIssueEvent(IssueEvent issueEvent) {

        try {
            if (issueEvent.getEventTypeId().equals(EventType.ISSUE_COMMENTED_ID)) {
                Issue subTask = issueEvent.getIssue();
                Issue parentIssue = subTask.getParentObject();
                if (parentIssue != null) {
                    if (PluginConfiguration.getInstance().isConfiguredProject(parentIssue.getProjectObject().getKey())) {
                        Comment comment = issueEvent.getComment();
                        ComponentManager.getInstance().getCommentManager()
                                .create(parentIssue, comment.getUpdateAuthor(),
                                        "Copied from sub-task " + subTask.getKey() + ": " + comment.getBody(), true);
                    }
                }
            }
        } catch (Throwable t) {
            log.error("Unable to process Jira event", t);
        }
    }
}
