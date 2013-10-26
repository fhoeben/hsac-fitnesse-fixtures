package nl.hsac.fitnesse.fixture.fit;

/**
 * Base class for fixtures that call a service and then another based on Freemarker templates.
 * @param <Response> class expected as response to original call.
 * @param <CheckResponse> class expected as response to check call (if any).
 */
public abstract class TemplateBasedMapColumnFixture<Response, CheckResponse> extends ServiceAndCheckMapColumnFixture<Response,CheckResponse> {
    private String templateName;
    private String checkTemplateName;

    public TemplateBasedMapColumnFixture(Class<? extends Response> aResponseClass,
                                         Class<? extends CheckResponse> aCheckResponseClass) {
        super(aResponseClass, aCheckResponseClass);
    }

    /**
     * @return the templateName
     */
    public String getTemplateName() {
        return templateName;
    }

    /**
     * @param aTemplateName the templateName to set
     */
    public void setTemplateName(String aTemplateName) {
        templateName = aTemplateName;
    }

    /**
     * @return the checkTemplateName
     */
    public String getCheckTemplateName() {
        return checkTemplateName;
    }

    /**
     * @param aCheckTemplateName the checkTemplateName to set
     */
    public void setCheckTemplateName(String aCheckTemplateName) {
        checkTemplateName = aCheckTemplateName;
    }

}
