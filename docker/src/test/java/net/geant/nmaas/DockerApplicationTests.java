package net.geant.nmaas;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DockerApplicationTests {

	@Autowired
	@Qualifier("DockerSwarm")
	public ContainerOrchestrationProvider orchestrator;

	@Test
	public void shouldInjectDockerSwarm() {
		assertThat(orchestrator, is(notNullValue()));
		assertThat(orchestrator.info(), containsString("DockerSwarm"));
	}

}
