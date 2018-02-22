# WS-Sec Signature

This directory contains a maven project and the Java source code  required to
compile a simple custom policy for Apigee Edge. The policy
signs a SOAP document using WSS4J.

Suppose you have a document like this:
```xml
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header/>
  <soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
```

You can sign the body so that it results in:

```xml
 <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/"
       xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
  <soap:Header>
    <wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" soap:mustUnderstand="1">
      <ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#" Id="SIG-1754a532-c1c5-42c1-b195-4a36b31ce9c9">
        <ds:SignedInfo>
          <ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#">
            <ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="soap xsd xsi"/>
          </ds:CanonicalizationMethod>
          <ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/>
          <ds:Reference URI="#id-3ddfa6e3-a96c-4b42-a1bb-8601ddfc36d0">
            <ds:Transforms>
              <ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#">
                <ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="xsd xsi"/>
              </ds:Transform>
            </ds:Transforms>
            <ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/>
            <ds:DigestValue>ezKnouSbVtsfuA+WA4abdOiBYOs=</ds:DigestValue>
          </ds:Reference>
        </ds:SignedInfo>
        <ds:SignatureValue>K4ZfcgVxthz0egyUbAkxqVFS04yWOcxSqOvkp+S3Wtcfe8wRMcGBx6gaaXMmz1IJXQIniPRwvRZF&#13;
301pQU9LsjHtHt2UBp1/qJt4HmguBnW54hYr2pUCcXf1DXeQcPS/05/iaOw99A0QHKGdqZRtgLGt&#13;
kJygLc3Wt14jy+DI10Cc3SaOwPgCWxdj3D+2E4ntnsMC+MgkuNXEzKp3gfdrZBy1I4gBoK4l1wGT&#13;
MkT5HJUL9sbgkGfBOd6fEs3in1YqgVDoWJLMgXv+l4WcEJQx1+EWWfWbB+cVueNVh83YyBWQKa+X&#13;
Df5F+vmR9pnA1oJkJJA6O2qVOtezyfZAuvrnuQ==</ds:SignatureValue>
        <ds:KeyInfo Id="KI-c3feb85a-8dab-4585-8f44-6812bf440b91">
          <wsse:SecurityTokenReference wsu:Id="STR-8a805657-add6-475a-aef7-b5eb2fb7fd15">
            <ds:X509Data>
              <ds:X509IssuerSerial>
                <ds:X509IssuerName>CN=SOADEV,OU=Dino,O=Chiesa,L=WVC,ST=WA,C=US</ds:X509IssuerName>
                <ds:X509SerialNumber>1453916936</ds:X509SerialNumber>
              </ds:X509IssuerSerial>
            </ds:X509Data>
          </wsse:SecurityTokenReference>
        </ds:KeyInfo>
      </ds:Signature>
    </wsse:Security>
  </soap:Header>
<soap:Body xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="id-3ddfa6e3-a96c-4b42-a1bb-8601ddfc36d0">
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
```

## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


## Using this Custom policy JAR

To use this JAR in Apigee Edge, you do not need to rebuild it. You need only to configure the Callout policy with the appropriate information.

## Simple Example Configuration: Signing

A simple configuration looks like this:

```xml
<JavaCallout name='Java-SignSoapDoc'>
  <Properties>
    <Property name='alias'>apigee</Property>
    <Property name='password'>Secret123</Property>
    <!-- the above really should be private variables retrieved from KVM:
      <Property name='alias'>{private.alias}</Property>
      <Property name='password'>{private.password}</Property>
    -->
  </Properties>
  <ClassName>com.google.apigee.callout.wssec.SOAPSigner</ClassName>
  <ResourceURL>java://edge-wssec-sign-x509-1.0.3.jar</ResourceURL>
</JavaCallout>
```

This uses the "compiled-in" .jks file, to sign a payload. This is probably not what you want.

A better configuration specifies the .JKS file to the callout.

```xml
<JavaCallout name='Java-SignSoapDoc-BYOJKS'>
  <Properties>
    <Property name='alias'>myalias</Property>
    <Property name='password'>mypassword</Property>
    <!-- this is the base64-encoded JKS file, generated with
         -->
    <Property name='jks-base64'>
      /u3+7QAAAAIAAAACAAAAAQAGc29hZGV2AAABUoQ0biUAAAUCMIIE/jAOBgorBgEE
      ASoCEQEBBQAEggTq3Nh2qAwD/cp3TB7dfy5GJRCSomjR0cWHPAeD/wHZDjiZYbBC
      XB2sByHfcogXKY5uIVfd7pD2DTCK3eFeYNnEQi0vhI2yGYbnwnX3GCOTpEv5ty1p
      xh1O+gQfm+GLCekeSOyjE2gt+TWC9Jt0VBHz87gn7dLyfTowEdPEo40/t9KJHgAd
      t6JhjomFwDjO5kTHvtwyWAl5taGRwgtXspGiuwgvD+9f9hKsqNGXijAocUjr3RFz
      BpyEHAtO8WBrFyRsHgu3hk1U++e4ncpMvxOyr2Jo93rSRyTp3VqmQRRHVSaoCRH8
      ....
    </Property>
  </Properties>
  <ClassName>com.google.apigee.callout.wssec.SOAPSigner</ClassName>
  <ResourceURL>java://edge-wssec-sign-x509-1.0.3.jar</ResourceURL>
</JavaCallout>
```

That octet stream is the result of base64-encoding a .jks file. To get that, you need to do 2 things.

1. generate a JKS
2. base64 encode it.

To generate, follow [Oracle's instructions using the keytool](https://docs.oracle.com/cd/E19509-01/820-3503/ggfen/index.html).

```
keytool -genkey -v -keystore my-keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias my-key-alias
```

On MacOS, you can do the encoding of the resulting .jks file like this:
```
 openssl base64 -in my-keystore.jks -out jks.bin
```

In this case, the sample .jks that is compiled into the JAR is not used to sign the payload. Instead the
.jks specified by the base64 string is used.


Best practice is to store those parameters in the encrypted
KVM, and reference them by variable, like this:

```xml
<JavaCallout name='Java-SignSoapDoc-BYOJKS'>
  <Properties>
    <Property name='alias'>{private.keyalias}</Property>
    <Property name='password'>{private.keypassword}</Property>
    <Property name='jks-base64'>{private.jks-base64}</Property>
  </Properties>
  <ClassName>com.google.apigee.callout.wssec.SOAPSigner</ClassName>
  <ResourceURL>java://edge-wssec-sign-x509-1.0.3.jar</ResourceURL>
</JavaCallout>
```

There is an additional property you can specify if appropriate: jks-password.


## Simple Example Configuration: Verifying

A simple configuration looks like this:

```xml
<JavaCallout name='Java-VerifySignature'>
  <Properties>
    <Property name='alias'>apigee</Property>
    <Property name='password'>Secret123</Property>
  </Properties>
  <ClassName>com.google.apigee.callout.wssec.SOAPVerifier</ClassName>
  <ResourceURL>java://edge-wssec-sign-x509-1.0.3.jar</ResourceURL>
</JavaCallout>
```

This uses the "compiled-in" .jks file, to sign a payload. This is probably not what you want. See the notes above.
The output of message.content will be stripped of the WS-Security header.


## Example API Proxy

You can find an example proxy bundle that uses the policy, [here in this repo](example-bundle/apiproxy).


## Building

Building from source requires Java 1.8, and Maven.

1. unpack (if you can read this, you've already done that).

2. Before building _the first time_, configure the build on your machine by loading the Apigee jars into your local cache:
  ```
  ./buildsetup.sh
  ```

3. Build with maven.
  ```
  mvn clean package
  ```
  This will build the jar and also run all the tests.




## License

This material is copyright 2018 Google Inc.  and is licensed under the
[Apache 2.0 License](LICENSE). This includes the Java code as well as
the API Proxy configuration.

## Bugs

* You must include your own .jks file in order to use this. I haven't yet figured out how to configure it to use a PEM-encoded certificate.
* The tests don't actually verify the signature.

