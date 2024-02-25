/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.useradministration.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.apache.fineract.useradministration.service.CloudinaryService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@CrossOrigin
@Path("/v1/users")
@Component
@Tag(name = "Users", description = "An API capability to support administration of application users.")
@RequiredArgsConstructor
public class UsersApiResource {

    /**
     * The set of parameters that are supported in response for {@link AppUserData}.
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName", "username",
            "firstname", "lastname", "email", "allowedOffices", "availableRoles", "selectedRoles", "staff", "frontId", "photoOfIndividual", "backId"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "USER";

    private final PlatformSecurityContext context;
    private final AppUserReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<AppUserData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private  final CloudinaryService service;
    private final ObjectMapper mapper;

    @GET
    @Operation(summary = "Retrieve list of users", description = "Example Requests:\n" + "\n" + "users\n" + "\n" + "\n"
            + "users?fields=id,username,email,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = UsersApiResourceSwagger.GetUsersResponse.class))))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final Collection<AppUserData> users = this.readPlatformService.retrieveAllUsers();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, users, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{userId}")
    @Operation(summary = "Retrieve a User", description = "Example Requests:\n" + "\n" + "users/1\n" + "\n" + "\n"
            + "users/1?template=true\n" + "\n" + "\n" + "users/1?fields=username,officeName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.GetUsersUserIdResponse.class)))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String retrieveOne(@PathParam("userId") @Parameter(description = "userId") final Long userId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS, userId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AppUserData user = this.readPlatformService.retrieveUser(userId);
        if (settings.isTemplate()) {
            final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            user = AppUserData.template(user, offices);
        }

        return this.toApiJsonSerializer.serialize(settings, user, RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Operation(summary = "Retrieve User Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "users/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.GetUsersTemplateResponse.class)))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String template(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        final AppUserData user = this.readPlatformService.retrieveNewUserDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, user, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Operation(summary = "Create a User", description = "Adds new application user.\n" + "\n"
            + "Note: Password information is not required (or processed). Password details at present are auto-generated and then sent to the email account given (which is why it can take a few seconds to complete).\n"
            + "\n" + "Mandatory Fields: \n" + "username, firstname, lastname, email, officeId, roles, sendPasswordToEmail\n" + "\n"
            + "Optional Fields: \n" + "staffId,passwordNeverExpires,isSelfServiceUser,clients")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.PostUsersRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.PostUsersResponse.class)))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createUser() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("/withImage")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({MediaType.APPLICATION_JSON})
    public String createWithImage(@FormDataParam("photoOfIndividual") InputStream photoOfIndividual,
                                  @FormDataParam("frontId") InputStream frontId,
                                  @FormDataParam("backId") InputStream backId,
                                  @FormDataParam("apiRequestBodyAsJson") String apiRequestBodyAsJson
    ) throws JsonProcessingException {
        ConcurrentMap<String, InputStream> tobeUploaded = new ConcurrentHashMap<>();
        tobeUploaded.put("photoOfIndividual",photoOfIndividual);
        tobeUploaded.put("frontId",frontId);
        tobeUploaded.put("backId",backId);

        ConcurrentMap<String, String> urlUploadMap = service.handleBulk(tobeUploaded);
        log.info("upload done");

        LinkedHashMap<String, Object> curr = (LinkedHashMap<String, Object>) mapper.readValue(apiRequestBodyAsJson, Object.class);
        curr.put("photoOfIndividual",urlUploadMap.get("photoOfIndividual"));
        curr.put("frontId",urlUploadMap.get("frontId"));
        curr.put("backId",urlUploadMap.get("backId"));

        String enriched = mapper.writeValueAsString(curr);
        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createUser() //
                .withJson(enriched) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

//        return "Yes sir";
        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{userId}")
    @Operation(summary = "Update a User", description = "When updating a password you must provide the repeatPassword parameter also.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.PutUsersUserIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.PutUsersUserIdResponse.class)))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String update(@PathParam("userId") @Parameter(description = "userId") final Long userId,
                         @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateUser(userId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{userId}")
    @Operation(summary = "Delete a User", description = "Removes the user and the associated roles and permissions.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = UsersApiResourceSwagger.DeleteUsersUserIdResponse.class)))})
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public String delete(@PathParam("userId") @Parameter(description = "userId") final Long userId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteUser(userId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getUserTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
                                    @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.USERS.toString(), officeId, staffId, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload users template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class))})
    public String postUsersTemplate(@FormDataParam("file") InputStream uploadedInputStream,
                                    @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
                                    @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.USERS.toString(), uploadedInputStream,
                fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
