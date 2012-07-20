package uk.org.sappho.jira.workflow.approvals;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import uk.org.sappho.configuration.SimpleConfiguration;
import uk.org.sappho.confluence4j.soap.ConfluenceSoapService;

import com.atlassian.jira.issue.Issue;
import com.opensymphony.workflow.WorkflowException;

public class PluginConfiguration extends SimpleConfiguration {

    private final Pattern projectsRegex;
    private final String wikiURL;
    private final String wikiUsername;
    private final String wikiPassword;
    private final Map<String, String> cachedWikiPages = new HashMap<String, String>();
    private static final Logger log = Logger.getLogger(PluginConfiguration.class);
    private static final String configurationFilename = "c:/var/jira/approvals/approvals.properties";
    public static final String allApprovalsIssueTypes = "all";
    public static final String undefined = "undefined";
    private static PluginConfiguration pluginConfiguration = null;

    public PluginConfiguration(String configurationFilename) throws WorkflowException {

        super();
        try {
            log.warn("Loading approvals configuration from " + configurationFilename);
            load(configurationFilename);
            projectsRegex = Pattern.compile(getProperty("projects"));
            wikiURL = getProperty("wiki.url");
            wikiUsername = getProperty("wiki.username");
            wikiPassword = getProperty("wiki.password");
            log.warn("Wiki URL: " + wikiURL);
            log.warn("Wiki username: " + wikiUsername);
        } catch (Throwable t) {
            throw new WorkflowException("Unable to get approvals plugin configuration!", t);
        }
    }

    public boolean isIssueType(String project, String key, String issueType) {

        return isRegexMatch(project, "issue.types." + key, issueType);
    }

    public boolean isRegexMatch(String project, String key, String value) {

        return Pattern.compile(getProperty(project, key, undefined)).matcher(value).matches();
    }

    public boolean isNotApproved(String project, Issue subTask) {

        return !pluginConfiguration.isRegexMatch(project, "statuses.approved", subTask.getStatusObject().getName());
    }

    public boolean isConfiguredProject(String project) {

        return projectsRegex.matcher(project).matches();
    }

    public String getProperty(String project, String key, String defaultValue) {

        return getProperty(project + "." + key, defaultValue);
    }

    public List<String> getPropertyList(String project, String key) throws WorkflowException {

        try {
            return getPropertyList(project + "." + key);
        } catch (Throwable t) {
            throw new WorkflowException("Unable to get plugin configuration!", t);
        }
    }

    public String getWikiPage(String project, String wikiKey) {

        String wikiSpaceName = getProperty(project, wikiKey + ".space", PluginConfiguration.undefined);
        String wikiPageName = getProperty(project, wikiKey + ".page", PluginConfiguration.undefined);
        String description = wikiSpaceName + ":" + wikiPageName + " from " + wikiURL;
        log.warn("Loading " + description);
        // Is the last page read cached? Use the cached value as a default
        String wikiPage = cachedWikiPages.get(description);
        try {
            // Attempt an update from the wiki
            ConfluenceSoapService confluenceSoapService =
                    new ConfluenceSoapService(wikiURL, wikiUsername, wikiPassword);
            wikiPage = confluenceSoapService.getService().getPage(confluenceSoapService.getToken(), wikiSpaceName,
                    wikiPageName).getContent();
            // Cache successfully read page
            cachedWikiPages.put(description, wikiPage);
        } catch (Throwable t) {
            log.error("Unable to load " + description, t);
            if (wikiPage != null)
                log.warn("Using previously loaded page content");
        }
        if (wikiPage == null) {
            wikiPage = "";
            log.warn("No page content available!");
        }
        return wikiPage;
    }

    public ApprovalsConfigurationPlugin getApprovalsConfigurationPlugin(Issue issue) throws WorkflowException {

        ApprovalsConfigurationPlugin approvalsConfigurationPlugin;
        try {
            Class<?> clazz = Class.forName(getProperty(issue.getProjectObject().getKey(),
                    "approvals.configuration.plugin.class", undefined));
            approvalsConfigurationPlugin = (ApprovalsConfigurationPlugin) clazz.newInstance();
        } catch (Throwable t) {
            log.error("Unable to get approvals plugin configuration!", t);
            throw new WorkflowException("Unable to get approvals plugin configuration!", t);
        }
        approvalsConfigurationPlugin.init(issue);
        return approvalsConfigurationPlugin;
    }

    synchronized public static PluginConfiguration getInstance() throws WorkflowException {

        if (pluginConfiguration == null)
            pluginConfiguration = new PluginConfiguration(configurationFilename);
        return pluginConfiguration;
    }
}
