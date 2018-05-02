# API Proxy bundle to demonstrate WS-Security signing

This Apigee Edge API Proxy demonstrates the use of the custom Java
policy that performs WS-Sec Signing.

It can be used on private cloud or public cloud instances of Edge.  It
relies on [the custom Java policy](../callout) included here.


## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


## Example usage

In all the examples that follow, you should replace the APIHOST with something like
* ORGNAME-ENVNAME.apigee.net, if running in the Apigee-managed public cloud
* VHOST_IP:VHOST_PORT, if running in a self-managed cloud


### Sign an existing SOAP request

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 "https://$APIHOST/ws-sec-signing/sign2" \
 -d ' <soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header/>
  <soap:Body>
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
'
```

The result will be a signed SOAP document.

### Verify the result of the above

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 "https://$APIHOST/ws-sec-signing/verify2" \
 -d '
<soap:Envelope xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
  <soap:Header><wsse:Security xmlns:wsse="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd" xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" soap:mustUnderstand="1"><ds:Signature xmlns:ds="http://www.w3.org/2000/09/xmldsig#" Id="SIG-51adfb9c-71e1-497c-9dd8-91ae2811b15a"><ds:SignedInfo><ds:CanonicalizationMethod Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"><ec:InclusiveNamespaces xmlns:ec="http://www.w3.org/2001/10/xml-exc-c14n#" PrefixList="soap"/></ds:CanonicalizationMethod><ds:SignatureMethod Algorithm="http://www.w3.org/2000/09/xmldsig#rsa-sha1"/><ds:Reference URI="#id-ed817564-96bb-45ff-a819-0a3edb9cf8b4"><ds:Transforms><ds:Transform Algorithm="http://www.w3.org/2001/10/xml-exc-c14n#"/></ds:Transforms><ds:DigestMethod Algorithm="http://www.w3.org/2000/09/xmldsig#sha1"/><ds:DigestValue>84G55Fd8UcYUcmzIlQ7JnPElUPM=</ds:DigestValue></ds:Reference></ds:SignedInfo><ds:SignatureValue>Vhs5WUE4uC/JpSGUO64s2N++CrTknm3AbfgDlz6KNRZOG2rYtHSUU4t2mz/H3HWdepCeZeAn71JH
uLirGSO0CwYsy8hVgC2ZP0dNG1crz/eBa0al4G++4LcKy+gzHY4uExTSjRJlHFVvqMzbBf6A5HWW
FOE1D46zllj41iZZmLSvo4zjAldVa2WYShRPLYXFOvDvv56r5VQ2J5kM+txeUrbLj0uyyfpjszwb
bJ9/dISoJVlUSBPzyyUEZCci4nx6rcTuqEO5qw5p3W82agGgWnuz2z+SHKwXF4WqE1abwIziMpJ/
pPVkzhITqTHMMzssPlrd6xHy3gqWnq17DdcScA==</ds:SignatureValue><ds:KeyInfo Id="KI-5e322180-f375-4c55-9752-8a26fb42f2e2"><wsse:SecurityTokenReference wsu:Id="STR-bea9fc71-08d7-46f1-9233-cb253c0dc911"><ds:X509Data><ds:X509IssuerSerial><ds:X509IssuerName>CN=Werner Heisenberg,OU=Apigee,O=Google Cloud,L=Seattle,ST=WA,C=US</ds:X509IssuerName><ds:X509SerialNumber>839208195</ds:X509SerialNumber></ds:X509IssuerSerial></ds:X509Data></wsse:SecurityTokenReference></ds:KeyInfo></ds:Signature></wsse:Security></soap:Header>
  <soap:Body xmlns:wsu="http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd" wsu:Id="id-ed817564-96bb-45ff-a819-0a3edb9cf8b4">
    <act:test xmlns:act="http://yyyy.com">
      <abc>
        <act:demo>fokyCS2jrkE5s+bC25L1Aax5sK....08GXIpwlq3QBJuG7a4Xgm4Vk</act:demo>
      </abc>
    </act:test>
  </soap:Body>
</soap:Envelope>
'
```


## Notes

* If you modify the signed payload in any way before passing it to the callout for verification, the verification will fail. For example if you "pretty print" the WS-Security header, inserting whitespace between elements or attributes, the subsequent verification will fail.  So don't do that. Sometimes tools like Postman can silently pretty print things for you. Be aware.

Happy Signing!
