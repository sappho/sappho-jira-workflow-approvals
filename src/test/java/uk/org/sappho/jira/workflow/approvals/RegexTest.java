package uk.org.sappho.jira.workflow.approvals;

import static org.junit.Assert.assertTrue;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.regex.Matcher;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;
import org.junit.Test;

public class RegexTest {

    private static final Logger log = Logger.getLogger(RegexTest.class);

    @Test
    public void shouldEvaluate() {

        Matcher matcher = ApprovalsConfiguration.tableRegex.matcher("| xx | z | qwert | fred |");
        assertTrue(matcher.matches());
        String first = matcher.group(1);
        String rest = matcher.group(2);
        assertTrue(first.equals("xx"));
        assertTrue(rest.equals("| z | qwert | fred |"));
    }

    @Test
    public void shouldGetConfig() throws MalformedURLException, RemoteException, ServiceException {

        ApprovalsConfiguration approvalsConfiguration = new ApprovalsConfiguration("http://wiki.catlin.com",
                "build_dev", "Build_D3v", "SYSCON", "", "Approvals");
        Map<String, String> approvals = approvalsConfiguration.getApprovalsAndApprovers("CRM", "Application",
                "theFrame");
        for (String approval : approvals.keySet())
            log.warn(approval + " -> " + approvals.get(approval));
    }
}
