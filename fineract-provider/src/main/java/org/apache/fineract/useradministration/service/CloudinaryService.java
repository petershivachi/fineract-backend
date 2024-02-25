package org.apache.fineract.useradministration.service;


import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.fineract.useradministration.domain.CloudinaryResponse;
import org.apache.fineract.useradministration.domain.WebResponse;
import org.apache.fineract.useradministration.starter.ApplicationConfigs;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static org.apache.fineract.useradministration.domain.Custom_Constants.*;


@Slf4j
@RequiredArgsConstructor
@Service
public class CloudinaryService {

    private final ApplicationConfigs configs;
    private final WebClientService clientService;

    private final ObjectMapper objectMapper;

    private String generateSignature(String timestamp, String public_id, String eager, String secret) {
        Map<String, String> values = new HashMap<>();
        values.put(EAGER_TAG, eager);
        values.put(PUBLIC_ID_TAG, public_id);
        values.put(TIMESTAMP_TAG, timestamp);

        String hashRaw = values.entrySet()
                .parallelStream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));
        hashRaw = hashRaw + secret;

        String sha1Hex = DigestUtils.sha1Hex(hashRaw);
        log.info("key before hashing : {} | After Hashing | {}", hashRaw, sha1Hex);
        return sha1Hex;
    }

    public String uploadFile(InputStream fileToUpload) {
        try {
            String timeStamp = String.valueOf(new Timestamp(System.currentTimeMillis()).getTime());
            String signature = generateSignature(timeStamp, timeStamp, configs.getCloudinaryApiConfigs().eager, configs.getCloudinaryApiConfigs().secret);
            ConcurrentMap<String, String> headers = new ConcurrentHashMap<>();
            headers.put(HttpHeaders.CONNECTION, "keep-alive");
            WebResponse webResponse = clientService.performPostMultipart(fileToUpload,
                    configs.getCloudinaryApiConfigs().uploadUrl, headers, timeStamp, signature,
                    configs.getCloudinaryApiConfigs().apiKey, configs.getCloudinaryApiConfigs().eager);
            CloudinaryResponse cloudinaryResponse = objectMapper.readValue(webResponse.getResponseString(), CloudinaryResponse.class);
            return cloudinaryResponse.getUrl();
        } catch (Exception e) {
            String errorMessage = String.format("Exception Calling Cloudinary : %s", e.getLocalizedMessage());
            log.error(errorMessage);
            return errorMessage;
        }
    }

    public String deleteFile() {
        try {

            return "done";
        } catch (Exception ex) {

            return "done";
        }
    }


    public ConcurrentMap<String, String> handleBulk(ConcurrentMap<String, InputStream> toBeUploaded) {
        ConcurrentMap<String, String> response = new ConcurrentHashMap<>();
        for(Map.Entry<String,InputStream> curr : toBeUploaded.entrySet() ){
            String uploadUrl = uploadFile(curr.getValue());
            response.put(curr.getKey(), uploadUrl);
        }

        return response;
    }

}
