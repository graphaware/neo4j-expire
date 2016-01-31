package com.graphaware.neo4j;

import com.graphaware.test.integration.GraphAwareApiTest;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

public class TempTestBootstrapper extends GraphAwareApiTest {

    @Override
    protected String propertiesFile() {
        try {
            return new ClassPathResource("neo4j-expire-manual.properties").getFile().getAbsolutePath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void justWait() throws InterruptedException {
        Thread.sleep(10000000000L);
    }
}
