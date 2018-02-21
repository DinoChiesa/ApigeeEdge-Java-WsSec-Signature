# API Proxy bundle to demonstrate WS-Security signing

This Apigee Edge API Proxy demonstrates the use of the custom Java policy that performs WS-Sec Signing.
It can be used
on private cloud or public cloud instances of Edge.  It relies on [the custom Java policy](../callout) included here.


## Disclaimer

This example is not an official Google product, nor is it part of an official Google product.


## Example usage

In all the examples that follow, you should replace the APIHOST with something like
* ORGNAME-ENVNAME.apigee.net, if running in the Apigee-managed public cloud
* VHOST_IP:VHOST_PORT, if running in a self-managed cloud


### Insert a node into an existing XML

```
curl -i -H 'content-type: application/xml' \
 -X POST \
 'http://APIHOST/ws-sec-signing/t1' \
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


## Notes

Happy Signing!
