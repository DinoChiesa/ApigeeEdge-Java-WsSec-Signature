# Callout for WS-Security signing

This Apigee Edge callout uses WSS4J (v2.1.x) to sign a SOAP message.

## This callout is basic

There are numerous options for using WS-Security on SOAP messages. One can encrypt parts, sign parts, add timestamps, sign and encrypt, and so on.

This callout is very basic - it just signs the SOAP:Body with an x509 key, obtained from a .jks file.


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
  <ResourceURL>java://edge-wssec-sign-x509-1.0.1.jar</ResourceURL>
</JavaCallout>
```

To add other extensions and options, modify the [Signer.java](./src/main/java/com/google/apigee/callout/wssec/Signer.java) class, and possibly the inner class called SigningOptions.



## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


