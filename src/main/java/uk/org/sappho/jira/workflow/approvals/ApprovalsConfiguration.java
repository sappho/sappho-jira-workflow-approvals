package uk.org.sappho.jira.workflow.approvals;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

import uk.org.sappho.configuration.ConfigurationException;
import uk.org.sappho.configuration.SimpleConfiguration;
import uk.org.sappho.confluence4j.soap.ConfluenceSoapService;

public class ApprovalsConfiguration extends SimpleConfiguration {

    private final String wikiURL;
    private final String wikiUsername;
    private final String wikiPassword;
    private final String wikiSpace;
    private String wikiPagePrefix;
    private String wikiPageSuffix;
    private final Map<String, String> lastConfigPages = new HashMap<String, String>();
    protected static final Pattern tableRegex = Pattern.compile("^ *\\| +(.+?) +(\\|.*)$");
    private static final Logger log = Logger.getLogger(ApprovalsConfiguration.class);
    private static final String configurationFilename = "c:/var/jira/approvals/approvals.properties";
    public static final String allApprovalsTypeRegexKey = "approvals.type.regex.all";
    public static final String undefined = "undefined";
    private static ApprovalsConfiguration approvalsConfiguration = null;

    private ApprovalsConfiguration() throws ConfigurationException {

        super();
        log.warn("Loading approvals configuration from " + configurationFilename);
        load(configurationFilename);
        wikiURL = getProperty("wiki.url", undefined);
        wikiUsername = getProperty("wiki.username", undefined);
        wikiPassword = getProperty("wiki.password", undefined);
        wikiSpace = getProperty("wiki.approvals.config.space", undefined);
        wikiPagePrefix = getProperty("wiki.approvals.config.page.prefix", "");
        wikiPageSuffix = getProperty("wiki.approvals.config.page.suffix", "");
        log.warn("Wiki URL: " + wikiURL);
        log.warn("Wiki username: " + wikiUsername);
        log.warn("Wiki space: " + wikiSpace);
        log.warn("Wiki page prefix: " + wikiPagePrefix);
        log.warn("Wiki page suffix: " + wikiPageSuffix);
        if (wikiPagePrefix.length() > 0)
            wikiPagePrefix += " ";
        if (wikiPageSuffix.length() > 0)
            wikiPageSuffix = " " + wikiPageSuffix;
    }

    public class WikiPageConfiguration {

        private final Map<String, List<String>> requiredApprovals = new HashMap<String, List<String>>();
        private final Map<String, String> approvers = new HashMap<String, String>();

        public WikiPageConfiguration(String project) {

            String wikiPage = wikiPagePrefix + project + wikiPageSuffix;
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
                        if (isApprovalIssueType(firstColumn, allApprovalsTypeRegexKey)) {
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
                                        if (isApprovalIssueType(approval, allApprovalsTypeRegexKey))
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

    synchronized public Map<String, String> getApprovalsAndApprovers(String project, String service, String type)
            throws MalformedURLException, RemoteException, ServiceException {

        Map<String, String> approvalsAndApprovers = new HashMap<String, String>();
        WikiPageConfiguration configuration = new WikiPageConfiguration(project);
        List<String> requiredApprovals = configuration.getRequiredApprovals(service, type);
        if (requiredApprovals != null)
            for (String requiredApproval : requiredApprovals) {
                approvalsAndApprovers.put(requiredApproval, configuration.getApprover(requiredApproval));
            }
        return approvalsAndApprovers;
    }

    synchronized public String getApprover(String project, String requiredApproval) throws MalformedURLException,
            RemoteException, ServiceException {

        return new WikiPageConfiguration(project).getApprover(requiredApproval);
    }

    public boolean isApprovalIssueType(String issueType, String regexKey) {

        return Pattern.compile(getProperty(regexKey, "undefined")).matcher(issueType).matches();
    }

    synchronized public static ApprovalsConfiguration getInstance() throws ConfigurationException {

        if (approvalsConfiguration == null)
            approvalsConfiguration = new ApprovalsConfiguration();
        return approvalsConfiguration;
    }
}
