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

import uk.org.sappho.confluence4j.soap.ConfluenceSoapService;

public class ApprovalsConfiguration {

    private final String wikiURL;
    private final String wikiUsername;
    private final String wikiPassword;
    private final String wikiSpace;
    private final String wikiPagePrefix;
    private final String wikiPageSuffix;
    private final Map<String, String> lastConfigPages = new HashMap<String, String>();
    private final Pattern approvalIssueTypeRegex;
    protected static final Pattern tableRegex = Pattern.compile("^ *\\| +(.+?) +(\\|.*)$");
    private static final Logger log = Logger.getLogger(ApprovalsConfiguration.class);

    public ApprovalsConfiguration(String wikiURL, String wikiUsername, String wikiPassword, String wikiSpace,
            String wikiPagePrefix, String wikiPageSuffix, String approvalIssueTypeRegex) {

        if (wikiPagePrefix.length() > 0)
            wikiPagePrefix += " ";
        if (wikiPageSuffix.length() > 0)
            wikiPageSuffix = " " + wikiPageSuffix;
        this.wikiURL = wikiURL;
        this.wikiUsername = wikiUsername;
        this.wikiPassword = wikiPassword;
        this.wikiSpace = wikiSpace;
        this.wikiPagePrefix = wikiPagePrefix;
        this.wikiPageSuffix = wikiPageSuffix;
        this.approvalIssueTypeRegex = Pattern.compile(approvalIssueTypeRegex);
    }

    public class Configuration {

        private final Map<String, List<String>> requiredApprovals = new HashMap<String, List<String>>();
        private final Map<String, String> approvers = new HashMap<String, String>();

        public Configuration(String project) {

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
                        if (isApprovalIssueType(firstColumn)) {
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
                                        if (isApprovalIssueType(approval))
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
        Configuration configuration = new Configuration(project);
        List<String> requiredApprovals = configuration.getRequiredApprovals(service, type);
        for (String requiredApproval : requiredApprovals) {
            approvalsAndApprovers.put(requiredApproval, configuration.getApprover(requiredApproval));
        }
        return approvalsAndApprovers;
    }

    synchronized public String getApprover(String project, String requiredApproval) throws MalformedURLException,
            RemoteException, ServiceException {

        return new Configuration(project).getApprover(requiredApproval);
    }

    public boolean isApprovalIssueType(String issueType) {

        return approvalIssueTypeRegex.matcher(issueType).matches();
    }
}
