package com.navigator.service.jdbsserviceimpl;

import com.navigator.db.dao.GraphDao;
import com.navigator.db.dao.GraphDaoImpl;
import com.navigator.db.exceptions.ServiceException;
import com.navigator.model.Edge;
import com.navigator.model.Graph;
import com.navigator.model.Node;
import com.navigator.math.FloydWarshall;
import com.navigator.service.PathResult;
import com.navigator.service.intarfaces.IGraphService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NavigatorService implements IGraphService {
    private static final Logger logger = LogManager.getLogger(NavigatorService.class);

    private final GraphDao graphDao;
    private Graph graph;
    private FloydWarshall fw;

    public NavigatorService() {
        this.graphDao = new GraphDaoImpl();
        logger.info("NavigatorService initialized with internal GraphDao");
    }

    @Override
    public void reloadGraph() throws ServiceException {
        try {
            logger.info("Reloading graph from database...");

            List<Node> nodes = graphDao.getAllNodes();
            List<Edge> edges = graphDao.getAllEdges();

            logger.debug("Nodes loaded: {}", nodes);
            logger.debug("Edges loaded: {}", edges);

            this.graph = new Graph(nodes, edges);
            this.fw = new FloydWarshall(graph);

            logger.info("Computing all-pairs shortest paths with Floyd-Warshall...");
            fw.compute();

            logger.info("Graph reloaded successfully with {} nodes and {} edges", nodes.size(), edges.size());
        } catch (Exception e) {
            logger.error("Failed to reload graph", e);
            throw new ServiceException("Error reloading graph", e);
        }
    }

    @Override
    public boolean addNode(String name, double x, double y) {
        return false;
    }

    @Override
    public boolean addEdge(String fromName, String toName, double weight) {
        return false;
    }

    @Override
    public boolean removeNode(String name) {
        return false;
    }

    @Override
    public boolean removeEdge(String fromName, String toName) {
        return false;
    }

    @Override
    public List<Node> getAllNodes() throws ServiceException {
        logger.info("Fetching all nodes from database");
        List<Node> nodes = graphDao.getAllNodes();
        logger.debug("Nodes retrieved: {}", nodes);
        return nodes;
    }

    @Override
    public List<Edge> getAllEdges() throws ServiceException {
        logger.info("Fetching all edges from database");
        List<Edge> edges = graphDao.getAllEdges();
        logger.debug("Edges retrieved: {}", edges);
        return edges;
    }

    @Override
    public PathResult shortestPath(String startName, String endName) throws ServiceException {
        if (graph == null || fw == null) {
            logger.info("Graph or FloydWarshall not initialized, reloading graph...");
            reloadGraph();
        }

        logger.info("Calculating shortest path from '{}' to '{}'", startName, endName);

        Node s = graph.getNodes().stream()
                .filter(n -> n.name.equalsIgnoreCase(startName))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Start node not found: {}", startName);
                    return new ServiceException("Start node not found: " + startName);
                });

        Node t = graph.getNodes().stream()
                .filter(n -> n.name.equalsIgnoreCase(endName))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("End node not found: {}", endName);
                    return new ServiceException("End node not found: " + endName);
                });

        int si = graph.indexOf(s.id);
        int ti = graph.indexOf(t.id);

        double dist = fw.getDistanceMatrix()[si][ti];
        if (Double.isInfinite(dist)) {
            logger.error("No path found between {} -> {}", startName, endName);
            throw new ServiceException("No path found between nodes: " + startName + " -> " + endName);
        }

        List<String> pathNames = fw.reconstructPath(si, ti).stream()
                .map(i -> graph.getNodes().get(i).name)
                .toList();

        logger.info("Shortest path {} -> {} found, distance: {}", startName, endName, dist);
        logger.debug("Path sequence: {}", pathNames);

        return new PathResult(dist, pathNames);
    }
}
