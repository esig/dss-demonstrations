The DSS-Web applications is built as demonstration for a Windows environment and all required elements (like the JRE 1.6) are provided.

To start any application go to the folder:
- ./bin

# For the web-application:
- Webapp-Startup.bat starts the Tomcat server
- Webapp-Shutdown.bat stops the Tomcat server

# For NexU:
- Nexu-Startup.bat starts NexU

The HTML page is available via http://localhost:8080/

The web-services (for e.g. integration in another application) are accessible via the following address http://localhost:8080/services/
  
There are three SOAP services available, that are described (WSDL) when accessing one of the aforementioned URLs:
1. Signature Service for one document : http://localhost:8080/services/soap/signature/one-document?wsdl
2. Signature Service for multiple documents : http://localhost:8080/services/soap/signature/multiple-documents?wsdl
3. Validation Service : http://localhost:8080/services/soap/ValidationService?wsdl

The same services are available as REST services :
1. Signature Service for one document : http://localhost:8080/services/rest/signature/one-document?_wadl
2. Signature Service for multiple documents : http://localhost:8080/services/rest/signature/multiple-documents?_wadl
3. Validation Service : http://localhost:8080/services/rest/validation?_wadl

Note that the web-application connects to external internet addresses to fetch data (e.g. CRL/OCSP).
If you have a proxy, then you may use the proxy configuration page:
- http://localhost:8080/admin/proxy
When the application is started, the european LoTL and its contained TSLs are fetched immediately.
It is quite "normal" that a TSL URL may not be accessible; this error is logged on the console.
