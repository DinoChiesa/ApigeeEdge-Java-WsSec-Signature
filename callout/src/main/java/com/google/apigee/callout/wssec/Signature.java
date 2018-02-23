// Copyright 2017 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//

package com.google.apigee.callout.wssec;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import org.apache.wss4j.common.WSEncryptionPart;
import org.apache.wss4j.common.crypto.CertificateStore;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.token.Reference;
import org.apache.wss4j.common.token.SecurityTokenReference;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.SOAPConstants;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.HandlerAction;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.handler.WSHandlerResult;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.apache.wss4j.dom.validate.Credential;
import org.apache.wss4j.dom.validate.SignatureTrustValidator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Attr;
import org.w3c.dom.NodeList;

public class Signature {
    private WSSecurityEngine secEngine = new WSSecurityEngine();
    private static final String wsseNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
    private static final String wsuNS = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd";
    private static final String xmlNS = "xmlns";
    
    static {
        // Initialize BouncyCastle conditionally. Security.addProvider() is not permitted when run within Apigee Edge,
        // and is not necessary there, anyway, since Edge includes it. addProvider() is required when running outside
        // of Edge, as with running unit tests, or in a standalone client.
        if (java.security.Security.getProvider("BC") == null)
            java.security.Security.addProvider(new BouncyCastleProvider());
    }

    public Signature() throws Exception {
        WSSConfig.setAddJceProviders(false);
        org.apache.wss4j.stax.setup.WSSec.init();
        org.apache.xml.security.Init.init();
        WSSConfig.init();
    }

    public String signMessage(String content, SigningOptions options) throws Exception {
        Crypto thisCrypto = CryptoFactory.getInstance("crypto.properties");
        return signMessage0(content, options, thisCrypto);
    }

    private String signMessage0(String content, SigningOptions options, Crypto crypto) throws Exception {
        maybeModifyCrypto(crypto, options);

        Document doc = XmlUtil.toDocument(content);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
        WSSecHeader secHeader = new WSSecHeader(doc);
        secHeader.insertSecurityHeader();

        WSSecSignature builder = new WSSecSignature(secHeader);

        builder.setUserInfo(options.alias, options.password);
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        builder.setIncludeSignatureToken(options.includeSignatureToken);

        // other options could be parameterized here as well

        WSEncryptionPart encP = new WSEncryptionPart(soapConstants.getBodyQName().getLocalPart(),
                                                     soapConstants.getEnvelopeURI(),
                                                     "Content");
        builder.getParts().add(encP);
        Document signedDoc = builder.build(crypto);

        //WSHandlerResult result = secEngine.processSecurityHeader(signedDoc, null, null, crypto);

        //String outputString = XmlUtil.toPrettyString(signedDoc);
        String outputString = XmlUtil.toString(signedDoc); // cannot do pretty here, becaue of digsig and c18n
        return outputString;
    }

    private void maybeModifyCrypto(Crypto crypto, Options options) throws java.io.IOException, java.security.cert.CertificateException, java.security.NoSuchAlgorithmException {
        // source the .jks file from the designated stream
        if (options.jksStream != null && crypto instanceof Merlin) {
            Merlin merlin = (Merlin) crypto;
            KeyStore keystore = merlin.getKeyStore();
            keystore.load(options.jksStream, (options.jksPassword == null)? null: options.jksPassword.toCharArray());
        }
    }

    public String verifyAndStrip(String content, VerifyingOptions options) throws Exception {
        Crypto thisCrypto = CryptoFactory.getInstance("crypto.properties");
        Document doc = XmlUtil.toDocument(content);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
        WSHandlerResult result = verify0(doc, options, thisCrypto);
        verifyCertificateTrust(result, thisCrypto);

        // remove the wssec header, and the containing header if it is empty
        NodeList nodes = doc.getElementsByTagNameNS(soapConstants.getEnvelopeURI(), "Header");
        if (nodes.getLength() == 1) {
            Element header = (Element) nodes.item(0);
            nodes = header.getElementsByTagNameNS(wsseNS, "Security");
            if (nodes.getLength() == 1) {
                Element sec = (Element) nodes.item(0);
                sec.getParentNode().removeChild(sec);
                doc.normalize();

                NodeList children = header.getChildNodes();
                int count = children.getLength();
                int numElements = 0;
                for (int i = 0; i < count; i++) {
                    if (children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                        numElements++;
                    }
                }                
                if (numElements==0) {
                    header.getParentNode().removeChild(header);
                    doc.normalize();
                }
            }
        }
        
        // remove the Id from the body as necessary
        nodes = doc.getElementsByTagNameNS(soapConstants.getEnvelopeURI(), "Body");
        if (nodes.getLength() == 1) {
            Element body = (Element) nodes.item(0);
            NamedNodeMap attributes = body.getAttributes();
            if (attributes.getLength() > 0) {
                Attr attr = (Attr) attributes.getNamedItemNS(wsuNS, "Id");
                if (attr != null) {
                    body.removeAttributeNode(attr);
                    attributes = body.getAttributes();
                    attr = (Attr) attributes.getNamedItemNS(xmlNS, "wsu");
                    if (attr != null) {
                        body.removeAttributeNode(attr);
                    }
                    doc.normalize();
                }
            }
        }
        return XmlUtil.toPrettyString(doc);        // pretty is ok here, because no digsig
    }
    
    public boolean verify(String content, VerifyingOptions options) throws Exception {
        Crypto thisCrypto = CryptoFactory.getInstance("crypto.properties");
        Document doc = XmlUtil.toDocument(content);
        WSHandlerResult result = verify0(doc, options, thisCrypto);
        verifyCertificateTrust(result, thisCrypto);
        return true;
    }

    private WSHandlerResult verify0(Document doc, VerifyingOptions options, Crypto crypto) throws Exception {
        maybeModifyCrypto(crypto, options);
        return secEngine.processSecurityHeader(doc, null, null, crypto);
    }

    private static final Pattern CN_PATTERN = Pattern.compile("CN=([0-9]{9})([^0-9].*)?$");
    private static final Pattern SN_PATTERN = Pattern.compile("SERIALNUMBER=([0-9]{9})", Pattern.CASE_INSENSITIVE);
    
    protected void verifyCertificateTrust(WSHandlerResult result, Crypto validationSignatureCrypto) throws WSSecurityException {
        List<WSSecurityEngineResult> signResults = result.getActionResults().getOrDefault(WSConstants.SIGN, Collections.emptyList());
        if (signResults.isEmpty()) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_CHECK, "No action results for 'Perform Signature' found");
        } else if (signResults.size() > 1) {
            throw new WSSecurityException(WSSecurityException.ErrorCode.FAILED_CHECK, "Multiple action results for 'Perform Signature' found. Expected only 1.");
        }
        WSSecurityEngineResult signResult = signResults.get(0);

        if (signResult != null) {
            X509Certificate returnCert =
                (X509Certificate) signResult.get(WSSecurityEngineResult.TAG_X509_CERTIFICATE);
            Credential credential = new Credential();
            credential.setCertificates(new X509Certificate[]{returnCert});

            RequestData requestData = new RequestData();
            requestData.setSigVerCrypto(validationSignatureCrypto);
            //requestData.setEnableRevocation(enableRevocation);
            Collection<Pattern> PATTERNS = Arrays.asList(CN_PATTERN, SN_PATTERN, Pattern.compile(".*"));
            requestData.setSubjectCertConstraints(PATTERNS);
    
            SignatureTrustValidator validator = new SignatureTrustValidator();
            validator.validate(credential, requestData);
        }
    }


    public static class SigningOptions extends Options {
        public boolean includeSignatureToken;
    }

    public static class VerifyingOptions extends Options {
    }

    static class Options {
        public String alias;
        public String password;
        public InputStream jksStream;
        public String jksPassword;
    }
}
