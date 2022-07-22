/*
 *  This Source Code Form is subject to the terms of the Mozilla Public
 *  * License, v. 2.0. If a copy of the MPL was not distributed with this
 *  * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package net.coru.api.generator.plugin.openapi.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MapSchema;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.parameters.RequestBody;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import net.coru.api.generator.plugin.openapi.exception.DuplicatedOperationException;
import net.coru.api.generator.plugin.openapi.model.AuthSchemaObject;
import net.coru.api.generator.plugin.openapi.model.BasicTypeConstants;
import net.coru.api.generator.plugin.openapi.model.ContentObject;
import net.coru.api.generator.plugin.openapi.model.GlobalObject;
import net.coru.api.generator.plugin.openapi.model.OperationObject;
import net.coru.api.generator.plugin.openapi.model.ParameterObject;
import net.coru.api.generator.plugin.openapi.model.PathObject;
import net.coru.api.generator.plugin.openapi.model.RequestObject;
import net.coru.api.generator.plugin.openapi.model.ResponseObject;
import net.coru.api.generator.plugin.openapi.parameter.FileSpec;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

public class MapperPathUtil {

  public static final String ARRAY = "array";

  public static final String MAP = "map";

  public static final String OBJECT = "object";

  public static final String INLINE_PARAMETER = "InlineParameter";

  private MapperPathUtil() {}

  public static GlobalObject mapOpenApiObjectToOurModels(final OpenAPI openAPI, final FileSpec fileSpec, final List<AuthSchemaObject> authSchemaList) {

    final var authList = getSecurityRequirementList(openAPI.getSecurity(), new ArrayList<>());

    return GlobalObject.builder()
                       .url(openAPI.getServers().get(0).getUrl())
                       .authSchemas(authSchemaList)
                       .authentications(authList)
                       .componentsTypeMap(getMapComponentsTypes(openAPI.getComponents(), fileSpec))
                       .build();
  }

  private static List<String> getSecurityRequirementList(final List<SecurityRequirement> securityRequirementList, final List<String> authentications) {
    final List<String> authSecList;
    if (null != securityRequirementList
        && !securityRequirementList.isEmpty()) {
      authSecList = new ArrayList<>();
      securityRequirementList.forEach(securityRequirement -> securityRequirement.forEach((key, value) -> authSecList.add(key)));
    } else {
      authSecList = authentications;
    }
    return authSecList;
  }

  private static HashMap<String, String> getMapComponentsTypes(final Components components, final FileSpec fileSpec) {
    final var mapComponents = new HashMap<String, String>();

    if (MapUtils.isNotEmpty(components.getSchemas())) {
      components.getSchemas().forEach((key, value) -> {
        if (!mapComponents.containsKey(key)) {
          final var type = checkSchemaType(value, fileSpec);
          mapComponents.put(key, OBJECT.equalsIgnoreCase(type) ? MapperUtil.getPojoName(key, fileSpec) : type);
        }
      });
    }
    return mapComponents;
  }

  private static String checkSchemaType(final Schema<?> schema, final FileSpec fileSpec) {
    var dataType = "";

    if (schema instanceof ArraySchema) {
      dataType = "array-" + MapperUtil.getTypeArray((ArraySchema) schema, fileSpec);
    } else if (schema instanceof MapSchema) {
      dataType = "map-" + MapperUtil.getTypeMap((MapSchema) schema, fileSpec);
    } else if (OBJECT.equals(schema.getType()) && StringUtils.isNotBlank(schema.get$ref())) {
      final String[] pathObjectRef = schema.get$ref().split("/");
      dataType = MapperUtil.getPojoName(pathObjectRef[pathObjectRef.length - 1], fileSpec);
    } else if (!Objects.nonNull(schema.getType()) && Boolean.TRUE.equals(checkSchemaCombinator(schema))) {
      dataType = OBJECT;
    } else {
      dataType = schema.getType();
    }

    return dataType;
  }

  public static Boolean checkSchemaCombinator(final Schema<?> schema) {
    return ObjectUtils.anyNotNull(schema.getAnyOf(), schema.getAllOf(), schema.getOneOf());
  }

  public static List<PathObject> mapPathObjects(
      final OpenAPI openAPI, final FileSpec fileSpec, final Entry<String, HashMap<String, PathItem>> path,
      final GlobalObject globalObject) {
    final List<PathObject> pathObjects = new ArrayList<>();
    for (Entry<String, PathItem> pathItem : path.getValue().entrySet()) {
      final PathObject pathObject = PathObject.builder()
                                              .pathName(pathItem.getKey())
                                              .globalObject(globalObject)
                                              .operationObjects(mapOperationObject(openAPI, fileSpec, pathItem, globalObject))
                                              .build();
      pathObjects.add(pathObject);
    }

    return pathObjects;
  }

  private static List<OperationObject> mapOperationObject(final OpenAPI openAPI, final FileSpec fileSpec, final Entry<String, PathItem> path, final GlobalObject globalObject) {
    final List<OperationObject> operationObjects = new ArrayList<>();
    final List<String> operationIdList = new ArrayList<>();
    if (Boolean.TRUE.equals(checkIfOperationIsNull(path.getValue().getGet()))) {
      operationObjects.add(createOperation(openAPI, path.getValue().getGet(), "GET", fileSpec, globalObject, operationIdList));
    }
    if (Boolean.TRUE.equals(checkIfOperationIsNull(path.getValue().getPost()))) {
      operationObjects.add(createOperation(openAPI, path.getValue().getPost(), "POST", fileSpec, globalObject, operationIdList));
    }
    if (Boolean.TRUE.equals(checkIfOperationIsNull(path.getValue().getDelete()))) {
      operationObjects.add(createOperation(openAPI, path.getValue().getDelete(), "DELETE", fileSpec, globalObject, operationIdList));
    }
    if (Boolean.TRUE.equals(checkIfOperationIsNull(path.getValue().getPut()))) {
      operationObjects.add(createOperation(openAPI, path.getValue().getPut(), "PUT", fileSpec, globalObject, operationIdList));
    }
    if (Boolean.TRUE.equals(checkIfOperationIsNull(path.getValue().getPatch()))) {
      operationObjects.add(createOperation(openAPI, path.getValue().getPatch(), "PATCH", fileSpec, globalObject, operationIdList));
    }

    return operationObjects;
  }

  private static OperationObject createOperation(
      final OpenAPI openAPI, final Operation operation, final String operationType,
      final FileSpec fileSpec, final GlobalObject globalObject, final List<String> operationIdList) {
    return OperationObject.builder()
                          .operationId(mapOperationId(operation.getOperationId(), operationIdList))
                          .operationType(operationType)
                          .summary(operation.getSummary())
                          .tags(operation.getTags())
                          .requestObjects(mapRequestObject(fileSpec, operation, globalObject))
                          .responseObjects(mapResponseObject(fileSpec, operation, globalObject))
                          .parameterObjects(mapParameterObjects(openAPI, operation.getParameters(), fileSpec, operation.getOperationId()))
                          .securities(getSecurityRequirementList(operation.getSecurity(), globalObject.getAuthentications()))
                          .consumes(getConsumesList(operation.getRequestBody()))
                          .produces(getProducesList(operation.getResponses()))
                          .build();
  }

  private static String mapOperationId(final String operationId, final List<String> operationIdList) {
    if (operationIdList.contains(operationId)) {
      throw new DuplicatedOperationException(operationId);
    } else {
      operationIdList.add(operationId);
      return operationId;
    }

  }

  private static List<String> getConsumesList(final RequestBody requestBody) {
    final var consumesList = new ArrayList<String>();
    if (requestBody != null && requestBody.getContent() != null
        && !requestBody.getContent().isEmpty()) {

      final Set<String> consumes = requestBody.getContent().keySet();
      consumes.forEach(key -> {
        if (!key.equalsIgnoreCase("*/*")) {
          consumesList.add(key.replace("\"", "\\\""));
        }
      });
    }

    return consumesList;
  }

  private static List<String> getProducesList(final ApiResponses responses) {
    final var producesList = new ArrayList<String>();

    if (Objects.nonNull(responses) && !responses.isEmpty()) {
      responses.forEach((key1, value) -> {
        if (value != null && value.getContent() != null && !value.getContent().isEmpty()) {
          final Set<String> produces = value.getContent().keySet();
          produces.forEach(key -> {
            if (!key.equalsIgnoreCase("*/*") && !producesList.contains(key)) {
              producesList.add(key.replace("\"", "\\\""));
            }
          });
        }
      });
    }
    return producesList;
  }

  private static List<RequestObject> mapRequestObject(final FileSpec fileSpec, final Operation operation, final GlobalObject globalObject) {
    final List<RequestObject> requestObjects = new ArrayList<>();
    final String operationIdWithCap = operation.getOperationId().substring(0, 1).toUpperCase() + operation.getOperationId().substring(1);
    if (Objects.nonNull(operation.getRequestBody())) {
      requestObjects.add(RequestObject.builder()
                                      .required(operation.getRequestBody().getRequired())
                                      .contentObjects(mapContentObject(fileSpec, operation.getRequestBody().getContent(), "InlineObject" + operationIdWithCap, globalObject))
                                      .build());
    }
    return requestObjects;
  }

  private static List<ParameterObject> mapParameterObjects(final OpenAPI openAPI, final List<Parameter> parameters, final FileSpec fileSpec, final String contentClassName) {
    final List<ParameterObject> parameterObjects = new ArrayList<>();
    if (Objects.nonNull(parameters) && !parameters.isEmpty()) {
      for (Parameter parameter : parameters) {
        if (Objects.nonNull(parameter.get$ref())) {
          final String[] wholeRef = parameter.get$ref().split("/");
          final String ref = wholeRef[wholeRef.length - 1];
          final Parameter refParameter = openAPI.getComponents().getParameters().get(ref);
          parameterObjects.add(ParameterObject.builder()
                                              .name(refParameter.getName())
                                              .required(refParameter.getRequired())
                                              .description(refParameter.getDescription())
                                              .in(refParameter.getDescription())
                                              .className(MapperUtil.getSimpleType(refParameter.getSchema(), fileSpec))
                                              .isCollection(ARRAY.equalsIgnoreCase(refParameter.getSchema().getType()))
                                              .build());
        } else if (Objects.nonNull(parameter.getContent())) {
          addInlineParametersToList(contentClassName, parameterObjects, parameter, fileSpec);
        } else {
          parameterObjects.add(ParameterObject.builder()
                                              .name(parameter.getName())
                                              .required(parameter.getRequired())
                                              .description(parameter.getDescription())
                                              .in(parameter.getIn())
                                              .className(MapperUtil.getSimpleType(parameter.getSchema(), fileSpec))
                                              .isCollection(ARRAY.equalsIgnoreCase(parameter.getSchema().getType()))
                                              .build());
        }
      }
    }
    return parameterObjects;
  }

  private static void addInlineParametersToList(final String contentClassName, final List<ParameterObject> parameterObjects, final Parameter parameter, final FileSpec fileSpec) {
    final Content content = parameter.getContent();
    for (Entry<String, MediaType> contentEntrySet : content.entrySet()) {
      if (OBJECT.equalsIgnoreCase(contentEntrySet.getValue().getSchema().getType())) {
        parameterObjects.add(ParameterObject.builder()
                                            .name(parameter.getName())
                                            .required(parameter.getRequired())
                                            .description(parameter.getDescription())
                                            .in(parameter.getIn())
                                            .className(getPojoName(INLINE_PARAMETER + StringUtils.capitalize(contentClassName)
                                                                   + StringUtils.capitalize(parameter.getName()), fileSpec))
                                            .importName(getPojoName(INLINE_PARAMETER + StringUtils.capitalize(contentClassName)
                                                                    + StringUtils.capitalize(parameter.getName()), fileSpec))
                                            .build());
      } else {
        parameterObjects.add(ParameterObject.builder()
                                            .name(parameter.getName())
                                            .required(parameter.getRequired())
                                            .description(parameter.getDescription())
                                            .in(parameter.getIn())
                                            .className(defineTypeName(contentEntrySet.getValue().getSchema()))
                                            .build());
      }
    }
  }

  private static List<ResponseObject> mapResponseObject(final FileSpec fileSpec, final Operation operation, final GlobalObject globalObject) {
    final List<ResponseObject> responseObjects = new ArrayList<>();
    final ApiResponses responses = operation.getResponses();
    if (Objects.nonNull(responses)) {
      responses.forEach((key, value) -> {
        final String operationIdWithCap = operation.getOperationId().substring(0, 1).toUpperCase() + operation.getOperationId().substring(1);
        responseObjects.add(ResponseObject.builder()
                                          .responseName(key)
                                          .description(value.getDescription())
                                          .contentObjects(mapContentObject(fileSpec, value.getContent(), "InlineResponse" + key + operationIdWithCap, globalObject))
                                          .build());
      });
    }
    return responseObjects;
  }

  private static List<ContentObject> mapContentObject(final FileSpec fileSpec, final Content content, final String inlineObject, final GlobalObject globalObject) {
    final List<ContentObject> contentObjects = new ArrayList<>();
    if (Objects.nonNull(content)) {
      for (Entry<String, MediaType> mediaTypeEntry : content.entrySet()) {
        if (Objects.nonNull(mediaTypeEntry.getValue().getSchema().getProperties())) {
          contentObjects.add(ContentObject.builder()
                                          .typeData(mapDataType(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap()))
                                          .name(mediaTypeEntry.getKey())
                                          .importName(StringUtils.capitalize(getPojoName(inlineObject, fileSpec)))
                                          .refName(getPojoName(inlineObject, fileSpec))
                                          .build());
        } else if (Objects.nonNull(mediaTypeEntry.getValue().getSchema().getType())
                   && BasicTypeConstants.BASIC_OBJECT_TYPE.contains(mediaTypeEntry.getValue().getSchema().getType())) {
          contentObjects.add(ContentObject.builder()
                                          .typeData(mapDataType(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap()))
                                          .name(mediaTypeEntry.getKey())
                                          .refName(defineTypeName(mediaTypeEntry.getValue().getSchema()))
                                          .build());
        } else if (ARRAY.equalsIgnoreCase(mediaTypeEntry.getValue().getSchema().getType()) && mediaTypeEntry.getValue().getSchema().getItems().get$ref() == null) {
          contentObjects.add(ContentObject.builder()
                                          .typeData(mapDataType(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap()))
                                          .name(mediaTypeEntry.getKey())
                                          .refName(defineTypeName(mediaTypeEntry.getValue().getSchema()))
                                          .build());
        } else {
          contentObjects.add(ContentObject.builder()
                                          .typeData(mapDataType(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap()))
                                          .name(mediaTypeEntry.getKey())
                                          .importName(StringUtils.capitalize(mapRefName(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap())))
                                          .refName(mapRefName(mediaTypeEntry.getValue().getSchema(), globalObject.getComponentsTypeMap()))
                                          .build());
        }
      }
    }
    return contentObjects;
  }

  private static String defineTypeName(final Schema<?> schema) {
    final String typeName;
    switch (schema.getType()) {
      case "integer":
        typeName = getIntegerFormat(schema);
        break;
      case "number":
        typeName = getNumberFormat(schema);
        break;
      case "boolean":
        typeName = "Boolean";
        break;
      case ARRAY:
        final ArraySchema arraySchema = (ArraySchema) schema;
        typeName = "List<" + defineTypeName(arraySchema.getItems()) + ">";
        break;
      case "string":
      default:
        typeName = "String";
        break;
    }
    return typeName;
  }

  private static String getIntegerFormat(final Schema<?> schema) {
    String typeName = "";
    if ("int32".equalsIgnoreCase(schema.getFormat()) || !Objects.nonNull(schema.getFormat())) {
      typeName = "Integer";
    } else if ("int64".equalsIgnoreCase(schema.getFormat())) {
      typeName = "Long";
    }
    return typeName;
  }

  private static String getNumberFormat(final Schema<?> schema) {
    String typeName = "";
    if ("float".equalsIgnoreCase(schema.getFormat())) {
      typeName = "Float";
    } else if ("double".equalsIgnoreCase(schema.getFormat())) {
      typeName = "Double";
    } else if (schema.getFormat().isEmpty()) {
      typeName = "Integer";
    }
    return typeName;
  }

  private static String mapDataType(final Schema<?> schema, final Map<String, String> componentsTypes) {
    final var type = getSchemaType(schema, componentsTypes);
    return StringUtils.isNotBlank(type) ? type : "";
  }

  private static String getSchemaType(final Schema<?> schema, final Map<String, String> componentsTypes) {
    String dataType = schema.getType();

    if (!StringUtils.isNotBlank(dataType) && Objects.nonNull(schema.get$ref())) {
      final String[] wholeRef = schema.get$ref().split("/");
      dataType = componentsTypes.getOrDefault(wholeRef[wholeRef.length - 1], "");
    }
    return dataType.startsWith(ARRAY) ? ARRAY : dataType.startsWith(MAP) ? MAP : dataType;
  }

  private static String mapRefName(final Schema<?> schema, final Map<String, String> componentsTypes) {

    var refSchema = "";

    if (ARRAY.equalsIgnoreCase(schema.getType())) {
      final ArraySchema arraySchema = (ArraySchema) schema;
      refSchema = arraySchema.getItems().get$ref();
    }
    if (Objects.nonNull(schema.get$ref())) {
      refSchema = schema.get$ref();
    }
    return StringUtils.isNotBlank(refSchema) ? getRefSchema(refSchema, componentsTypes) : "";
  }

  private static String getRefSchema(final String refSchema, final Map<String, String> componentsTypes) {
    final String[] wholeRef = refSchema.split("/");
    var refName = componentsTypes.getOrDefault(wholeRef[wholeRef.length - 1], "");

    if (StringUtils.isNotBlank(refName) && refName.contains("-")) {
      final String[] wholeRefName = refName.split("-");
      refName = wholeRefName[wholeRefName.length - 1];
    }
    return refName;
  }

  private static Boolean checkIfOperationIsNull(final Operation operation) {
    return Objects.nonNull(operation);
  }

  public static String getPojoName(final String namePojo, final FileSpec fileSpec) {
    return (StringUtils.isNotBlank(fileSpec.getModelNamePrefix()) ? fileSpec.getModelNamePrefix() : "")
           + namePojo
           + (StringUtils.isNotBlank(fileSpec.getModelNameSuffix()) ? fileSpec.getModelNameSuffix() : "");
  }

}
