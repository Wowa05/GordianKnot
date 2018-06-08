package de.tautenhahn.dependencies.rest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.tautenhahn.dependencies.analyzers.DiGraph;
import de.tautenhahn.dependencies.analyzers.DiGraph.IndexedNode;
import de.tautenhahn.dependencies.parser.ClassNode;
import de.tautenhahn.dependencies.parser.Node;
import de.tautenhahn.dependencies.parser.Node.ListMode;


/**
 * Information to display about a node.
 *
 * @author TT
 */
public class NodeInfo
{

  private final String nodeName;

  private String resourceType;

  private String resourceName;

  private final String type;

  private String name;

  private final String listMode;

  private final int numberContainedClasses;

  private final int numberCollapsable;

  private final int numberExpandable;

  /**
   * Creates immutable instance
   *
   * @param graph
   * @param number
   */
  public NodeInfo(DiGraph graph, int number)
  {
    this(graph.getAllNodes().get(number));
  }

  /**
   * Creates immutable instance
   *
   * @param node
   */
  public NodeInfo(IndexedNode node)
  {
    nodeName = node.getNode().getName();
    numberContainedClasses = node.getNumberClasses();
    Node n = node.getNode();
    ListMode originalMode = n.getListMode();
    n.setListMode(ListMode.EXPANDED);
    numberExpandable = (int)n.walkSubTree().count();
    n.setListMode(originalMode);
    numberCollapsable = (int)n.getParent().walkSubTree().count();
    listMode = node.getNode().getListMode().toString();
    parseName();
    type = getType(node.getNode());
  }

  private void parseName()
  {
    Pattern regex = Pattern.compile("([^\\.]+\\.)*(\\w+):([^\\.]+)\\.?(.*)");
    Matcher m = regex.matcher(nodeName);
    if (!m.matches())
    {
      throw new IllegalArgumentException("not a valid node name");
    }
    name = m.group(4);
    if (name.isEmpty())
    {
      name = m.group(3);
      if (m.group(1) != null)
      {
        Matcher m2 = Pattern.compile("([^\\.]+\\.)*(\\w+):([^\\.]+)\\.$").matcher(m.group(1));
        m2.find();
        resourceType = m2.group(2);
        resourceName = m2.group(3);
      }
    }
    else
    {
      resourceType = m.group(2);
      resourceName = m.group(3);
      name = m.group(4);
    }
  }

  private String getType(Node node)
  {
    if (node instanceof ClassNode)
    {
      return "class";
    }
    String simpleName = node.getSimpleName();
    int pos = simpleName.indexOf(':');
    if (pos == -1)
    {
      return node.getListMode() == ListMode.COLLAPSED ? "package tree" : "package";
    }
    return simpleName.substring(0, pos);
  }

  /**
   * Returns name of the original parsed node.
   */
  String getNodeName()
  {
    return nodeName;
  }

  /**
   * Returns type of resource node found in, null if it is a root directory of archive file.
   */
  String getResourceType()
  {
    return resourceType;
  }

  /**
   * Returns name of resource node found in, null if it is a root directory of archive file.
   */
  String getResourceName()
  {
    return resourceName;
  }

  /**
   * Returns type of represented entity.
   */
  String getType()
  {
    return type;
  }

  /**
   * Returns name of represented entity.
   */
  String getName()
  {
    return name;
  }

  /**
   * Returns the list mode.
   */
  String getListMode()
  {
    return listMode;
  }

  /**
   * Returns the number of represented classes.
   */
  int getNumberContainedClasses()
  {
    return numberContainedClasses;
  }
}
