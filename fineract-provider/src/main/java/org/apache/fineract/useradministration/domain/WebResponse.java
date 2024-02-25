package org.apache.fineract.useradministration.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.hc.core5.http.Header;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebResponse {
    Header[] responseHeaders;
    String reasonPhrase;
    int status;
    String responseString;

    public String getHeader(String headerTag){
        return Arrays.stream(responseHeaders)
                .filter(h -> h.getName().equals(headerTag))
                .findFirst()
                .map(Header::getValue)
                .orElse("NotFound");

    }

    @Override
    public String toString() {

        return "WebResponse{" +
                "responseHeaders=" + Arrays.toString(responseHeaders) +
                ", reasonPhrase=" + reasonPhrase +
                ", httpStatus="+status+
                ", responseString='" + responseString + '\'' +
                '}';
    }
}
