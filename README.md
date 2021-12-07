# Lyniate-Rhapsody-Tools
A set of tools to work with the Lyniate Rhapsody system (formerly Orion Health)

# Files
- RhapsodyHttpClient.groovy: This file has the core HTTP methods to interact with the API. It manages the CSRF token for requests as well and packaging up the query parameters on requests and getting the payload as a plain string.
- RhapsodyAPIClient.groovy: This file interacts with the Rhapsody API

# Requirements
These tools were written in Groovy Script to work with:
 - Rhapsody API 6.6
 - SOAP UI 5.6.0

They require the following libraries. The version of SOAP UI may already include these packages and can be used right out of the box. However, you may need to include these libraries to use these tools:
  -  [Apache HttpClient 4.5.13](https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.5.13)
  -  [Apache Log4j 1.2.14](https://mvnrepository.com/artifact/log4j/log4j/1.2.14)

# Groovy Compilation into Java:
`groovyc -cp "httpcomponents-client-4.5.13\lib\*;log4j-1.2.14.jar" -d "MyDestinationFolder" RhapsodyAPIClient.groovy`

# JAR Compilation of Java
`jar cvf Rhapsody.jar -C classes .`
