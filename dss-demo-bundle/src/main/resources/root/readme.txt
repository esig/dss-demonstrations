The DSS-Web applications is built as demonstration for a Windows environment and all required elements (like the JRE 1.8) are provided.

# For the web-application:
- Webapp-Startup.bat starts the Tomcat server
- Webapp-Shutdown.bat stops the Tomcat server

The HTML page is available via http://localhost:8080/

The web-services (for e.g. integration in another application) are accessible via the following address http://localhost:8080/services/
  
There are four SOAP services available, that are described (WSDL) when accessing one of the aforementioned URLs:
1. Signature Service for one document : http://localhost:8080/services/soap/signature/one-document?wsdl
2. Signature Service for multiple documents : http://localhost:8080/services/soap/signature/multiple-documents?wsdl
3. Validation Service : http://localhost:8080/services/soap/ValidationService?wsdl
4. Server signing (with HSM/Keystore) : http://localhost:8080/services/soap/server-signing?wsdl

The same services are available as REST services :
1. Signature Service for one document : http://localhost:8080/services/rest/signature/one-document?_wadl
2. Signature Service for multiple documents : http://localhost:8080/services/rest/signature/multiple-documents?_wadl
3. Validation Service : http://localhost:8080/services/rest/validation?_wadl
4. Server signing (with HSM/Keystore) : http://localhost:8080/services/rest/server-signing?_wadl

Note that the web-application connects to external internet addresses to fetch data (e.g. CRL/OCSP).

When the application is started, the European LOTL and its contained TLs are fetched immediately.
It is quite "normal" that a TL URL may not be accessible; this error is logged on the console.
