package nl.hsac.fitnesse.fixture.leanapps;

import nl.hsac.fitnesse.fixture.Environment;

/**
 * Helper to provide recurring XPaths to get information from LAL getPolicy response.
 */
public class LalPolicyXPaths {
    private final static String NAMESPACE_PREFIX = "ns:";

    /**
     * Registers businesslayer namespace with Environment.
     */
    public static void registerNamespace() {
        Environment.getInstance().registerNamespace("lal", "http://www.leanapps.com/businesslayer/xml");
    }
    
    /**
     * Gets the value xsi:type attribute will have for provided type.
     * @param type sub type wanted.
     * @return type name including namespace prefix as present in response.
     */

    public static String getXsiTypeValue(String type) {
        return NAMESPACE_PREFIX + type;
    }
    public static String partyInitials(String partyNr) {
        return getPartySelect(partyNr) + "lal:initials";
    }

    public static String partyFirstName(String partyNr) {
        return getPartySelect(partyNr) + "lal:firstName";
    }

    public static String partyMiddleName(String partyNr) {
        return getPartySelect(partyNr) + "lal:middleName";
    }

    public static String partyLastName(String partyNr) {
        return getPartySelect(partyNr) + "lal:lastName";
    }

    public static String partyDateOfBirth(String partyNr) {
        return getPartySelect(partyNr) + "lal:dateOfBirth";
    }

    public static String partyGender(String partyNr) {
        return getPartySelect(partyNr) + "lal:gender";
    }

    public static String partyBSN(String partyNr) {
        return getPartySelect(partyNr) + "lal:socialSecurityNr";
    }

    public static String partyStreet(String partyNr) {
        return getPartyStreet(partyNr) + "lal:streetName";
    }

    public static String partyStreetNr(String partyNr) {
        return getPartyStreet(partyNr) + "lal:streetNumber";
    }

    public static String partyZipCode(String partyNr) {
        return getPartyAddress(partyNr) + "lal:zipCode";
    }

    public static String partyCity(String partyNr) {
        return getPartyAddress(partyNr) + "lal:city";
    }

    public static String partyCountryCode(String partyNr) {
        return getPartyAddress(partyNr) + "lal:countryCode";
    }

    public static String partyPhone(String partyNr) {
        return getPartyAddress(partyNr) + "lal:phone";
    }

    public static String partyEmail(String partyNr) {
        return getPartyAddress(partyNr) + "lal:email";
    }

    public static String partyAccountType(String partyNr, String accountId) {
        return getAccount(partyNr, accountId) + "@xsi:type";
    }
    
    public static String partyAccountNumber(String partyNr, String accountId) {
        return getAccount(partyNr, accountId) + "lal:accountNumber";
    }
    
    public static String partyAccountCity(String partyNr, String accountId) {
        return getAccount(partyNr, accountId) + "lal:city";
    }

    public static String partyBlockCorrespondence(String partyNr) {
        String partySelect = getPartySelect(partyNr);
        return partySelect + "lal:attributes/lal:attribute[lal:specificationKey='BLOCK_CORRESPONDENCE']/lal:boolean";
    }
    
    public static String premium() {
        return getInsuranceSelect() + "lal:premiums/lal:premium/";
    }
    
    public static String insuranceAccount() {
        return getInsuranceSelect() + "lal:insuranceAccount/";
    }
    
    public static String equalPremium() {
        //xsi:type="ns:EqualPremium"
        return getInsuranceSelect() + "lal:premiums/lal:premium[@xsi:type='ns:EqualPremium']/";
    }
    
    public static String singlePremium() {
        return getInsuranceSelect() + "lal:singlePremiums/lal:singlePremium/";
    }
    
    public static String yslKind() {
        return getInsuranceSelect() + "/lal:attributes/lal:attribute[lal:specificationKey/text() = 'YSL_KIND']/lal:boolean";
    }
    

    public static String insuredAmount() {
        return deathCoverageSelect() + "/lal:amount";
    }
    

    public static String getDeathCoverageSubselect() {
        return "/lal:deathCoverages/lal:deathCoverage";
    }
    
    public static String deathCoverageSelect() {
        return getInsuranceSelect() + getDeathCoverageSubselect();
    }
    
    public static String sumFinancialBookingForSinglePayment() {
        return "sum(" + getFinancialBooking()
                                + "[lal:transactionCategory/text()='SINGLE_PAYMENT' and lal:state/text() = 'RECEIVABLE_STATE_CREATED']/lal:amount)";
    }
    
    public static String singlePayment(){
        return getSinglePaymentSelect() + "/lal:amount";
    }
    
    public static String intermediary(LalResponse lalResponse) {
        return partyInRole(lalResponse, "InsuranceIntermediary");
    }

    public static String partyInRole(LalResponse lalResponse, String role) {
        String roleType = lalResponse.getXsiTypeValue(role);
        return getInsuranceSelect() + "/lal:roles/lal:role[@xsi:type='"+ roleType +"']/lal:partyId/lal:key";
    }
    

    public static String productionCompany() {
        String roleType = getXsiTypeValue("InsuranceInstitute");
        return getInsuranceSelect() + "/lal:roles/lal:role[@xsi:type='" + roleType + "']/lal:partyId/lal:key";
    }
       
    protected static String getSinglePaymentSubSelect() {
        return "/lal:singlePayments/lal:singlePayment";
    }

    protected static String getSinglePaymentSelect(){
        return getInsuranceSelect() + getSinglePaymentSubSelect();
    }
    
    public static String getFinancialBooking() {
        return getInsuranceSelect() + "/lal:financialBookings/lal:financialBooking";
    }
    
    private static String getAccount(String partyNr, String accountId) {
        return getPartySelect(partyNr) + "lal:accounts/lal:account[lal:id/lal:key = '"+ accountId + "']/";
    }

    private static String getPartyStreet(String partyNr) {
        return getPartyAddress(partyNr) + "lal:street/";
    }

    private static String getPartyAddress(String partyNr) {
        String partySelect = getPartySelect(partyNr);
        return partySelect + "lal:addresses/lal:address/";
    }

    private static String getPartySelect(String partyNr) {
        return getPoliciesSelect() + "lal:parties/lal:party[lal:id/lal:key='" + partyNr + "']/";
    }

    private static String getInsuranceSelect() {
        return getPoliciesSelect() + "lal:policies/lal:policy/lal:insurances/lal:insurance/";
    }
    
    private static String getPoliciesSelect() {
        return "/env:Envelope/env:Body/lal:getPoliciesBody/lal:policiesBody/";
    }
}
