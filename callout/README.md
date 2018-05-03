# Callout for WS-Security signing

This Apigee Edge callout uses WSS4J (v2.1.x) to sign a SOAP message.


## This callout is Basic

There are numerous options for using WS-Security on SOAP messages. One can encrypt parts, sign parts, add timestamps, sign and encrypt, and so on.

This callout is very basic - it just signs the SOAP:Body with an x509 key, obtained from a .jks file.


## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


## Extending

It will be simple to add on other options. For example, right now there is the possibility to include the signature Token in the header. Do that with this configuration:

```xml
<JavaCallout name='Java-SignSoapDoc'>
  <Properties>
    <Property name='alias'>soadev</Property>
    <Property name='password'>devteam</Property>
    <Property name='include-signature-token'>true</Property>
  </Properties>
  <ClassName>com.google.apigee.callout.wssec.SOAPSignerCallout</ClassName>
  <ResourceURL>java://edge-wssec-sign-x509-1.0.4.jar</ResourceURL>
</JavaCallout>
```

To add other extensions and options, modify the [Signer.java](./src/main/java/com/google/apigee/callout/wssec/Signature.java) class, and possibly the inner class called SigningOptions, and then parameterize the callout class.


## Note

In general, you cannot "pretty print" the signed payload and get a successful verification.
Modifying the whitespace within the SignedInfo element in the header causes the signature verification to fail.

A full signed payload looks like this:
```
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:ns="http://example.com/ns">
   <soapenv:Header><wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" soapenv:mustUnderstand="1"><ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#" Id="SIG-fe82667b-76ab-4005-b2c9-ef9f4eec7330"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"><ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="ns soapenv"/></ds:CanonicalizationMethod><ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><ds:Reference URI="#id-72fc668f-210e-4176-a180-bcd156da8a48"><ds:Transforms><ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"><ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="ns"/></ds:Transform></ds:Transforms><ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><ds:DigestValue>RI7VKtgBiRj6+kForY0RHt06zhY=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>O7eDlcA4YrDvceG0GURbd7wh04BhwQL1LOCnmElttMsCdwwXw9TaRlreloNaRZSmcHKpzUd4cXBX
Ta/NCNacCY1VNU6z60syD+bE5wYyjAwSuG10duvtY/jl/B/h1No5a/PYC2M+WsaFXXB5uZeq+pIH
mz8TbwDmdMlM3f3uVfP3ZDMA9L7hXm4V6BtXvKr/FUeo3m/ZqXNwIAZKXWU53qmjcUATRdj1onu3
tRQ2YTCGLSuiDJdoXTl06KLaGDvBbiwNNeXJ8k2grElgbp5zporH0wP9//dk1BDMilRZXoRKBZGk
dZMgE+yW69Zy1Ae7MBiLhJWg3U7XHuqTmhg33A==</ds:SignatureValue><ds:KeyInfo Id="KI-94134f5b-b7c6-4dad-aefa-4df57fb8ade9"><wsse:SecurityTokenReference wsu:Id="STR-c3667fcd-af4d-48a0-92ce-be316b86040e"><ds:X509Data><ds:X509IssuerSerial><ds:X509IssuerName>CN=Werner Heisenberg,OU=Apigee,O=Google Cloud,L=Seattle,ST=WA,C=US</ds:X509IssuerName><ds:X509SerialNumber>839208195</ds:X509SerialNumber></ds:X509IssuerSerial></ds:X509Data></wsse:SecurityTokenReference></ds:KeyInfo></ds:Signature></wsse:Security></soapenv:Header>
   <soapenv:Body xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="id-72fc668f-210e-4176-a180-bcd156da8a48">
     <ns:request>
      <ns:customer>
       <ns:id>123</ns:id>
       <ns:name type="NCHZ">John Brown</ns:name>
      </ns:customer>
     </ns:request>
   </soapenv:Body>
</soapenv:Envelope>
```

Notice that the XML for the wsse:Security element is all on one line,
except for the linebreaks in the base64-encoded SignatureValue.

You may be tempted to pretty-print that thing, for demos or in your
app. This would add newlines and whitespace after each end-element and before each begin-element. This will make your signed XML look pretty, but the signature won't verify after that transformation.

[See here](https://lists.w3.org/Archives/Public/w3c-ietf-xmldsig/2002JanMar/0001.html) for some history.
The short story is, you cannot modify whitespace inside SignedInfo, without invalidating the signature.

The reason for this is, the SignedInfo is the thing that actually gets canonicalized and then signed. The digest within the SignedInfo element represents the Body, but it's just a hash. Injecting whitespace there will cause the signing base to change.

Inserting Whitespace before or after SignedInfo is ok.

This is ok, allows signature verification to succeed.
```
    <ds:SignedInfo><ds:CanonicalizationMethod Algorithm="...
```

This is not ok, sig verification fails:
```
    <ds:SignedInfo>
      <ds:CanonicalizationMethod Algorithm="...
```



