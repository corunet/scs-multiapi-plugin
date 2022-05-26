package com.corunet.api.generator.openapi.integrationTest;

import static com.corunet.api.generator.testUtils.TestUtils.validateFiles;
import static com.soebes.itf.extension.assertj.MavenITAssertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.soebes.itf.jupiter.extension.MavenGoal;
import com.soebes.itf.jupiter.extension.MavenJupiterExtension;
import com.soebes.itf.jupiter.extension.MavenRepository;
import com.soebes.itf.jupiter.extension.MavenTest;
import com.soebes.itf.jupiter.maven.MavenProjectResult;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

@MavenRepository
@MavenJupiterExtension
@Execution(ExecutionMode.SAME_THREAD)
public class OpenApiGenerationTest {

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testApiClientGeneration(MavenProjectResult result) throws IOException {
    List<File> expectedFile = List.of(
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiClientGeneration/assets/TestApi.java"));

    List<File> expectedModelFiles = List.of(
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiClientGeneration/assets/ApiErrorDTO.java"),
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiClientGeneration/assets/ApiTestDTO.java"),
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiClientGeneration/assets/ApiTestInfoDTO.java")
    );

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    Path pathToTargetApi = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testApi");
    Path pathToTargetModel = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testApi/model");

    File targetApiFolder = pathToTargetApi.toFile();
    assertThat(targetApiFolder).isNotEmptyDirectory();

    File targetModelFolder = pathToTargetModel.toFile();
    assertThat(targetModelFolder).isNotEmptyDirectory();

    validateFiles(expectedFile, targetApiFolder);
    validateFiles(expectedModelFiles, targetModelFolder);
  }

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testApiReactiveGeneration(MavenProjectResult result) throws IOException {
    List<File> expectedFile = List.of(new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiReactiveGeneration/assets" +
                                               "/TestApi.java"));

    List<File> expectedModelFiles = List.of(
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiReactiveGeneration/assets/ApiErrorDTO.java"),
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiReactiveGeneration/assets/ApiTestDTO.java"),
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiReactiveGeneration/assets/ApiTestInfoDTO.java")
    );

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    Path pathToTargetApi = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testReactiveApi");
    Path pathToTargetModel = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testReactiveApi/model");

    File targetApiFolder = pathToTargetApi.toFile();
    assertThat(targetApiFolder).isNotEmptyDirectory();

    File targetModelFolder = pathToTargetModel.toFile();
    assertThat(targetModelFolder).isNotEmptyDirectory();

    validateFiles(expectedFile, targetApiFolder);
    validateFiles(expectedModelFiles, targetModelFolder);
  }

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testApiTagsGeneration(MavenProjectResult result) throws IOException {

    List<File> expectedModelFiles = List.of(
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiTagsGeneration/assets/TestTagFirstApi.java"),
        new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiTagsGeneration/assets/TestTagSecondApi.java"));

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    pathToTarget = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testTags");

    File targetApiFolder = pathToTarget.toFile();
    assertThat(targetApiFolder).isNotEmptyDirectory();

    validateFiles(expectedModelFiles, targetApiFolder);
  }

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testApiMultiGeneration(MavenProjectResult result) throws IOException {
    List<File> expectedFileFirst = List.of(new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiMultiGeneration/assets" +
                                                    "/TestFirstApi.java"));
    List<File> expectedFileSecond = List.of(new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testApiMultiGeneration/assets" +
                                                     "/TestSecondApi.java"));

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    Path pathToTargetFirst = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testMultiFile/first");
    Path pathToTargetSecond = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testMultiFile/second");

    File targetFirstFolder = pathToTargetFirst.toFile();
    assertThat(targetFirstFolder).isNotEmptyDirectory();

    File targetSecondFolder = pathToTargetSecond.toFile();
    assertThat(targetFirstFolder).isNotEmptyDirectory();

    validateFiles(expectedFileFirst, targetFirstFolder);
    validateFiles(expectedFileSecond, targetSecondFolder);
  }

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testWebClientApiGeneration(MavenProjectResult result) throws IOException {
    List<File> expectedFileFirst = List.of(new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testWebClientApiGeneration/assets" +
                                             "/TestApi.java"));

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    Path pathToTargetFirst = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testWebClient");

    File targetFirstFolder = pathToTargetFirst.toFile();
    assertThat(targetFirstFolder).isNotEmptyDirectory();

    validateFiles(expectedFileFirst, targetFirstFolder);
  }

  @MavenTest
  @MavenGoal("${project.groupId}:${project.artifactId}:${project.version}:openapi-generation")
  void testRestClientApiGeneration(MavenProjectResult result) throws IOException {
    List<File> expectedFileFirst = List.of(new File("src/test/resources/com/corunet/api/generator/openapi/integrationTest/OpenApiGenerationTest/testRestClientApiGeneration" +
                                                    "/assets/TestApi.java"));

    assertThat(result).hasTarget();
    Path pathToTarget = result.getTargetProjectDirectory().toPath();
    Path pathToTargetFirst = pathToTarget.resolve("target/generated-sources/corunet/apigenerator/com/corunet/multifileplugin/testRestClient");

    File targetFirstFolder = pathToTargetFirst.toFile();
    assertThat(targetFirstFolder).isNotEmptyDirectory();

    validateFiles(expectedFileFirst, targetFirstFolder);
  }
}
