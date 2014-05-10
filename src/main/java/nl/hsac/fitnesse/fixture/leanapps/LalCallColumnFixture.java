package nl.hsac.fitnesse.fixture.leanapps;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.fit.SoapCallMapColumnFixture;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

/**
 * Base class for fixtures calling LeanApps directly.
 */
public class LalCallColumnFixture extends SoapCallMapColumnFixture<LalResponse> {
    static String getLalUrl() {
        return Environment.getInstance().getRequiredSymbol("lalUrl");
    }

    /**
     * Creates new.
     */
    public LalCallColumnFixture() {
        super(LalResponse.class);
    }

    @Override
    protected LalResponse callService() {
        String templateName = getTemplateName();
        return callLalPolicy(templateName, getCurrentRowValues());
    }

    @Override
    protected XmlHttpResponse callCheckService() {
        String checkTemplateName = getCheckTemplateName();
        return callLalPolicy(checkTemplateName, getCurrentRowValues());
    }

    public LalResponse callLalPolicy(String templateName, Object model) {
        LalResponse result = new LalResponse();
        getEnvironment().callService(getLalPolicyUrl(), templateName, model, result);
        return result;
    }

    private String getLalPolicyUrl() {
        return getLalUrl() + "/LAWebServices/PolicyService.jws";
    }

    /**
     * @return OK if no error was received, NOK otherwise.
     */
    public String errorStatus() {
        return getErrorStatus();
    }
    
    private String getErrorStatus() {
        return getRawResponse().getStatus();
    }

    /**
     * @return error message from Lal
     */
    public String error() {
        return getRawResponse().getError();
    }

}
