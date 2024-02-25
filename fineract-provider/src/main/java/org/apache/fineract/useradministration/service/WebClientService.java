package org.apache.fineract.useradministration.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.useradministration.domain.WebResponse;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.message.BasicHeader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.apache.fineract.useradministration.domain.Custom_Constants.*;


@Slf4j
@Service
@RequiredArgsConstructor
public class WebClientService {

    private final CloseableHttpClient httpClient;

    public  WebResponse performPostMultipart(InputStream file, String url,
                                             ConcurrentMap<String, String> headersMap,
                                             String filename, String signature, String apiKey, String eager) {
        HttpPost postRequest = new HttpPost(url);
        WebResponse webResponse = new WebResponse();
        List<Header> headers = new ArrayList<>();
        if (headersMap != null)
            headersMap.forEach((headerName, headerValue) -> headers.add(new BasicHeader(headerName, headerValue)));
        postRequest.setHeaders(headers.toArray(new Header[headers.size()]));
        try{
            final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.setMode(HttpMultipartMode.EXTENDED);
            builder.addBinaryBody(FILE_TAG, file, ContentType.DEFAULT_BINARY, filename);
            builder.addTextBody(PUBLIC_ID_TAG, filename, ContentType.DEFAULT_TEXT);
            builder.addTextBody(TIMESTAMP_TAG, filename, ContentType.DEFAULT_TEXT);
            builder.addTextBody(SIGNATURE_TAG, signature, ContentType.DEFAULT_TEXT);
            builder.addTextBody(API_KEY_TAG, apiKey, ContentType.DEFAULT_TEXT);
            builder.addTextBody(EAGER_TAG, eager, ContentType.DEFAULT_TEXT);
            final HttpEntity entity = builder.build();
            postRequest.setEntity(entity);
            log.info("URL : {} | signature : {} | timeStamp : {} | eager : {} | apiKey : {}",url,signature,filename,eager, apiKey);
            CloseableHttpResponse response = httpClient.execute(postRequest);
            webResponse = processResponse(response);

        }catch (Exception ex ){
            // TODO: 25/02/2024 handle this
        }

        return webResponse;
    }


    private WebResponse processResponse(CloseableHttpResponse response) throws IOException, ParseException {
        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");
        WebResponse webResponse = new WebResponse(response.getHeaders(), response.getReasonPhrase(), response.getCode(), responseString);
        log.info("Response from third party -> {}", webResponse);
        return webResponse;

    }


    private String generateRequestParams(Map<String, String> requestParams) {
        if (requestParams.isEmpty())
            return "";
        return "?" + requestParams.entrySet()
                .parallelStream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
    }
}
