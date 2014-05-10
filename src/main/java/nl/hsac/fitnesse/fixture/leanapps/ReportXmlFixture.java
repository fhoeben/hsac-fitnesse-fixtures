package nl.hsac.fitnesse.fixture.leanapps;

import fit.ColumnFixture;
import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * Checks report triggers in Report XML documents.
 * Waits before the first retrieval of XML to allow XML generation to take place.
 */
public class ReportXmlFixture extends ColumnFixture {
    private final Environment env = Environment.getInstance();
    private boolean hasWaited = false;
    private XmlHttpResponse response;
    private String policyNr;
    private String versionNr = "1";
    private boolean isTermination = false;

    @Override
    public void execute() throws Exception {
        if (!hasWaited) {
            waitForReportXml();
            hasWaited = true;
        }

        super.execute();
        if (!StringUtils.isEmpty(policyNr)) {
            response = getReportXml(getReportXmlFilename());
        }
    }

    /**
     * Gets content of reportXmlFile
     * @param reportXmlFilename file to retrieve
     * @return report XML
     */
    private XmlHttpResponse getReportXml(String reportXmlFilename) {
        String url = LalCallColumnFixture.getLalUrl() + "/xmlrr/archive/Report/" + reportXmlFilename;
        return env.doHttpGetXml(url);
    }

    /**
     * Waits for report XML to br created (which may take a while).
     */
    private void waitForReportXml() {
        String waitTime = env.getRequiredSymbol("reportXmlWait");
        long waitDuration = Long.parseLong(waitTime);
        try {
            Thread.sleep(waitDuration);
        } catch (InterruptedException e) {
            throw new RuntimeException("Waiting for report XML interrupted", e);
        }
    }

    @Override
    public void reset() throws Exception {
        super.reset();
        response = new XmlHttpResponse();
        policyNr = null;
    }

    /**
     * @return report trigger in report XML
     */
    public String reportTrigger() {
        String foundTrigger;
        try{
          foundTrigger = response.getXPath("//LaLifeFreeParams/LaLifeFreeParam[name=\"YARDEN_REPORT_TRIGGER\"]/value");
        } catch (Exception e) {
            foundTrigger = null; 
        }
        return foundTrigger;
    }

    /**
     * @return the reportXmlFilename
     */
    public String getReportXmlFilename() {
        String postFix = ".xml";
        if (!isTermination) {
            postFix = "_1_" + versionNr + postFix;
        }
        String preFix = "policy_";
        if (isTermination) {
            preFix += "laa004_";
        }
        return preFix + getPolicyNr() +  postFix;
    }

    /**
     * @return url of report XML
     */
    public String reportTriggerUrl() {
        return response.getRequest();
    }
    /**
     * @return HTML formatted xml report content
     */
    public String reportXml() {
        return Environment.getInstance().getHtmlForXml(response.getResponse());
    }
    /**
     * @return the policyNr
     */
    public String getPolicyNr() {
        return policyNr;
    }

    /**
     * @param aPolicyNr the policyNr to set
     */
    public void setPolicyNr(String aPolicyNr) {
        policyNr = aPolicyNr;
    }

    /**
     * @param aResponse the response to set
     */
    public void setResponse(XmlHttpResponse aResponse) {
        response = aResponse;
    }

    /**
     * @return the isTermination
     */
    public boolean isTermination() {
        return isTermination;
    }

    /**
     * @param aIsTermination the isTermination to set
     */
    public void setTermination(boolean aIsTermination) {
        isTermination = aIsTermination;
    }

    /**
     * @param aVersionNr the versionNr to set
     */
    public void setVersionNr(String aVersionNr) {
        versionNr = aVersionNr;
    }
}
