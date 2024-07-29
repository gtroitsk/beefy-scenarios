package io.quarkus.qe.quartz;

import static io.restassured.RestAssured.get;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import io.quarkus.qe.quartz.resources.QuartzNodeApplicationResource;
import io.quarkus.qe.quartz.resources.RestApplicationResource;
import io.quarkus.test.QuarkusProdModeTest;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.restassured.RestAssured;

@WithTestResource(H2DatabaseTestResource.class)
public class AnnotationScheduledJobsQuartzTest {

    private static final int REST_PORT = 8081;
    private static final String NODE_ONE_NAME = "node-one";
    private static final String NODE_TWO_NAME = "node-two";

    @RegisterExtension
    static final QuarkusProdModeTest nodeOneApp = new QuartzNodeApplicationResource(NODE_ONE_NAME);

    @RegisterExtension
    static final QuarkusProdModeTest nodeTwoApp = new QuartzNodeApplicationResource(NODE_TWO_NAME);

    @RegisterExtension
    static final QuarkusProdModeTest restApp = new RestApplicationResource(REST_PORT);

    @BeforeAll
    public static void beforeAll() {
        RestAssured.port = REST_PORT;
    }

    @Test
    public void testClusteringEnvironmentWithUniqueJobs() {
        whenBothNodesAreUpAndRunning();
        thenJobIsExecutedWithOwner(NODE_ONE_NAME, NODE_TWO_NAME);

        whenShutdownNodeOne();
        thenJobIsExecutedWithOwner(NODE_TWO_NAME);

        whenStartupNodeOne();
        thenJobIsExecutedWithOwner(NODE_ONE_NAME, NODE_TWO_NAME);

        whenShutdownNoneTwo();
        thenJobIsExecutedWithOwner(NODE_ONE_NAME);
    }

    private void whenBothNodesAreUpAndRunning() {
        assertFalse(nodeOneApp.getStartupConsoleOutput().isEmpty(), "Node One should be up and running");
        assertFalse(nodeTwoApp.getStartupConsoleOutput().isEmpty(), "Node Two should be up and running");
    }

    private void whenShutdownNodeOne() {
        nodeOneApp.stop();
    }

    private void whenStartupNodeOne() {
        nodeOneApp.start();
    }

    private void whenShutdownNoneTwo() {
        nodeTwoApp.stop();
    }

    private void thenJobIsExecutedWithOwner(String... possibleOwners) {
        RestAssured.port = REST_PORT;

        Awaitility.await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            final ExecutionEntity[] executions = get("/executions")
                    .then().statusCode(200)
                    .extract().as(ExecutionEntity[].class);
            assertNotEquals(0, executions.length);
            final String lastOwner = executions[executions.length - 1].owner;
            final List<String> possibleOwnersList = Arrays.asList(possibleOwners);
            assertTrue(possibleOwnersList.contains(lastOwner),
                    String.format("Owner %s not found in the list of possible owners: %s", lastOwner, possibleOwnersList));
        });
    }
}
