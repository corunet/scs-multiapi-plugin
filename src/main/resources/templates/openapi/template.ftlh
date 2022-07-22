package <#if packageApi??>${packageApi}<#elseif package??> ${package}</#if>;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.NativeWebRequest;
import javax.validation.Valid;
import java.util.Optional;
import java.util.List;
import java.util.Map;

<#assign imports=[]>
<#list pathObjects as path>
  <#list path.operationObjects as operation>
    <#list operation.requestObjects as request>
      <#list request.contentObjects as content>
        <#if content.importName?? && (!imports?seq_contains(content.importName))>
          <#if (!checkBasicTypes?seq_contains(content.importName))>
            <#assign imports = imports+[content.importName]>
          </#if>
        </#if>
      </#list>
    </#list>
    <#list operation.responseObjects as response>
      <#list response.contentObjects as content>
        <#if content.importName?? && (!imports?seq_contains(content.importName))>
          <#if (!checkBasicTypes?seq_contains(content.importName))>
            <#assign imports = imports+[content.importName]>
          </#if>
        </#if>
      </#list>
    </#list>
    <#list operation.parameterObjects as parameter>
        <#if parameter.importName?? && (!imports?seq_contains(parameter.importName))>
          <#if (!checkBasicTypes?seq_contains(parameter.importName))>
            <#assign imports = imports+[parameter.importName]>
          </#if>
        </#if>
    </#list>
  </#list>
</#list>
<#list imports as import>
import <#if packageModel??>${packageModel}<#elseif package??>${package}</#if>.${import};
</#list>

public interface ${className?cap_first}Api {

  default Optional<NativeWebRequest> getRequest() {
    return Optional.empty();
  }

<#list pathObjects as path>
<#list path.operationObjects as operation>
  /**
  * ${operation.operationType} ${path.pathName} <#if operation.summary?has_content>: ${operation.summary}</#if><#if operation.responseObjects?has_content>
  <#if operation.parameterObjects?has_content>
  *<#list operation.parameterObjects as parameter> @param ${parameter.name} ${parameter.description} ${parameter.required?c}</#list>
  </#if>
  <#if path.parameterObjects?has_content>
  *<#list path.parameterObjects as parameter> @param ${parameter.name} ${parameter.description} ${parameter.required?c}</#list>
  </#if>
  <#if operation.requestObjects?has_content>
  *<#list operation.requestObjects as request><#list request.contentObjects as content> @param ${content.refName?uncap_first}<#if content?has_next>, </#if></#list>${request.description! ""}<#if request.required == true> (required)</#if></#list>
  </#if>
  * @return<#list operation.responseObjects as response><#if response.responseName != "default">  ${response.description}; (status code ${response.responseName})</#if></#list></#if>
  */

  @Operation(
     operationId = "${operation.operationId}",<#if operation.summary?has_content>
     summary = "${operation.summary}",</#if>
     tags = { <#list operation.tags as tag>"${tag}"</#list> }<#if operation.responseObjects?has_content>,
     responses = {
         <#list operation.responseObjects as response> @ApiResponse(responseCode = "${response.responseName}", description = "${response.description}" <#if response.contentObjects?has_content>, content = @Content(<#list response.contentObjects as content>mediaType = "${content.name}" <#if content.refName?has_content>,schema = @Schema(implementation = <#if content.refName?contains("List")>List.class)<#else>${content.refName?cap_first?no_esc}.class)</#if>)</#if> </#list></#if>)<#if !response?is_last>,</#if></#list>
     }</#if>
  )
  @RequestMapping(
    method = RequestMethod.${operation.operationType},
    value = "${path.pathName}",
    produces = { "application/json" }
  )

  default ResponseEntity<@compress single_line=true><<#if operation.responseObjects[0].contentObjects[0]??>
    <#if operation.responseObjects[0].contentObjects[0].typeData?? && (operation.responseObjects[0].contentObjects[0].typeData == "map")>
    Map<String,${operation.responseObjects[0].contentObjects[0].refName?cap_first}>
    <#else>${operation.responseObjects[0].contentObjects[0].refName?cap_first?no_esc}</#if><#else>Void</#if>></@compress> ${operation.operationId}(<@compress single_line=true>
    <#if operation.parameterObjects?has_content><#list operation.parameterObjects as parameter> @Parameter(name = "${parameter.name}", description = "${parameter.description}", required = ${parameter.required?c}, schema = @Schema(description = "")) <#if parameter.in == "path"> @PathVariable("${parameter.name}") <#elseif parameter.in == "query"> @RequestParam </#if> ${parameter.className?cap_first} ${parameter.name} <#if parameter?has_next || operation.requestObjects?has_content> , </#if></#list></#if>
    <#if path.parameterObjects?has_content><#list path.parameterObjects as parameter> @Parameter(name = "${parameter.name}", description = "${parameter.description}", required = ${parameter.required?c}, schema = @Schema(description = "")) <#if parameter.in == "path"> @PathVariable("${parameter.name}") <#elseif parameter.in == "query"> @RequestParam </#if> ${parameter.className?cap_first} ${parameter.name} <#if parameter?has_next || operation.requestObjects?has_content> , </#if></#list></#if>
    <#if operation.requestObjects?has_content><#list operation.requestObjects as request> @Parameter(name = "${request.contentObjects[0].refName}", description = "${request.description! ""}", required = ${request.required?c} , schema = @Schema(description = "${request.contentObjects[0].description! ""}")) @Valid @RequestBody
    <#if request.contentObjects[0].typeData?? && (request.contentObjects[0].typeData == "array")> List<${request.contentObjects[0].refName?cap_first}>
    <#elseif request.contentObjects[0].typeData?? && (request.contentObjects[0].typeData == "map")> Map<String,${request.contentObjects[0].refName?cap_first}>
    <#else> ${request.contentObjects[0].refName?cap_first} </#if> ${request.contentObjects[0].refName?uncap_first} <#if request?has_next> , </#if></#list></#if></@compress>) {
    return new ResponseEntity<>(HttpStatus.NOT_IMPLEMENTED);
  }
  </#list>
</#list>

}
