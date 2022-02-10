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

public class RhapsodyHttpClient
{
    private String CSRFToken;
    private String baseURL;
    private DefaultHttpClient httpclient;
    private String baseAuthUsername;
    private String baseAuthPassword;

    /**
     * Rhapsody HTTP Client Constructor
     * 
     * @param baseURL the base url for the Rhapsody API
     * @param baseAuthUsername the username for the Rhapsody API User
     * @param baseAuthPassword the password for the Rhapsody API User
     * @return RhapsodyHttpClient
     */
    public RhapsodyHttpClient(String baseURL, String baseAuthUsername, String baseAuthPassword)
    {
        this.baseURL = baseURL;
        this.baseAuthUsername = baseAuthUsername;
        this.baseAuthPassword = baseAuthPassword;
        this.httpclient = new DefaultHttpClient();

        httpclient.getCredentialsProvider().setCredentials(
            new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), 
            new UsernamePasswordCredentials(baseAuthUsername, baseAuthPassword)
        );
    }

    /**
     * Performs an HTTP Get with optional headers and query parameters
     * 
     * @param path the relative path after the baseURL to do an HTTP GET
     * @param headers any additional headers to append to the request
     * @param baseAuthPassword the password for the Rhapsody API User
     * @return HttpResponse
     */
    public HttpResponse HttpGet(String path, Map headers = null, Map queryParameters = null)
    {
        String url = baseURL + path;
        url = setQueryParameters(url, queryParameters);
        HttpGet request = new HttpGet(url);
        setHeaders(request, headers);
        HttpResponse response = executeRequest(request);

        return response;
    }

    public HttpResponse HttpPut(String path, String payload, Map headers = null, Map queryParameters = null)
    {
        String url = baseURL + path;
        url = setQueryParameters(url, queryParameters);
        HttpPut request = new HttpPut(url);
        StringEntity entity = new StringEntity(payload);
        request.setEntity(entity);
        setHeaders(request, headers);
        HttpResponse response = executeRequest(request);

        return response;
    }

    public HttpResponse HttpPost(String path, String payload, Map headers = null, Map queryParameters = null)
    {
        String url = baseURL + path;
        url = setQueryParameters(url, queryParameters);
        HttpPost request = new HttpPost(url);
        StringEntity entity = new StringEntity(payload);
        request.setEntity(entity);
        setHeaders(request, headers);
        HttpResponse response = executeRequest(request);

        return response;
    }

    public HttpResponse HttpDelete(String path, String payload = null, Map headers = null, Map queryParameters = null)
    {
        String url = baseURL + path;
        url = setQueryParameters(url, queryParameters);
        HttpPost request = new HttpDelete(url);
        if (payload != null)
        {
            StringEntity entity = new StringEntity(payload);
            request.setEntity(entity);
        }
        setHeaders(request, headers);
        HttpResponse response = executeRequest(request);

        return response;
    }

    public String GetResponseBodyAsString(HttpResponse response, String encoding = "UTF-8")
    {
        HttpEntity responseEntity = response.getEntity();
        
        if (responseEntity != null)
        {
            return EntityUtils.toString(responseEntity, encoding);
        }

        return null;
    }

    /**
     * Sets the headers for the request. Automatically includes the CSRF Token header with the request.
     * 
     * @param request the request object to append to
     * @param headers any additional headers to append to the request     
     */
    private void setHeaders(HttpMessage request, Map headers)
    {
        if (this.CSRFToken != null)
        {
            request.addHeader("X-CSRF-Token", this.CSRFToken)
        }
        if (headers != null)
        {
            headers.each{ key, value ->
                request.addHeader(key, value)
            }
        }
    }

    private String setQueryParameters(String url, Map queryParameters)
    {
        if (queryParameters != null)
        {
            url += "?";
            String params = "";
            queryParameters.each{ key, value ->
                params += (URLEncoder.encode(key, StandardCharsets.UTF_8.toString()) + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8.toString()));
            }
            url += params;
        }

        return url;
    }

    private HttpResponse executeRequest(HttpMessage request)
    {
        HttpResponse response = this.httpclient.execute(request);
        this.CSRFToken = response.getFirstHeader("X-CSRF-Token").getValue();

        return response;
    }
}
