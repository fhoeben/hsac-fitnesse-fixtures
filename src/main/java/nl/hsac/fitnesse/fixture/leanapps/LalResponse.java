package nl.hsac.fitnesse.fixture.leanapps;

import nl.hsac.fitnesse.fixture.Environment;
import nl.hsac.fitnesse.fixture.util.XPathCheckResult;
import nl.hsac.fitnesse.fixture.util.XmlHttpResponse;

import java.util.List;

/**
 * SOAP response from LeanApps Life.
 */
public class LalResponse extends XmlHttpResponse {
    /** Namespace prefix as present in response (which we normally know as lal:, but sometimes the literal value is needed.)*/
    private final static String NAMESPACE_PREFIX = "ns:";

    @Override
    public void validResponse() {
        super.validResponse();
        if (!"OK".equals(getStatus())) {
            String response = getResponse();
            Environment.handleErrorResponse("NOK response received: ", response);
        }
    }

    /**
     * @return response status by LeanApps
     */
    public String getStatus() {
        return getRawXPath(getResponse(), "//*[local-name()='status']/*[local-name()='status']");
    }


    public String getError() {
        return getRawXPath(getResponse(), "//*[local-name()='error']/*[local-name()='message']");
    }
    
    /**
     * Gets the value xsi:type attribute will have for provided type.
     * @param type sub type wanted.
     * @return type name including namespace prefix as present in response.
     */
    @Override
    public String getXsiTypeValue(String type) {
        return NAMESPACE_PREFIX + type;
    }

    // xpath location of insurance in LalResponse-xml.
    protected static String INSURANCE_SELECT = "/env:Envelope/env:Body/lal:getPoliciesBody/lal:policiesBody/lal:policies/lal:policy/lal:insurances/lal:insurance";
    protected static String POLICY_SELECT = "/env:Envelope/env:Body/lal:getPoliciesBody/lal:policiesBody/lal:policies/lal:policy";

    protected static String getInsuranceSelect() {
        return INSURANCE_SELECT;
    }

    protected static String getPolicySelect() {
        return POLICY_SELECT;
    }

    public String paymentFrequency() {
        return getLalResponse().getXPath(getPremiumSelect() + "/lal:frequency");
    }

    public LalResponse getLalResponse() {
        return this;
    }

    public Double premiumAmount() {
        return getXPathAmount(getPremiumSelect() + "/lal:amount");
    }

    public Double insuredAmount() {
        return getXPathAmount(deathCoverageSelect() + "/lal:amount");
    }

    public String deathCoverageSelect() {
        return getInsuranceSelect() + getDeathCoverageSubselect();
    }

    public static String getDeathCoverageSubselect() {
        return LalPolicyXPaths.getDeathCoverageSubselect();
    }

    public String premiumEndDate() {
        return getXPathDate(getPremiumSelect() + "/lal:endDate");
    }

    public String intermediary() {
        String roleType = getLalResponse().getXsiTypeValue("InsuranceIntermediary");
        return getLalResponse().getXPath(
                getInsuranceSelect() + "/lal:roles/lal:role[@xsi:type='%s']/lal:partyId/lal:key", roleType);
    }


    public String productionCompany() {
        String roleType = getLalResponse().getXsiTypeValue("InsuranceInstitute");
        return getLalResponse().getXPath(
                getInsuranceSelect() + "/lal:roles/lal:role[@xsi:type='%s']/lal:partyId/lal:key", roleType);
    }

    
    public String collectionMethod() {
        return getXPath(getPremiumPayerSelect() + "/lal:collectionMethod");
    }

    public String birthDate(String partyNr) {
        return getXPathDate(LalPolicyXPaths.partyDateOfBirth(partyNr));
    }

    public boolean containsPolicy(String policyNr) {
        Double countPolicy = getLalResponse().getXPathAmount(
                "count(" + getPolicySelect() + "/lal:id/lal:key[text()= '" + policyNr + "'])");
        if (countPolicy > 0.0d) {
            return true;
        } else {
            return false;
        }

    }

    public XPathCheckResult containsPolicies(List<String> policyNrList) {
        XPathCheckResult result = new XPathCheckResult();
        int i = 0;
        for (String currentPolicyNr : policyNrList) {
            i++;
            if (!containsPolicy(currentPolicyNr)) {
                result.addMisMatch(Integer.toString(i), currentPolicyNr, null);
            }
        }
        return result;

    }

    protected String getPremiumPayerSelect() {
        return getPremiumSelect() + "/lal:premiumPayers/lal:premiumPayer";
    }

    protected String getPremiumSelect() {
        return getInsuranceSelect() + getPremiumSubSelect();
    }

    protected String getSinglePaymentSelect(){
        return getInsuranceSelect() + getSinglePaymentSubSelect();
    }
    
    protected String getPremiumSubSelect() {
        if (isPremiumPremium()) {
            return getPremiumPremiumSubSelect();
        } else {
            return getSinglePremiumSubSelect();
        }

    }

    protected boolean isPremiumPremium() {
        Double premiumOid = getLalResponse().getXPathAmount(
                getInsuranceSelect() + getPremiumPremiumSubSelect() + "/lal:id/lal:oid");
        if (premiumOid != null) {
            if (premiumOid > 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }

    public Double premiumPremiumOid() {
        return getXPathAmount(getInsuranceSelect() + getPremiumPremiumSubSelect() + "/lal:id/lal:oid");
    }

    protected String getPremiumPremiumSubSelect() {
        return "/lal:premiums/lal:premium";
    }

    protected String getSinglePremiumSubSelect() {
        return "/lal:singlePremiums/lal:singlePremium";
    }
    
    protected String getSinglePaymentSubSelect() {
        return "/lal:singlePayments/lal:singlePayment";
    }

    protected String getFinancialBooking() {
       return getInsuranceSelect() + "/lal:financialBookings/lal:financialBooking";
    }

    
    protected String getPoliciesSelect() {
        return "/env:Envelope/env:Body/lal:getPoliciesBody/lal:policiesBody/lal:policies";
    }
    
    protected String getInsuranceStatedSelect(){
        return getPoliciesSelect() + "/lal:policy/lal:insurances/lal:insurance/lal:versionInfo/lal:currentVersion/lal:state";
    }
    
    public Double sumFinancialBookingForSinglePayment() {
        return getXPathAmount(
                        "sum("
                                + getFinancialBooking()
                                + "[lal:transactionCategory/text()='SINGLE_PAYMENT' and lal:state/text() = 'RECEIVABLE_STATE_CREATED']/lal:amount)");
    }

    public Double sumFinancialBooking() {
        return getXPathAmount("sum(" + getFinancialBooking() + "/lal:amount)");
    }


    public Double sumFinancialBookingDeath() {
        return getXPathAmount("sum(" + getFinancialBooking() + "[lal:transactionCategory/text()='DEATH']/lal:amount)");
    }
    
    public Double singlePayment(){
        return getXPathAmount(getSinglePaymentSelect() + "/lal:amount");
    }
    
    public Double financialBooking() {
        return getXPathAmount(getFinancialBooking() + "/lal:amount");
    }

    public Double numberInsuranceTerminatedDeath() {
        return getXPathAmount("count(" + getInsuranceStatedSelect() + "[text()='TERMINATED_DEATH'])" );
    }

    public Double numberInsuranceInForce() {
        return  getXPathAmount("count(" + getInsuranceStatedSelect() + "[text()='IN_FORCE'])" );
    }

    public String insuranceState() {
        return getXPath(getInsuranceStatedSelect() );
    }

    public String getInsured(){
        return getXPath(LalPolicyXPaths.partyInRole(this, "InsuranceInsured"));
    }

    public String getPolicyHolder(){
        return getXPath(LalPolicyXPaths.partyInRole(this, "InsurancePolicyHolder"));
    }
    
}
