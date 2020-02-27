package nl.hsac.fitnesse.fixture.slim;

public class WsSecurityXmlHttpTest extends XmlHttpTest {

    /**
     * Creates new.
     */
    public WsSecurityXmlHttpTest() {
        super();
    }

    /**
     * If true, apply a WS-SEC signature to the soap message before sending
     * You need to configure the keystore, certificate, key identifier, canonicalization method and digest algrithm as well
     * Optionally set single cert usage.
     *
     * @param sign true to sign the message
     */
    public void wsSecApplySoapSignatureBeforeSending(boolean sign) {
        signingHelper().applySignature(sign);
    }

    /**
     * If true, apply a WS-SEC timestamp
     * Optionally set TTL and if precision is in milliseconds
     *
     * @param applyTimestamp true to apply timestamp
     */
    public void wsSecApplyTimestampBeforeSending(boolean applyTimestamp) {
        signingHelper().applyTimestamp(applyTimestamp);
    }

    /**
     * If true, apply a WS-SEC username token to the SOAP message before sending
     * You need to set the token user and password. Optionally set to add nonce and/or created
     *
     * @param applyUsernameToken true to apply the token.
     */
    public void wsSecApplyUsernameTokenBeforeSending(boolean applyUsernameToken) {
        signingHelper().applyUsernameToken(applyUsernameToken);
    }

    /**
     * Configure the keystore to use for SOAP Signature
     *
     * @param keyStoreFile     The (wiki) path to the keystore file
     * @param keyStoreType     The keystore type. One of: {@value nl.hsac.fitnesse.fixture.util.SoapSigningHelper#VALID_KEY_STORE_TYPES}
     * @param keyStorePassword The keystore password
     */
    public void wsSecSetKeystoreOfTypeWithPassword(String keyStoreFile, String keyStoreType, String keyStorePassword) {
        signingHelper().setKeyStore(keyStoreFile, keyStoreType);
        signingHelper().setKeyStorePassword(keyStorePassword);
    }

    /**
     * The certificate alias to use for signing.
     *
     * @param alias    The certificate alias name
     * @param password The certificate password
     */
    public void wsSecUseKeyAliasWithPassword(String alias, String password) {
        signingHelper().setKeyAlias(alias);
        if (null != password && !password.equals("")) {
            signingHelper().setKeyPassword(password);
        }
    }

    /**
     * Set the identifier type. Use one of: {@value nl.hsac.fitnesse.fixture.util.SoapSigningHelper#VALID_KEY_IDENTIFIER_TYPES}
     *
     * @param keyIdentifierType The key identifier type to use
     */
    public void wsSecKeyIdentifierType(String keyIdentifierType) {
        signingHelper().setKeyIdentifierType(keyIdentifierType);
    }

    /**
     * Configure usage of single cert
     *
     * @param useSingleCert true for single
     */
    public void wsSecUseSingleCertificate(boolean useSingleCert) {
        signingHelper().useSingleCertificate(useSingleCert);
    }

    /**
     * Set the canonicalization method. Use one of: {@value nl.hsac.fitnesse.fixture.util.SoapSigningHelper#VALID_CANONICALIZATION_METHODS}
     *
     * @param canonicalization The canonicalization method to use
     */
    public void wsSecSetCanonicalizationMethod(String canonicalization) {
        signingHelper().setCanonicalizationMethod(canonicalization);
    }

    /**
     * Set the digest algorithm. Use one of: {@value nl.hsac.fitnesse.fixture.util.SoapSigningHelper#VALID_DIGEST_ALGORITHMS}
     *
     * @param algorithm The algorithm to use
     */
    public void wsSecSetDigestAlgorithm(String algorithm) {
        signingHelper().setDigestAlgorithm(algorithm);
    }

    /**
     * Configure the username token
     *
     * @param username The username to apply
     * @param password The password to apply
     */
    public void wsSecUsernameTokenUsernamePassword(String username, String password) {
        signingHelper().setTokenUsername(username);
        signingHelper().setTokenPassword(password);
    }

    /**
     * Add a nonce to the username token to hash the password
     *
     * @param addNonce true to add nonce
     */
    public void wsSecAddNonceToToken(boolean addNonce) {
        signingHelper().addNonceToToken(addNonce);
    }

    /**
     * Add a created node to the username token
     *
     * @param addCreated true to add a created node
     */
    public void wsSecAddCreatedToToken(boolean addCreated) {
        signingHelper().addCreatedToToken(addCreated);
    }

    /**
     * Configure optional WS-SEC timestamp settings
     *
     * @param ttl               The ttl
     * @param precisionInMillis true to set the precision to milliseconds
     */
    public void wsSecConfigureTimestampTtlPrecicionInMillis(int ttl, boolean precisionInMillis) {
        signingHelper().setTimestampTTL(ttl);
        signingHelper().setTimestampPrecisionToMillis(precisionInMillis);
    }

    /**
     * Sends HTTP method call template with current values to service endpoint.
     *
     * @param serviceUrl   service endpoint to send request to.
     * @param aContentType content type to use for post.
     * @param method       HTTP method to use
     * @return true if call could be made and response did not indicate error.
     */
    @Override
    public boolean sendTemplateTo(String serviceUrl, String aContentType, String method) {
        boolean result;
        resetResponse();
        if (template == null) {
            throw new StopTestException("No template available to use in " + method);
        } else {
            String url = getUrl(serviceUrl);
            try {
                String body = getEnvironment().processTemplate(template, getCurrentValues());
                storeLastCall(method, serviceUrl);
                sendToImpl(body, url, aContentType, method);
            } catch (Throwable t) {
                handleCallException("Unable to get response from " + method + " to: " + url, t);
            }
            result = postProcessResponse();
        }
        return result;
    }

    @Override
    protected boolean sendToImpl(String body, String serviceUrl, String aContentType, String method) {
        boolean result;
        resetResponse();
        body = getEnvironment().getSoapSigningHelper().signSoapMessageIfNeeded(body);
        response.setRequest(body);
        String url = getUrl(serviceUrl);
        try {
            storeLastCall(method, serviceUrl);
            switch (method) {
                case "POST":
                    getEnvironment().doHttpPost(url, response, headerValues, aContentType);
                    break;
                case "PUT":
                    getEnvironment().doHttpPut(url, response, headerValues, aContentType);
                    break;
                case "DELETE":
                    getEnvironment().doDelete(url, response, headerValues, aContentType);
                    break;
                case "PATCH":
                    getEnvironment().doHttpPatch(url, response, headerValues, aContentType);
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported method: " + method);
            }
        } catch (Throwable t) {
            handleCallException("Unable to get response from " + method + " to: " + url, t);
        }
        result = postProcessResponse();
        return result;
    }
}
