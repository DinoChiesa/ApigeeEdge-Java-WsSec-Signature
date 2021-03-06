// Copyright 2017-2018 Google LLC
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

package com.google.apigee.edgecallouts.wssec;

import com.apigee.flow.execution.ExecutionResult;
import com.apigee.flow.execution.spi.Execution;
import com.apigee.flow.message.Message;
import com.apigee.flow.message.MessageContext;
import com.google.apigee.util.Signature;
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
    public ExecutionResult execute0(MessageContext msgCtxt) throws Exception {
        Message msg = msgCtxt.getMessage();
        String msgContent = msg.getContent();
        // Signature signer = new Signature();
        // Signature.SigningOptions options = new Signature.SigningOptions();
        // options.includeSignatureToken = getIncludeSignatureToken(msgCtxt);
        // options.alias = getSimpleRequiredProperty("alias", msgCtxt);
        // options.password = getSimpleRequiredProperty("password", msgCtxt);
        // String jksBase64 = getSimpleOptionalProperty("jks-base64", msgCtxt);
        // if (jksBase64 != null) {
        //     if (jksBase64.equals("")) {
        //         msgCtxt.setVariable(varName("error"), "empty jks-base64 property");
        //         return ExecutionResult.ABORT;
        //     }
        //     options.jksStream = new Base64InputStream(new ByteArrayInputStream(normalizeString(jksBase64).getBytes(StandardCharsets.UTF_8)));
        //     options.jksPassword = getSimpleOptionalProperty("jks-password", msgCtxt);
        // }
        // else {
        //     msgCtxt.setVariable(varName("error"), "empty jks-base64 property");
        //     return ExecutionResult.ABORT;
        // }
        // String signedMessage = signer.signMessage(msgContent, options);
        // String outputVar = getOutputVar(msgCtxt);
        // msgCtxt.setVariable(outputVar, signedMessage);

        // =======================================================================

        Signature signer = new Signature(msgCtxt);
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

        // throws ExceptionInInitializerError (uncatchable)
        String signedMessage = signer.signMessage(msgContent, options);
        String outputVar = getOutputVar(msgCtxt);
        msgCtxt.setVariable(outputVar, signedMessage);

        return ExecutionResult.SUCCESS;
    }
}
