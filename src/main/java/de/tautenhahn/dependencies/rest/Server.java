package de.tautenhahn.dependencies.rest;

import static spark.Spark.before;
import static spark.Spark.get;
import static spark.Spark.options;
import static spark.Spark.put;
import static spark.Spark.staticFiles;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.tautenhahn.dependencies.analyzers.CycleFinder;
import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.parser.ClassPathUtils;
import de.tautenhahn.dependencies.parser.ContainerNode;
import de.tautenhahn.dependencies.parser.Filter;
import de.tautenhahn.dependencies.parser.ProjectScanner;
import spark.Request;
import spark.Response;
import spark.ResponseTransformer;


/**
 * The REST server. Supplies the graph for displaying and allows calling some commands.
 *
 * @author TT
 */
public class Server
{

  static PrintStream out = System.out;

  private ContainerNode root;

  private final List<Function<DiGraph, DiGraph>> operations = new ArrayList<>();

  /**
   * Command line call.
   *
   * @param args
   */
  public static void main(String... args)
  {
    if (args.length == 0 || args[0].toLowerCase().matches("--?h(elp)?"))
    {
      out.println("\"Gordic Knot\" dependency checker version 0.2 alpha"
                  + "\nUsage: gordicKnot <classpathToCheck> [projectName] [options]");
      return;
    }
    new Server().init(args[0], args.length > 1 ? args[1] : null);
    out.println("Server started, point your browsert to http://localhost:4567/index.html");
  }

  void init(String classPath, String name)
  {
    List<Path> parsedPath = ClassPathUtils.parseClassPath(classPath);
    ProjectScanner analyzer = new ProjectScanner(new Filter());
    root = analyzer.scan(parsedPath);
    // root.walkSubTree().forEach(n -> n.setListMode(ListMode.LEAFS_COLLAPSED));
    startSpark();
  }

  void startSpark()
  {
    staticFiles.location("frontend");
    allowCrossSiteCalls();
    get("view", this::getDisplayableGraph, new JsonTransformer());
    put("view/node/:id/:command", null, new JsonTransformer());

  }

  DisplayableDiGraph getDisplayableGraph(Request req, Response res)
  {
    DiGraph graph = new DiGraph(root);
    for ( Function<DiGraph, DiGraph> fn : operations )
    {
      graph = fn.apply(graph);
    }
    return new DisplayableDiGraph(graph);
  }

  private DiGraph restrictToCycles(DiGraph input)
  {
    return new CycleFinder(input).createGraphFromCycles();
  }


  /**
   * All structured output to front end is JSON.
   */
  static class JsonTransformer implements ResponseTransformer
  {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public String render(Object model)
    {
      return gson.toJson(model);
    }

  }

  static void allowCrossSiteCalls()
  {
    before((request, response) -> {
      response.header("Access-Control-Allow-Origin", "*");
      response.header("Access-Control-Request-Method", "*");
      response.header("Access-Control-Allow-Headers", "*");
      // Note: this may or may not be necessary in your particular application
      response.type("application/json");
    });

    options("/*", (request, response) -> {

      String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
      if (accessControlRequestHeaders != null)
      {
        response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
      }

      String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
      if (accessControlRequestMethod != null)
      {
        response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
      }

      return "OK";
    });
  }

  /**
   * Hides everything except nodes and arcs which are part of a cyclic dependency.
   */
  void showOnlyCycles()
  {
    operations.add(this::restrictToCycles);
  }
}
