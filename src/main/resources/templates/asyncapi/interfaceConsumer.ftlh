package ${subscribePackage};

import ${method.classNamespace}<#if subscribeEntitiesSuffix?has_content>${subscribeEntitiesSuffix}</#if>;

public interface I${method.operationId?cap_first} {

    void ${method.operationId?uncap_first}(${method.className}<#if subscribeEntitiesSuffix?has_content>${subscribeEntitiesSuffix}</#if> value);
}