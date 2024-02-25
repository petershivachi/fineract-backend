package org.apache.fineract.useradministration.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * {
 *     "asset_id": "0d5c464bdaf36adfe4723eed722ac2e4",
 *     "public_id": "1708811055095.jpg",
 *     "version": 1708811177,
 *     "version_id": "ec0a33ff1dfebb4d7d18d5e7a56a6579",
 *     "signature": "a69b72a88558fb2ecdf07bcbbe01b9f4168a4ccd",
 *     "width": 4000,
 *     "height": 6000,
 *     "format": "jpg",
 *     "resource_type": "image",
 *     "created_at": "2024-02-24T21:46:17Z",
 *     "tags": [],
 *     "bytes": 4352472,
 *     "type": "upload",
 *     "etag": "85aae90459429a92874a735068b71406",
 *     "placeholder": false,
 *     "url": "http://res.cloudinary.com/dstey66qy/image/upload/v1708811177/1708811055095.jpg.jpg",
 *     "secure_url": "https://res.cloudinary.com/dstey66qy/image/upload/v1708811177/1708811055095.jpg.jpg",
 *     "folder": "",
 *     "original_filename": "Gidraph",
 *     "eager": [
 *         {
 *             "transformation": "w_400,h_300,c_pad",
 *             "width": 400,
 *             "height": 300,
 *             "bytes": 12151,
 *             "format": "jpg",
 *             "url": "http://res.cloudinary.com/dstey66qy/image/upload/w_400,h_300,c_pad/v1708811177/1708811055095.jpg.jpg",
 *             "secure_url": "https://res.cloudinary.com/dstey66qy/image/upload/w_400,h_300,c_pad/v1708811177/1708811055095.jpg.jpg"
 *         },
 *         {
 *             "transformation": "w_260,h_200,c_crop",
 *             "width": 260,
 *             "height": 200,
 *             "bytes": 4718,
 *             "format": "jpg",
 *             "url": "http://res.cloudinary.com/dstey66qy/image/upload/w_260,h_200,c_crop/v1708811177/1708811055095.jpg.jpg",
 *             "secure_url": "https://res.cloudinary.com/dstey66qy/image/upload/w_260,h_200,c_crop/v1708811177/1708811055095.jpg.jpg"
 *         }
 *     ],
 *     "api_key": "648537526944615"
 * }
 * */
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class CloudinaryResponse{

	@JsonProperty("eager")
	private List<EagerItem> eager;

	@JsonProperty("signature")
	private String signature;

	@JsonProperty("format")
	private String format;

	@JsonProperty("resource_type")
	private String resourceType;

	@JsonProperty("secure_url")
	private String secureUrl;

	@JsonProperty("created_at")
	private String createdAt;

	@JsonProperty("asset_id")
	private String assetId;

	@JsonProperty("version_id")
	private String versionId;

	@JsonProperty("type")
	private String type;

	@JsonProperty("version")
	private int version;

	@JsonProperty("url")
	private String url;

	@JsonProperty("public_id")
	private String publicId;

	@JsonProperty("tags")
	private List<Object> tags;

	@JsonProperty("folder")
	private String folder;

	@JsonProperty("original_filename")
	private String originalFilename;

	@JsonProperty("api_key")
	private String apiKey;

	@JsonProperty("bytes")
	private int bytes;

	@JsonProperty("width")
	private int width;

	@JsonProperty("etag")
	private String etag;

	@JsonProperty("placeholder")
	private boolean placeholder;

	@JsonProperty("height")
	private int height;

	@Data
	@JsonIgnoreProperties(ignoreUnknown = true)
    public static class EagerItem{

        @JsonProperty("bytes")
        private int bytes;

        @JsonProperty("width")
        private int width;

        @JsonProperty("format")
        private String format;

        @JsonProperty("secure_url")
        private String secureUrl;

        @JsonProperty("transformation")
        private String transformation;

        @JsonProperty("url")
        private String url;

        @JsonProperty("height")
        private int height;
    }
}