package com.google.apigee.callout.wssec;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.text.SimpleDateFormat;

import org.apache.wss4j.dom.SOAPConstants;
import org.apache.wss4j.common.WSEncryptionPart;
import org.apache.wss4j.common.bsp.BSPRule;
import org.apache.wss4j.common.crypto.Crypto;
import org.apache.wss4j.common.crypto.CryptoFactory;
import org.apache.wss4j.common.crypto.CertificateStore;
import org.apache.wss4j.common.ext.WSSecurityException;
import org.apache.wss4j.common.token.Reference;
import org.apache.wss4j.common.token.SecurityTokenReference;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.dom.message.WSSecHeader;
import org.apache.wss4j.dom.message.WSSecSignature;

import org.apache.wss4j.dom.engine.WSSConfig;
import org.apache.wss4j.dom.engine.WSSecurityEngine;
import org.apache.wss4j.dom.engine.WSSecurityEngineResult;
import org.apache.wss4j.dom.handler.HandlerAction;
import org.apache.wss4j.dom.handler.RequestData;
import org.apache.wss4j.dom.util.WSSecurityUtil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
    
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class Signer {
    static {
        // Do this conditionally. It is not permitted when run within Apigee Edge,
        // and is not necessary there, anyway. It is required when running outside
        // of Edge, as with running unit tests, or in a standalone client.
        if (java.security.Security.getProvider("BC") == null)
            java.security.Security.addProvider(new BouncyCastleProvider());
    }

    public Signer() throws Exception {
        WSSConfig.setAddJceProviders(false);
        WSSConfig.init();
    }
    
    public String signMessage(String content, SigningOptions options) throws Exception {
        Crypto thisCrypto = CryptoFactory.getInstance("crypto.properties");
        return signMessage0(content, options, thisCrypto);
    }
    
    private String signMessage0(String content, SigningOptions options, Crypto crypto) throws Exception {
        Document doc = XmlUtil.toDocument(content);
        SOAPConstants soapConstants = WSSecurityUtil.getSOAPConstants(doc.getDocumentElement());
        WSSecHeader secHeader = new WSSecHeader(doc);
        secHeader.insertSecurityHeader();
        
        WSSecSignature builder = new WSSecSignature(secHeader);
        builder.setUserInfo("soadev", "devteam");
        builder.setKeyIdentifierType(WSConstants.ISSUER_SERIAL);
        builder.setIncludeSignatureToken(options.includeSignatureToken);
        
        // other options could be parameterized here as well
        
        WSEncryptionPart encP = new WSEncryptionPart(soapConstants.getBodyQName().getLocalPart(),
                                                     soapConstants.getEnvelopeURI(),        
                                                     "Content");
        builder.getParts().add(encP);
        Document signedDoc = builder.build(crypto);
        String outputString = XmlUtil.toPrettyString(signedDoc);
        return outputString;
    }

        
    static class SigningOptions {
        public boolean includeSignatureToken;
        public String alias;
        public String password;
    }
}
