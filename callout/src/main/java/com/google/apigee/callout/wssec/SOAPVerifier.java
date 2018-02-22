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
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SOAPVerifier extends WsSecCalloutBase implements Execution {
    public SOAPVerifier(Map properties) {
        super(properties);
    }

    @Override
    public ExecutionResult execute(MessageContext msgCtxt, ExecutionContext execCtxt) {

        try {
            Message msg = msgCtxt.getMessage();
            String msgContent = msg.getContent();
            Signature verifier = new Signature();

            Signature.VerifyingOptions options = new Signature.VerifyingOptions();
            options.alias = getSimpleRequiredProperty("alias", msgCtxt);
            options.password = getSimpleRequiredProperty("password", msgCtxt);
            String jksBase64 = getSimpleOptionalProperty("jks-base64", msgCtxt);
            if (jksBase64 != null) {
                options.jksStream = new Base64InputStream(new ByteArrayInputStream(normalizeString(jksBase64).getBytes(StandardCharsets.UTF_8)));
                options.jksPassword = getSimpleOptionalProperty("jks-password", msgCtxt);
            }
            String signedMessage = verifier.verifyAndStrip(msgContent, options);
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
