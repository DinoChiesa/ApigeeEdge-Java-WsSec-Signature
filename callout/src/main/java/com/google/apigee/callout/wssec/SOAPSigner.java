package com.google.apigee.callout.wssec;

import com.apigee.flow.execution.ExecutionContext;
import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.lang3.exception.ExceptionUtils;

public class SOAPSigner extends WsSecCalloutBase implements Execution {
    public SOAPSigner(Map properties) {
        super(properties);
    }

    private boolean getIncludeSignatureToken(MessageContext msgCtxt) throws Exception {
        String dest = getSimpleOptionalProperty("include-signature-token", msgCtxt);
        boolean inc = (dest != null) && Boolean.parseBoolean(dest);
        return inc;
    }

    @Override
    public ExecutionResult execute(MessageContext msgCtxt, ExecutionContext execCtxt) {

        try {
            Message msg = msgCtxt.getMessage();
            String msgContent = msg.getContent();
            Signature signer = new Signature();

            Signature.SigningOptions options = new Signature.SigningOptions();
            options.includeSignatureToken = getIncludeSignatureToken(msgCtxt);
            options.alias = getSimpleRequiredProperty("alias", msgCtxt);
            options.password = getSimpleRequiredProperty("password", msgCtxt);
            String jksBase64 = getSimpleOptionalProperty("jks-base64", msgCtxt);
            if (jksBase64 != null) {
                if (jksBase64.equals("")) {
                    msgCtxt.setVariable(varName("error"), "empty jks-base64 property");
                    return ExecutionResult.ABORT;
                }
                options.jksStream = new Base64InputStream(new ByteArrayInputStream(normalizeString(jksBase64).getBytes(StandardCharsets.UTF_8)));
                options.jksPassword = getSimpleOptionalProperty("jks-password", msgCtxt);
            }
            else {
                msgCtxt.setVariable(varName("error"), "empty jks-base64 property");
                return ExecutionResult.ABORT;
            }
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
