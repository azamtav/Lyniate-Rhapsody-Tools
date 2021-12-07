import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.HttpMessage;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.http.entity.StringEntity;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Calendar;
import groovy.util.XmlParser;
import groovy.json.JsonSlurper;
import org.apache.log4j.Logger;

public class RhapsodyAPIClient
{
    private RhapsodyHttpClient httpClient;
    private Logger log;

    public RhapsodyAPIClient(String baseURL, String baseAuthUsername, String baseAuthPassword)
    {
        this.httpClient = new RhapsodyHttpClient(baseURL, baseAuthUsername, baseAuthPassword);
        this.log = log;
    }
    
    public String GetLookupTableGuid(String lookupTableName)
    {
        String guid;
        def response = httpClient.HttpGet("/admin/lookuptables", ["Accept" : "application/xml"], ["includeValues" : "false"]);
        def statusCode = response.getStatusLine().getStatusCode();
        if (statusCode == 200)
        {
            def responseString = httpClient.GetResponseBodyAsString(response);
            def lookupTablesXML = new XmlParser().parseText(responseString);
            def lookupTable = lookupTablesXML.lookupTable.find { node ->
                node.name.text() == lookupTableName;
            }
            if (lookupTable != null)
            {
                guid = lookupTable.'@guid'
            }
        }

        return guid;
    }

    public String GetLookupTableName(String lookupTableGuid)
    {
        def url = "/admin/lookuptables/" + lookupTableGuid;
        def response = httpClient.HttpGet(url, ["Accept" : "application/xml"], ["includeValues" : "false"])
        def statusCode = response.getStatusLine().getStatusCode();
        String tableName;
        if (statusCode == 200)
        {
            def responseString = httpClient.GetResponseBodyAsString(response);
            def lookupTableXML = new XmlParser().parseText(responseString);
            tableName = lookupTableXML.name.text()
        }

        return tableName;
    }

    public String GetLookupTableValues(String lookupTableGuid)
    {
        def url = "/admin/lookuptables/" + lookupTableGuid + "/values";        
        def response = httpClient.HttpGet(url, ["Accept" : "text/csv"]);
        def statusCode = response.getStatusLine().getStatusCode();
        def responseString = httpClient.GetResponseBodyAsString(response);
        if (statusCode == 200)
        {
            return responseString;
        }

        return null;
    }

    public boolean AppendToLookupTable(String lookupTableGuid, ArrayList tableRows, String commitComment = null)
    {
        
        def body = GetLookupTableValues(lookupTableGuid);
        if (body != null && tableRows.size() > 0)
        {
            def payload = body;
            for (int i = 0; i < tableRows.size(); i++)
            {
                payload += "\n" + tableRows[i];
            }
          
            return OverwriteLookupTable(lookupTableGuid, payload, commitComment);
        }

        return false;
    }
    
    public boolean OverwriteLookupTable(String lookupTableGuid, String newTable, String commitComment = null)
    {
        if (commitComment == null)
        {
            def tableName = GetLookupTableName(lookupTableGuid);
            commitComment = "Updating values of Lookup Table: " + tableName;
        }

        if (newTable != null)
        {
            def url = "/admin/lookuptables/" + lookupTableGuid + "/values";
            def response = httpClient.HttpPut(url, newTable, ["Content-Type" : "text/csv"], ["commitComment" : commitComment])
            def statusCode = response.getStatusLine().getStatusCode()
            if (statusCode == 204)
            {
                return true;
            }
        }

        return false; 
    }

    /**
    * Get a CommPoint Id By Path
    *
    * @param path The full path to the locker, folder, communication point, filter or route with lockers and 
    * folders separated using a forward slash, for example locker/folder/route1. The path is case sensitive.
    *
    * @return int The CommPoint Id
    *
    **/
    public int GetCommPointByPath(String path)
    {
        def url = "/api/components/find";
        def payload = path;        
        def response = httpClient.HttpPost(url, payload, ["Accept" : "text/plain", "Content-Type" : "text/plain"])
        def statusCode = response.getStatusLine().getStatusCode()
        if (statusCode == 200)
        {
            String responseString = httpClient.GetResponseBodyAsString(response);
            int commpointId = Integer.parseInt(responseString)
            return commpointId;
        }

        return null;
    }

    public String GetAllComponents()
    {
        def response = httpClient.HttpGet("/api/components", ["Accept" : "application/json"])
        def statusCode = response.getStatusLine().getStatusCode()
        if (statusCode == 200)
        {
            return httpClient.GetResponseBodyAsString(response)
        }

        return null
    }

    /**
    * Get a Component by name
    *
    * @param componentType The type of component. Values can be: COMMUNICATION_POINT or ROUTE or FILTER
    * @param componentName The name of the component
    *
    * @return int the component id, or -1 if not found
    *
    **/
    public int GetComponentByName(String componentType, String componentName)
    {
        def allComponentsString = GetAllComponents()
        if (allComponentsString != null)
        {
            def slurper = new JsonSlurper()
            def allComponentsJson = slurper.parseText(allComponentsString)
            def mainFolders = allComponentsJson.data.childFolders[0]
            
            return RecursiveSearch(mainFolders, componentType, componentName)            
        }

        return -1;
    }

    public int RecursiveSearch(Object folder, String componentType, String componentName)
    {
        if (folder.childFolders != null && folder.childFolders.size() > 0)
        {
              def found = -1;
              for (int i = 0; i < folder.childFolders.size(); i++)
              {
                  found = RecursiveSearch(folder.childFolders[i], componentType, componentName)
                  if(found != -1)
                  {
                      return found;
                  }
              }            
        }
        if (folder.childComponents != null && folder.childComponents.size() > 0)        
        {
            def components = folder.childComponents;
            for (int i = 0; i < components.size(); i++)
            {
                if (components[i].type == componentType && components[i].name == componentName)
                {
                    return components[i].id;
                }
            }
        }
        
        return -1;
    }

    public boolean UpdateCommPointState(int commpointId, String state)
    {
        def url = "/api/commpoint/" + commpointId + "/state"        
        def response = httpClient.HttpPut(url, state, ["Content-Type" : "text/plain"])
        def statusCode = response.getStatusLine().getStatusCode()
        def responseBody = httpClient.GetResponseBodyAsString(response)
        if (statusCode == 204)
        {
            return true;
        }

        return false;
    }

    public boolean StopCommPoint(int commpointId)
    {
        return UpdateCommPointState(commpointId, "STOP")
    }

    public boolean StartCommPoint(int commpointId)
    {
        return UpdateCommPointState(commpointId, "START");
    }

    public boolean RestartCommPoint(int commpointId)
    {
        return UpdateCommPointState(commpointId, "RESTART");
    }    

    public boolean SearchLookupTable(String lookupTableGuid, String searchString)
    {
        def tableBody = GetLookupTableValues(lookupTableGuid);
        if (tableBody != null)
        {
            def tableRows = tableBody.split('\n');
            for (int i = 0; i < tableRows.size(); i++)
            {
                if (tableRows[i].contains(searchString))
                {
                    return true;
                }
            }
        }

        return false;
    }

    public String wrapInQuotes(String input)
    {
        return '"' + input + '"';
    }
}