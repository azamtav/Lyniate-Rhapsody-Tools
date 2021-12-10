# Lyniate-Rhapsody-Tools
A set of tools to work with the Lyniate Rhapsody system (formerly Orion Health)

# Files
- RhapsodyHttpClient.groovy: This file has the core HTTP methods to interact with the API. It manages the CSRF token for requests as well and packaging up the query parameters on requests and getting the payload as a plain string.
- RhapsodyAPIClient.groovy: This file interacts with the Rhapsody API

# Requirements
These tools were written in Groovy Script to work with:
 - Rhapsody API 6.6
 - SOAP UI 5.6.0

# Groovy Compilation into Java:
`groovyc -cp "httpcomponents-client-4.5.13\lib\*;log4j-1.2.14.jar" -d "MyDestinationFolder" RhapsodyAPIClient.groovy`

# JAR Compilation of Java
`jar cvf Rhapsody.jar -C classes .`
