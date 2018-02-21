package com.google.apigee.callout.wssec;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SOAPSignerCallout implements Execution {
    private final static String varprefix= "cert_";
    private static String varName(String s) { return varprefix + s;}
    private final static String variableReferencePatternString = "(.*?)\\{([^\\{\\} ]+?)\\}(.*?)";
    private final static Pattern variableReferencePattern = Pattern.compile(variableReferencePatternString);
    private Map properties;
    public SOAPSignerCallout(Map properties) {
        this.properties = properties;
    }

    // private String getCert(MessageContext msgCtxt) throws Exception {
    //     String certificate = (String) this.properties.get("certificate");
    //     if (certificate == null || certificate.equals("")) {
    //          throw new IllegalStateException("certificate is not specified or is empty.");
    //     }
    //     certificate = (String) resolvePropertyValue(certificate, msgCtxt);
    //     if (certificate == null || certificate.equals("")) {
    //         throw new IllegalStateException("certificate is null or empty.");
    //     }
    //     return certificate;
    // }

    private boolean getDebug() {
        String wantDebug = (String) this.properties.get("debug");
        boolean debug = (wantDebug != null) && Boolean.parseBoolean(wantDebug);
        return debug;
    }

    private String getOutputVar(MessageContext msgCtxt) throws Exception {
        String dest = getSimpleOptionalProperty("output-variable", msgCtxt);
        if (dest == null) {
            return "message.content";
        }
        return dest;
    }

    private boolean getIncludeSignatureToken(MessageContext msgCtxt) throws Exception {
        String dest = getSimpleOptionalProperty("include-signature-token", msgCtxt);
        boolean inc = (dest != null) && Boolean.parseBoolean(dest);
        return inc;
    }

    private String getAlias(MessageContext msgCtxt) throws Exception {
        String alias = getSimpleRequiredProperty("alias", msgCtxt);
        return alias;
    }

    private String getPassword(MessageContext msgCtxt) throws Exception {
        String password = getSimpleRequiredProperty("password", msgCtxt);
        return password;
    }

    private String getSimpleRequiredProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        value = value.trim();
        if (value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) {
            throw new IllegalStateException(propName + " resolves to an empty string.");
        }
        return value;
    }

    private String getSimpleOptionalProperty(String propName, MessageContext msgCtxt) throws Exception {
        String value = (String) this.properties.get(propName);
        if (value == null) { return null; }
        value = value.trim();
        if (value.equals("")) { return null; }
        value = resolvePropertyValue(value, msgCtxt);
        if (value == null || value.equals("")) { return null; }
        return value;
    }

    // If the value of a property contains a pair of curlies,
    // eg, {apiproxy.name}, then "resolve" the value by de-referencing
    // the context variable whose name appears between the curlies.
    private String resolvePropertyValue(String spec, MessageContext msgCtxt) {
        Matcher matcher = variableReferencePattern.matcher(spec);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "");
            sb.append(matcher.group(1));
            sb.append((String) msgCtxt.getVariable(matcher.group(2)));
            sb.append(matcher.group(3));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    @Override
    public ExecutionResult execute(MessageContext msgCtxt, ExecutionContext execCtxt) {

        try {
            // String certString = getCert(msgCtxt);
            // InputStream instream = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
            // CertificateFactory cFactory = CertificateFactory.getInstance("X.509");
            // X509Certificate certificate = (X509Certificate) cFactory.generateCertificate(instream);

            Message msg = msgCtxt.getMessage();
            String msgContent = msg.getContent();
            Signer signer = new Signer();

            Signer.SigningOptions options = new Signer.SigningOptions();
            options.includeSignatureToken = getIncludeSignatureToken(msgCtxt);
            options.alias = getAlias(msgCtxt);
            options.password = getPassword(msgCtxt);

            String signedMessage = signer.signMessage(msgContent, options);
            String outputVar = getOutputVar(msgCtxt);
            msgCtxt.setVariable(outputVar, signedMessage);
        }
        catch (Exception e) {
            if (getDebug()) {
                System.out.println(ExceptionUtils.getStackTrace(e));
            }
            String error = e.toString();
            msgCtxt.setVariable(varName("exception"), error);
            int ch = error.lastIndexOf(':');
            if (ch >= 0) {
                msgCtxt.setVariable(varName("error"), error.substring(ch+2).trim());
            }
            else {
                msgCtxt.setVariable(varName("error"), error);
            }
            msgCtxt.setVariable(varName("stacktrace"), ExceptionUtils.getStackTrace(e));
            return ExecutionResult.ABORT;
        }

        return ExecutionResult.SUCCESS;
    }
}
