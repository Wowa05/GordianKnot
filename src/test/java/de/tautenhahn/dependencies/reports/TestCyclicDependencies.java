package de.tautenhahn.dependencies.reports;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import java.nio.file.Paths;

import org.junit.Test;

import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ParsedClassPath;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import de.tautenhahn.dependencies.rest.TestProjectView;


/**
 * Unit test for finding cyclic package dependencies.
 *
 * @author TT
 */
public class TestCyclicDependencies
{

  /**
   * Assert that report mentions causing classes.
   */
  @Test
  public void report()
  {
    assertThat("just creating a cylcle to report", TestProjectView.class, notNullValue());
    ProjectScanner scanner = new ProjectScanner(new Filter());
    ContainerNode root = scanner.scan(new ParsedClassPath(Paths.get("build", "classes", "java", "test")
                                                               .toString()));
    assertThat("report",
               CyclicPackageDependencies.findFor(root).toString(),
               containsString("TestCyclicDependencies -> TestProjectView"));

  }
}
