package com.sun.faban.driver.transport.htmlunit;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebResponse;
import com.gargoylesoftware.htmlunit.util.NameValuePair;
import com.sun.faban.driver.HttpTransport;
import static com.sun.faban.driver.HttpTransport.BUFFER_SIZE;
import com.sun.faban.driver.engine.CycleThread;
import com.sun.faban.driver.engine.DriverContext;
import com.sun.faban.driver.util.Timer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import org.apache.commons.httpclient.Cookie;

public class ExtendedJSTransport extends HttpTransport {

    private HttpTransport http;
    /**
     * The response code of the last response.
     */
    private int responseCodeExtend;
    /**
     * The response headers of the last response.
     */
    private Map<String, String> responseHeaderExtend;
    /**
     * The main appendable buffer for the total results.
     */
    private StringBuilder charBufferExtend;
    private HashSet<String> texttypes;
    /**
     * The content size of the last read page.
     */
    private int contentSizeExtend;
    /**
     * The byte buffer used for the reads in read* methods.
     */
    private byte[] byteReadBufferExtend = new byte[BUFFER_SIZE];
    /**
     * The char used for the reads in fetch* methods.
     */
    private char[] charReadBufferExtend = new char[BUFFER_SIZE];

    /**
     *
     * @author: limp
     */
    public ExtendedJSTransport(HttpTransport http) {
        this.http = http;
        texttypes = new HashSet<String>();
        texttypes.add("application/json");
    }

    public StringBuilder fetchURLWithJS(String url) throws IOException {
        return fetchURLWithJS(new URL(url));
    }

    public StringBuilder fetchURLWithJS(URL url)
            throws IOException {
        DriverContext ctx = DriverContext.getContext();
        //DriverContext ctx = new DriverContext(new CycleThread(), new Timer());
        WebClient webClient = new WebClient();
        webClient.getOptions().setCssEnabled(false);
        List<WebResponse> responses = webClient.getPageResponseList(url);

        for (WebResponse response : responses) {
            ctx.initResponseInfoList();
            ctx.recordResponseInfo(response.getStartTime(), response.getEndTime(),
                    response.getWebRequest().getUrl().toExternalForm());
        }

        WebResponse response = responses.get(0);
        responseCodeExtend = response.getStatusCode();

        buildResponseHeaders(response);
        return fetchResponse(response);
    }

    private void buildResponseHeaders(WebResponse response) {
        List<NameValuePair> heads = response.getResponseHeaders();
        responseHeaderExtend = new LinkedHashMap<String, String>();

        for (NameValuePair head : heads) {
            String name = head.getName();
            String value = head.getValue();
            responseHeaderExtend.put(name, value);
        }
    }

    private StringBuilder fetchResponse(WebResponse response) throws IOException {
        String contentType = response.getResponseHeaderValue("content-type");
        //
        // get encoding method
        String hdr = "charset=";
        int hdrLen = hdr.length();
        String encoding = "UTF-8";
        if (contentType != null) {
            StringTokenizer t = new StringTokenizer(contentType, ";");
            contentType = t.nextToken().trim();
            while (t.hasMoreTokens()) {
                String param = t.nextToken().trim();
                if (param.startsWith(hdr)) {
                    encoding = param.substring(hdrLen);
                    break;
                }
            }
        }

        if (contentType != null && (contentType.startsWith("text/")
                || texttypes.contains(contentType))) {
            InputStream is = response.getContentAsStream();
            if (is != null) {
                Reader reader;
                reader = new InputStreamReader(is, encoding);
                fetchResponseDataExtend(reader);
                reader.close();
            } else {
                reInitBuffer(2048); // Ensure we have an empty buffer.
            }
            return charBufferExtend;
        }
        readResponse(response);
        return null;
    }

    public StringBuilder fetchResponseDataExtend(Reader reader) throws IOException {
        int totalLength = 0;
        int length = reader.read(charReadBufferExtend, 0, charReadBufferExtend.length);
        if (length > 0) {
            reInitBuffer(length);
        } else {
            reInitBuffer(2048);
        }

        while (length != -1) {
            totalLength += length;
            charBufferExtend.append(charReadBufferExtend, 0, length);
            length = reader.read(charReadBufferExtend, 0, charReadBufferExtend.length);
        }
        contentSizeExtend = totalLength;
        return charBufferExtend;
    }

    /**
     * Initializes or re-initializes the buffer.
     *
     * @param size The size of the buffer
     */
    private void reInitBuffer(int size) {
        if (charBufferExtend == null) {
            charBufferExtend = new StringBuilder(size);
        } else {
            charBufferExtend.setLength(0);
        }
    }

    private int readResponse(WebResponse response) throws IOException {
        int totalLength = 0;
        InputStream in;

        in = response.getContentAsStream();
        if (in != null) {
            int length = in.read(byteReadBufferExtend);
            while (length != -1) {
                totalLength += length;
                length = in.read(byteReadBufferExtend);
            }
            in.close();
            contentSizeExtend = totalLength;
        }
        return totalLength;
    }

    public int getResponseCodeExtend() {
        return responseCodeExtend;
    }

    public int getContentSizeExtend() {
        return contentSizeExtend;
    }

    @Override
    public void setFollowRedirects(boolean follow) {
        http.setFollowRedirects(follow);
    }

    @Override
    public void addTextType(String texttype) {
        http.addTextType(texttype);
    }

    @Override
    public boolean isFollowRedirects() {
        return http.isFollowRedirects();
    }

    @Override
    public StringBuilder getResponseBuffer() {
        return http.getResponseBuffer();
    }

    @Override
    public int readURL(URL url, Map<String, String> headers) throws IOException {
        return http.readURL(url, headers);
    }

    @Override
    public int readURL(URL url) throws IOException {
        return http.readURL(url);
    }

    @Override
    public int readURL(String url, Map<String, String> headers) throws IOException {
        return http.readURL(url, headers);
    }

    @Override
    public int readURL(String url) throws IOException {
        return http.readURL(url);
    }

    @Override
    public int readURL(URL url, String postRequest) throws IOException {
        return http.readURL(url, postRequest);
    }

    @Override
    public int readURL(URL url, String postRequest, Map<String, String> headers) throws IOException {
        return http.readURL(url, headers);
    }

    @Override
    public int readURL(URL url, byte[] postRequest, Map<String, String> headers) throws IOException {
        return http.readURL(url, postRequest, headers);
    }

    @Override
    public int readURL(String url, byte[] postRequest) throws IOException {
        return http.readURL(url, postRequest);
    }

    @Override
    public int readURL(String url, String postRequest) throws IOException {
        return http.readURL(url, postRequest);
    }

    @Override
    public int readURL(String url, String postRequest, Map<String, String> headers) throws IOException {
        return http.readURL(url, postRequest, headers);
    }

    @Override
    public StringBuilder fetchURL(URL url, Map<String, String> headers) throws IOException {
        return http.fetchURL(url, headers);
    }

    @Override
    public StringBuilder fetchURL(URL url) throws IOException {
        return http.fetchURL(url);
    }

    @Override
    public StringBuilder fetchURL(String url, Map<String, String> headers) throws IOException {
        return http.fetchURL(url, headers);
    }

    @Override
    public StringBuilder fetchURL(String url) throws IOException {
        return http.fetchURL(url);
    }

    @Override
    public byte[] downloadURL(String url) throws IOException {
        return http.downloadURL(url);
    }

    @Override
    public StringBuilder fetchURL(String url, String postRequest) throws IOException {
        return http.fetchURL(url, postRequest);
    }

    @Override
    public StringBuilder fetchURL(String url, String postRequest, Map<String, String> headers) throws IOException {
        return http.fetchURL(url, postRequest, headers);
    }

    @Override
    public StringBuilder fetchURL(URL url, String postRequest, Map<String, String> headers) throws IOException {
        return http.fetchURL(url, postRequest, headers);
    }

    @Override
    public StringBuilder fetchURL(URL url, String postRequest) throws IOException {
        return http.fetchURL(url, postRequest);
    }

    @Override
    public StringBuilder fetchURL(URL page, URL[] images, String postRequest) throws IOException {
        return http.fetchURL(page, images, postRequest);
    }

    @Override
    public StringBuilder fetchPage(String page, String[] images, String postRequest) throws IOException {
        return http.fetchPage(page, images, postRequest);
    }

    @Override
    public int getContentSize() {
        return http.getContentSize();
    }

    @Override
    public StringBuilder fetchResponseData(InputStream stream) throws IOException {
        return http.fetchResponseData(stream);
    }

    @Override
    public StringBuilder fetchResponseData(Reader reader) throws IOException {
        return http.fetchResponseData(reader);
    }

    @Override
    public boolean matchResponse(String regex) {
        return http.matchResponse(regex);
    }

    @Override
    public boolean matchResponse(InputStream stream, String regex) throws IOException {
        return http.matchResponse(stream, regex);
    }

    @Override
    public boolean matchResponse(Reader reader, String regex) throws IOException {
        return http.matchResponse(reader, regex);
    }

    @Override
    public boolean matchURL(String url, String regex) throws IOException {
        return http.matchURL(url, regex);
    }

    @Override
    public boolean matchURL(String url, String regex, Map<String, String> headers) throws IOException {
        return http.matchURL(url, regex, headers);
    }

    @Override
    public boolean matchURL(URL url, String regex) throws IOException {
        return http.matchURL(url, regex);
    }

    @Override
    public boolean matchURL(URL url, String regex, Map<String, String> headers) throws IOException {
        return http.matchURL(url, regex, headers);
    }

    @Override
    public boolean matchURL(URL url, String postRequest, String regex) throws IOException {
        return http.matchURL(url, regex);
    }

    @Override
    public boolean matchURL(URL url, String postRequest, String regex, Map<String, String> headers) throws IOException {
        return http.matchURL(url, postRequest, regex, headers);
    }

    @Override
    public boolean matchURL(String url, String postRequest, String regex) throws IOException {
        return http.matchURL(url, postRequest, regex);
    }

    @Override
    public boolean matchURL(String url, String postRequest, String regex, Map<String, String> headers) throws IOException {
        return http.matchURL(url, postRequest, regex, headers);
    }

    @Override
    public String[] getCookieValuesByName(String name) {
        return http.getCookieValuesByName(name);
    }

    @Override
    public Cookie[] getCookies() {
        return http.getCookies();
    }

    @Override
    public String[] getResponseHeader(String name) {
        return http.getResponseHeader(name);
    }

    @Override
    public String dumpResponseHeaders() {
        return http.dumpResponseHeaders();
    }

    @Override
    public int getResponseCode() {
        return http.getResponseCode();
    }

    @Override
    public void setDownloadSpeed(int kbps) {
        http.setDownloadSpeed(kbps);
    }

    @Override
    public void setUploadSpeed(int kbps) {
        http.setUploadSpeed(kbps);
    }
}