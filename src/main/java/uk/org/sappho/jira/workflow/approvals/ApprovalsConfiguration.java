package uk.org.sappho.jira.workflow.approvals;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.opensymphony.workflow.WorkflowException;

import uk.org.sappho.configuration.SimpleConfiguration;
import uk.org.sappho.confluence4j.soap.ConfluenceSoapService;

public class ApprovalsConfiguration extends SimpleConfiguration {

    private final Pattern projectsRegex;
    private final String wikiURL;
    private final String wikiUsername;
    private final String wikiPassword;
    private final Map<String, String> lastConfigPages = new HashMap<String, String>();
    protected static final Pattern tableRegex = Pattern.compile("^ *\\| +(.+?) +(\\|.*)$");
    private static final Logger log = Logger.getLogger(ApprovalsConfiguration.class);
    private static final String configurationFilename = "c:/var/jira/approvals/approvals.properties";
    public static final String allApprovalsIssueTypes = "all";
    public static final String undefined = "undefined";
    private static ApprovalsConfiguration approvalsConfiguration = null;

    private ApprovalsConfiguration() throws WorkflowException {

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

    public class WikiPageApprovalsConfiguration {

        private final Map<String, List<String>> requiredApprovals = new HashMap<String, List<String>>();
        private final Map<String, String> approvers = new HashMap<String, String>();

        public WikiPageApprovalsConfiguration(String project) {

            String wikiSpace = getProperty(project, "wiki.approvals.config.space", undefined);
            String wikiPage = getProperty(project, "wiki.approvals.config.page", undefined);
            String description = wikiSpace + ":" + wikiPage + " from " + wikiURL;
            log.warn("Reading " + description);
            String configPage = lastConfigPages.get(project);
            try {
                ConfluenceSoapService confluenceSoapService = new ConfluenceSoapService(wikiURL, wikiUsername,
                        wikiPassword);
                configPage = confluenceSoapService.getService().getPage(confluenceSoapService.getToken(),
                        wikiSpace, wikiPage).getContent();
                lastConfigPages.put(project, configPage);
            } catch (Throwable t) {
                log.error("Ubable to load " + description, t);
                if (configPage != null)
                    log.warn("Using previously loaded configuration");
            }
            if (configPage == null)
                log.error("No configuration available!");
            else {
                for (String configLine : configPage.split("\n")) {
                    Matcher matcher = tableRegex.matcher(configLine);
                    if (matcher.matches()) {
                        String firstColumn = matcher.group(1);
                        String restOfLine = matcher.group(2);
                        if (isIssueType(project, allApprovalsIssueTypes, firstColumn)) {
                            String approval = firstColumn;
                            matcher = tableRegex.matcher(restOfLine);
                            if (matcher.matches()) {
                                String approver = matcher.group(1);
                                approvers.put(approval, approver);
                                log.info("Approver for " + approval + " is " + approver);
                            }
                        } else {
                            String service = firstColumn;
                            matcher = tableRegex.matcher(restOfLine);
                            if (matcher.matches()) {
                                String type = matcher.group(1);
                                List<String> approvalsList = new ArrayList<String>();
                                while (true) {
                                    restOfLine = matcher.group(2);
                                    matcher = tableRegex.matcher(restOfLine);
                                    if (matcher.matches()) {
                                        String approval = matcher.group(1);
                                        if (isIssueType(project, allApprovalsIssueTypes, approval))
                                            approvalsList.add(approval);
                                    } else
                                        break;
                                }
                                requiredApprovals.put(combinedServiceAndType(service, type), approvalsList);
                                log.info("Approval of " + service + " - " + type);
                                for (String approval : approvalsList)
                                    log.info(" requires " + approval);
                            }
                        }
                    }
                }
            }
        }

        public List<String> getRequiredApprovals(String service, String type) {

            return requiredApprovals.get(combinedServiceAndType(service, type));
        }

        public String getApprover(String requiredApproval) {

            return approvers.get(requiredApproval);
        }

        private String combinedServiceAndType(String service, String type) {

            return service + " - " + type;
        }
    }

    synchronized public Map<String, String> getApprovalsAndApprovers(String project, String service, String type) {

        Map<String, String> approvalsAndApprovers = new HashMap<String, String>();
        WikiPageApprovalsConfiguration configuration = new WikiPageApprovalsConfiguration(project);
        List<String> requiredApprovals = configuration.getRequiredApprovals(service, type);
        if (requiredApprovals != null)
            for (String requiredApproval : requiredApprovals) {
                String approver = configuration.getApprover(requiredApproval);
                approvalsAndApprovers.put(requiredApproval, approver);
                log.warn("Required approval: " + requiredApproval + " by " + approver);
            }
        return approvalsAndApprovers;
    }

    synchronized public String getApprover(String project, String requiredApproval) {

        return new WikiPageApprovalsConfiguration(project).getApprover(requiredApproval);
    }

    public boolean isIssueType(String project, String key, String issueType) {

        return isRegexMatch(project, "issue.types." + key, issueType);
    }

    public boolean isRegexMatch(String project, String key, String value) {

        return Pattern.compile(getProperty(project, key, undefined)).matcher(value).matches();
    }

    public boolean isConfiguredProject(String project) {

        return projectsRegex.matcher(project).matches();
    }

    public String getProperty(String project, String key, String defaultValue) {

        return getProperty(project + "." + key, defaultValue);
    }

    synchronized public static ApprovalsConfiguration getInstance() throws WorkflowException {

        if (approvalsConfiguration == null)
            approvalsConfiguration = new ApprovalsConfiguration();
        return approvalsConfiguration;
    }
}
