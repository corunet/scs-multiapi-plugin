[![Codacy Badge](https://app.codacy.com/project/badge/Grade/9a486e91e2b245d8abe2e523c95bdf9a)](https://www.codacy.com/gh/corunet/scs-multiapi-plugin/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=corunet/scs-multiapi-plugin&amp;utm_campaign=Badge_Grade)
[![Maven Central](https://img.shields.io/maven-central/v/net.coru/scs-multiapi-maven-plugin.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22net.coru%22%20AND%20a:%22scs-multiapi-maven-plugin%22)
# SCS MultiApi Maven Plugin

This is a Maven plugin designed to help developers automatizing the creation of
code classes from YML files based on AsyncApi and OpenAPI.

## Index

- [SCS MultiApi Maven Plugin](#scs-multiapi-maven-plugin)
- [Index](#index)
- [Main Configuration](#main-configuration)
  - [How to configure the POM file](#how-to-configure-the-pom-file)
- [AsyncApi Generator](#asyncapi-generator)
  - [Configuration](#configuration)
    - [Generated Sources Folder](#generated-sources-folder)
  - [How apiPackage is setted?](#how-apiPackage-is-setted)
  - [How modelPackage is setted?](#how-modelpackage-is-setted)
  - [Class Generation](#class-generation)
    - [Consumer and Supplier classes](#consumer-and-supplier-classes)
      - [Method interfaces](#method-interfaces)
      - [Mapper](#mapper)
        - [Implementation](#implementation)
    - [Stream Bridge class](#stream-bridge-class)
- [OpenApi Generator](#openapi-generator)
  - [Getting Started](#getting-started)
  - [Initial Considerations](#initial-considerations)
  - [Usage](#usage)

## Main Configuration

This plugin allows developers to automatize the creation of code classes for
REST and Kafka connections, based on YML files under the AsyncApi and OpenApi
specifications. In the latter case, many of the configuration options and classes
that are generated are based on reimplementation or modification of the OpenAPI Generator
models and template designs.

The generation of the REST and Kafka connections is independent each other and
could be used only one, or both at the same time.

Here is the documentation for these technologies:

- [OpenApi](https://swagger.io/specification/)
- [AsyncApi](https://www.asyncapi.com/docs/getting-started)
- [OpenAPI Generator](https://openapi-generator.tech/docs/configuration)

### How to configure the POM file

To mantain the generation of the diferent types of classes independent, they
are configured as two different goals on the plugin, `asyncapi-generation` and
`openapi-generation`.
As commented above, they both could be used at the same time, setting a double
*execution* for the plugin in the `pom.xml` file.

```xml
<plugin>
  <groupId>net.coru</groupId>
  <artifactId>scs-multiapi-maven-plugin</artifactId>
  <version>3.0.1</version>
  <executions>
    <execution>
      <id>asyncapi</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>asyncapi-generation</goal>
      </goals>
      <configuration>
        <fileSpecs>
          ...
        </fileSpecs>
      </configuration>
    </execution>

    <execution>
      <id>openapi</id>
      <phase>generate-sources</phase>
      <goals>
        <goal>openapi-generation</goal>
      </goals>
      <configuration>
        <fileSpecs>
          <fileSpec>
            ...
          </fileSpec>
        </fileSpecs>
      </configuration>
    </execution>
  </executions>
</plugin>
```

In the example above, you can see a partial configuration for the plugin with
a double *execution*. This makes neccesary to set an `id` for each execution,
`asyncapi` and `openapi` in this case.

In the case that you only want to run one of the goals of the plugin, you only
need to remove the *execution* section that you don't need.

In the [AsyncApi Generator](#asyncapi-generator) and the
[OpenApi Generator](#openapi-generator) sections, you can find more information
about how they work, and the parameters and configuration options they offer.

## AsyncApi Generator

### Configuration

The plugin defined `phase` and `goal` parameters are expected to be
*generate-sources* and *asyncapi-generation*, as they are the only values for
which the plugin is designed.

```xml
<plugin>
<groupId>net.coru</groupId>
<artifactId>scs-multiapi-maven-plugin</artifactId>
<version>3.0.1</version>
<executions>
  <execution>
    <phase>generate-sources</phase>
    <goals>
      <goal>asyncapi-generation</goal>
    </goals>
    <configuration>
      <fileSpecs>
        <fileSpec>
          <filePath>PATH_TO_YML</filePath>
        </fileSpec>
        <fileSpec>
          <filePath>PATH_TO_YML</filePath>
          <consumer>
            <ids>publishOperation</ids>
            <classNamePostfix>MY_CONSUMER_CLASS</classNamePostfix>
            <modelNameSuffix>DTO</modelNameSuffix>
            <apiPackage>net.coru.apigenerator.asyncapi.business_model.model.event.consumer</apiPackage>
            <modelPackage>net.coru.apigenerator.asyncapi.business_model.model.event</modelPackage>
          </consumer>
          <supplier>
            <ids>subscribeOperation</ids>
            <apiPackage>net.coru.apigenerator.asyncapi.business_model.model.event.producer</apiPackage>
            <modelPackage>net.coru.apigenerator.asyncapi.business_model.model.event</modelPackage>
          </supplier>
        </fileSpec>
      </fileSpecs>
      <generatedSourcesFolder>sources-generated</generatedSourcesFolder>
    </configuration>
  </execution>
</executions>
</plugin>
```

As you can see in the example above, there is a main parameter **fileSpecs**
that receives a list of **fileSpec** attributes groups, so you can set as many
YML files as you want.

**fileSpecs** could be configured in two different ways:

1. The first one is to configure only the YML file. This is made using the
**filePath** parameter, that expects to receive the path to the file. Using
the plugin in this way, you can't configure the model package or the api
package in the pom file, neither other options, so they will be configured as
its explained in [apiPackage](#how-apiPackage-is-setted) and
[modelPackage](#how-modelPackage-is-setted) sections.  
This way it's limited to the usage of Consumer and Supplier methods.

  ```xml
  <fileSpec>
      <filePath>PATH_TO_YML</filePath>
  </fileSpec>
  ```

2. The second one is to configure the YML file with the consumers, supplier
producers and streamBrige producers that you want to generate.

``````xml
<fileSpec>
    <filePath>PATH_TO_YML</filePath>
    <consumer>
        <ids>publishOperation</ids>
        <classNamePostfix>MY_CONSUMER_CLASS</classNamePostfix>
        <modelNameSuffix>DTO</modelNameSuffix>
        <apiPackage>net.coru.apigenerator.asyncapi.business_model.model.event.consumer</apiPackage>
        <modelPackage>net.coru.apigenerator.asyncapi.business_model.model.event</modelPackage>
    </consumer>
    <supplier>
        <ids>subscribeOperation</ids>
        <apiPackage>net.coru.apigenerator.asyncapi.business_model.model.event.producer</apiPackage>
        <modelPackage>net.coru.apigenerator.asyncapi.business_model.model.event</modelPackage>
    </supplier>
    <streamBridge>
        <ids>streamBridgeOperation</ids>
        <apiPackage>net.coru.apigenerator.asyncapi.business_model.model.event.producer</apiPackage>
        <modelPackage>net.coru.apigenerator.asyncapi.business_model.model.event</modelPackage>
    </streamBridge>
</fileSpec>
``````

As you can see in the example above, there are three blocks of parameters that
can be configured in the plugin.

- **filePath**: This parameter works in the same way as in the first option.
- **consumer**, **supplier** and **streamBridge**: They are both configured in
the same way and can receive the same parameters. These parameters are:
  - **ids**: With this parameter you can set the operationId that you want to
  be generated as subscriber or publisher. If this parameter is not defined for
  the `consumer` section, all the subscribe operations defined in the YML file,
  will be generated. If only one of `supplier` and `streamBridge` sections are
  defined, and this parameter is not defined inside it, all the publish
  operations defined in the YML file will be generated. If both `supplier` and
  `streamBridge` sections are defined, it`s needed to define which operations
  belongs to each category.
  - **classNamePostfix**: This parameter receives the name of the class that
  it's going to be generated containing the Beans. This parameter is optional,
  and by default the classes will be called `Producer`, `StreamBridgeProducer`
  and `Subscriber`.
  - **modelNameSuffix**: With this parameter you can set the suffix that is
  going to be used in the entities of the generated classes. For example if
  you set this to `DTO`, and there is a class named `EntityClass`, it will
  result as `EntityClassDTO`. This parameter is optional.
  - **apiPackage**: This parameter receive a package name, where the
  generated classes will be generated. This parameter is optional.
  Check [how the apiPackage is setted](#how-apiPackage-is-setted) for
  more information about how this parameter works, and the values it
  could have.
  - **modelPackage**: This parameter receive a package name, where the entities
  used for the generated classes are defined. As it's explained in the
  [Mapper Section](#mapper), those entities are usually auto-generated, so the
  plugin expects the modelPackage to be the package where them are included.
    **Note that the plugin doesn't create the entities neither checks their
  existence**, it takes their names from the YML file and assume that they are
  created by the user. As the previous parameter, this is also optional.
  Check [how the modelPackage is setted](#how-modelPackage-is-setted) for more
  information about how his parameter works, and the values it could have.

The configuration of `consumer`, `supplier` and `streamBridge` are independent.
If only one of them is configured in the pom file, only that one will be
generated.

#### Generated Sources Folder

There is also an independent parameter that affects to all the *fileSpecs*
generated, which is called **generatedSourcesFolder**. This parameter expects
to receive a string, that could include letters, numbers and `-`, with the
name of the folder where generated sources by the plugin will be located.

By default, it's values is `generated-sources`, so the files will be in
`.../target/generated-sources/apigenerator/...`. If you set another value in
the pom.xml file, as in the example above, files will remain in
`.../target/sources-generated/apigenerator/...`.

### How apiPackage is setted?

The api package could be set in three different ways.

- **User definition**: The user provides a package name using the parameter in
the pom.xml file.
- **GroupID from YML**: If the user doesn't provide a package name, the plugin
will try to use the `groupId` attribute from the YML file that is in use.
- **Default package name**: If neither of the previous options were given, the
plugin will use a default package name, that is stablished as
`net.coru.apigenerator.asyncapi`.

### How modelPackage is setted?

The model package could be set in four different ways.

- **User definition**: The user provides a package name using the parameter in
the pom.xml file.
- **Namespace from YML**: If the user doesn't provide a package name, the
plugin will check if the entity name definition in the YML file, includes a
complete package name.

```yaml
order/createCommand:
    subscribe:
      operationId: "subscribeOperation"
      message:
        $ref: '#/components/messages/net.coru.apigenerator.asyncapi.model.CreateOrder'
```

- **Namespace from Avro**: If the user doesn't provide a package name, and the
entity is defined by an Avro Schema, the plugin will check for a `namespace`
attribute defined in the Avro file, and if there is, it will use it. The plugin
expects to receive a relative path from the `yml` file folder.

```yaml
order/created:
    publish:
      operationId: "publishOperation"
      message:
        $ref: 'path_to_Avro_file'
```

- **Default package name**: If neither of the previous options were given, the
plugin will use a default package name, that is stablished as
`net.coru.apigenerator.asyncapi.model`.

### Class Generation

#### Consumer and Supplier classes

Those are a pair of classes, separated by the directionality of the messages.
They came from the plugin fully implemented by making reference to the
interfaces of the next section. Their names could be modified using the
`classNamePostfix` parameter specified on the
[Usage section](#using-in-other-projects), being by default **Producer** and
**Subscriber**.

```java
@Configuration
public class StreamTopicListenerConsumer {

    private final ISubscribeOperation subscribeOperation;

    protected StreamTopicListenerConsumer(final ISubscribeOperation subscribeOperation){
      this.subscribeOperation = subscribeOperation;
    }

    @Bean
    public Consumer<CreateOrder> consumerSubscribeOperation(){ 
      return value -> subscribeOperation.subscribeOperation(value); }
}
```

This sample class, is related to the previosly used YML file, and in it you
could see that it came fully implemented, based on the related Interface that
lets the personalitation and implementation to the user. Also, in this example
is possible to see how the YML attribute 'operationId' is used to name the
methods as `Consumer'OperationId'` or `Publisher'OperationId'`.

##### Method interfaces

Those are a group of interfaces that are related to the previous seen classes.
There are as many as operations are defined in the YML file, and in the
previous classes, so there is only one operation defined in each interface.

This layer is the only one that needs work by the end user, so it needs to
implement these interfaces.

This interfaces are named following the "I*OperationId*" pattern, where
'OperationId' comes from the YML file definition of the channels section.
Also the method is named as 'OperationId' as well as on the classes in the
above section.

```java
public interface ISubscribeOperation {

  void subscribeOperation(CreateOrder value);
}
```

##### Mapper

The entities used for the definitions both on the previous seen classes and
this interfaces, are auto-generated entities, based on the same YML file.
Because of that, they need to be mapped to a user defined entity using a mapper
utility class.

This mapper must be defined by the user on it's own way to improve the
personalitation capabilities of the plugin.

Down here you have an example of the mapper utility class as well as an simple
class implementing the interface defined above.

```java
@Mapper
public interface Mapper {
  Order map(net.coru.apigenerator.asyncapi.business_model.model.event.Order value);
}
```

###### Implementation

```java
@Component
public class subscribeOperation implements ISubscribeOperation {
  private final Mapper mapper;

  public subscribeOperation(final Mapper mapper) {this.mapper = mapper;}

  @Override
  public void subscribeOperation(final Order value) {
    net.coru.apigenerator.asyncapi.business_model.model.Order orderMapped = mapper.map(value);
    //TODO: implement the functionality
  }
}
```

#### Stream Bridge class

In this case, there is only one class where all the selected operations will be
included. It's name could be modified using the `classNamePostfix` parameter
specified on the [Usage section](#using-in-other-projects), being by default
**StreamBridgeProducer**.

```java
@Configuration
public class StreamBridgeProducer {

    private StreamBridge streamBridge;

    public void streamBridgeOperation(CreateOrder createOrder){
        streamBridge.send("publishOperation", createOrder);
    }
}
```

This sample class, is related to the previosly used YML file, and in it you
could see that it came fully implemented.

Also, it's important to note that using Stream Bridge, the *binding* where the
messages are going to be sent is included in the auto generated class. This is
defined by the application properties using `function`, `binders` and
`bindings`, as in the next example:

```yaml
spring:
  kafka:
    bootstrap-servers: localhost:xxxx
    producer:
      client-id: peter
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
  cloud:
    function:
      definition: publishOperation
    stream:
      defaultBinder: kafka
      bindings:
        publishOperation:
          destination: orderCreated
      binders:
        kafka:
          defaultCandidate: true
          type: kafka
          producer-properties:
            key.serializer: org.apache.kafka.common.serialization.StringSerializer
            value.serializer: org.springframework.kafka.support.serializer.JsonSerializer
```

Because the plugin cannot access the application properties, the name of the
corresponding *binding* must be used as the **channel identifier** in the YML
file that's setted on the plugin configuration, as you can see on the next
extract:

```yaml
channels:
  publishOperation:
    subscribe:
      operationId: "streamBridgeOperation"
      message:
        $ref: '#/components/messages/CreateOrder'
```

Due to the limitations on topics naming, the identifier of the channels that
are going to be used as Stream Bridge publishers, **only could include `-` or
`.` as separators**, slash `/` is not allowed.

## OpenApi Generator

### Getting Started

In order to get this plugin working, you need the following things installed
in your computer:

- Java 11 Version
- Maven

Depending on the approach with which you are going to use the plugin, other
dependencies will be necessary, for example:

- spring-boot-starter-webflux, in case you want to implement an API with
responses in Mono/Flux Reactor types or use them for external calls through
Spring WebClient.

After you have these installed, you need to add this plugin in your pom.xml
file. Here is an example of a basic configuration:

```xml
<plugin>
  <groupId>net.coru</groupId>
  <artifactId>scs-multiapi-maven-plugin</artifactId>
  <version>3.0.1</version>
  <executions>
    <execution>
        <goals>
            <goal>openapi-generation</goal>
        </goals>
        <configuration>
            <fileSpecs>
                <fileSpec>
                    <filePath>${project.basedir}/src/main/resources/api/api.yml</filePath>
                    <apiPackage>net.coru.apigenerator.openapi.api</apiPackage>
                    <modelPackage>net.coru.apigenerator.openapi.api.model</modelPackage>
                    <modelNameSuffix>DTO</modelNameSuffix>
                </fileSpec>
            </fileSpecs>
        </configuration>
    </execution>
  </executions>
</plugin>
```

### Initial Considerations

Before using this plugin we have to warn that not all the complexity and
support offered by the use of swagger.io yml files is supported.

Since 1.1.0 version, we support the definition of parameters in both Path
and Operation object, but you can only define it in one of them.
If you specify them in both objects it will trigger an Exception.

We establish here some of these options that are not yet supported and that
will be added to this plugin as time goes by and the existing need among users.

- The use of parameters defined in the component element by reference.

- The use of parameters with content tag.

- Using Multiple Authentication Types within the security options both at an
operational and general level.

- The use of OAuth 2 and OpenID Connect Discovery Authentication Types.

### Usage

This plugin allows us to create multiple apis with just one maven clean
install execution, in this way the user can configure several fileSpecs tags
with different uses, thus generating Apis in the two possible modes: send or
receive calls, depending on the options of configuration selected in said
fileSpecs.

```xml
<configuration>
    <fileSpecs>
        <fileSpec>
            <filePath>${project.basedir}/src/main/resources/api/api.yml</filePath>
            <apiPackage>net.coru.apigenerator.openapi.api</apiPackage>
            <modelPackage>net.coru.apigenerator.openapi.api.model</modelPackage>
            <modelNameSuffix>DTO</modelNameSuffix>
        </fileSpec>
    </fileSpecs>
</configuration>
```

To customize these fileSpecs tags we are going to specify them inside the
configuration tag, we must declare the fileSpecs tag that contains all files
that will be used. Each fileSpec has their own configuration:

| Name                     | Description                                                                                                                                                                                         | Example                                           |
|--------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------|
| filePath                 | Path where the yaml is located                                                                                                                                                                      | ${project.basedir}/src/main/resources/api/api.yml |
| apiPackage               | Path where the api interface will be located                                                                                                                                                        | net.coru.apigenerator.openapi                     |
| modelPackage             | Path where the models will be located                                                                                                                                                               | net.coru.apigenerator.openapi.model               |
| modelNamePrefix          | Prefix that will be used ahead of every model´s name                                                                                                                                                | Api                                               |
| modelNameSuffix          | Suffix that will be used after every model´s name                                                                                                                                                   | DTO                                               |
| callMode                 | Boolean value to decide if you want to generate the api for external calls. **Use RestClient by default. It´s initialized to false by default**                                                     | false                                             |
| useTagsGroup             | Boolean value to decide if using tags instead of an URL for group the API. **It´s initialized to false by default**                                                                                 | false                                             |
| useLombokModelAnnotation | Boolean value to decide if you want your models with Lombok or not   **It´s initialized to false by default**                                                                                       | false                                             |
| isReactive               | Boolean value to decide if you want to generate the api with responses in Mono/Flux Reactor types. If callmode = true use WebClient instead of RestClient. **It´s initialized to false by default** | false                                             |

As the configuration options already indicate, the data model will also be
created within the specified path.This model will be created with the indicated
prefixes and suffixes and the instances and imports will be made to that model
within the corresponding Api.

There are two properties configured outside the fileSpecs, the path where the
RestClient and the WebClient will be located, if this option is set in any
of the fileSpecs, and the name of the folder where the generated sources will
be saved in the api of the project.

| Name                                                | Description                                                                                                                                                                                                                                                                    | Example                              |
|-----------------------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|--------------------------------------|
| clientPackage                                       | Path where the RestClient and/or WebClient are located                                                                                                                                                                                                                         | net.coru.apigenerator.openapi.client |
| [generatedSourcesFolder](#generated-sources-folder) | Name of the folder, inside `target`, where the files will be located. By defaut it's `generated-sources`                                                                                                                                                                       | generated-sources                    |
| overWriteModel                                      | Boolean value to decide if you want your models to be overwritten if two or more models have the same name. True means that models will be overwritten and if false is set, it will throw an exception if two models share the same name.It is initialized to false by default | true                                 |

We must clarify that the options to make calls are configured under the
RestClient or WebClient specifications as indicated above in the configuration
options. If several of the APIs to be generated are defined under the same call
option, a single RestClient/Webclient will be generated for all of them, which
is initialized with the specific options needed within the class that defines
each API.

### Usage considerations

This plugin has been implemented trying to behave like OpenApi Generator Tool
but we decided to change the approach concerning the support of AllOfs, OneOfs
and AnyOfs.

Every property that has been indicated in any of these types will be generated
in the model entity.

The way the model will behave changes depending on whether it is an AllOf, or
an AnyOf/OneOf:

If it is an AllOf, every property referenced will be treated as required
regardless of which ones are defined in the "required" field of the allOf
structure.

If it is an AnyOf or an OneOf, the plugin will only mark as required the
properties that have been defined as such in the "required" field of these
structures. After that, the constructor will check that at least one of the
properties will have a value, nothing else, so it is up to the user to fulfill
the restrictions he needs for the entity.

**IMPORTANT NOTE**: As previously stated, OneOf and AnyOf will behave the same,
this means that OneOf will work the same way as an AnyOf.
