package com.navigator.service.mybatisimpl;

import com.navigator.db.dao.GraphDao;
import com.navigator.db.exceptions.ServiceException;
import com.navigator.model.Edge;
import com.navigator.model.Graph;
import com.navigator.model.Node;
import com.navigator.math.FloydWarshall;
import com.navigator.service.PathResult;
import com.navigator.service.factory.MyBatisDaoFactory;
import com.navigator.service.intarfaces.IGraphService;

import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class NavigatorService implements IGraphService {

    private static final Logger logger = LogManager.getLogger(NavigatorService.class);
    private static final SqlSessionFactory SESSION_FACTORY = MyBatisDaoFactory.getSessionFactory();
    private Graph graph;
    private FloydWarshall fw;

    public NavigatorService() {
        logger.info("NavigatorService (MyBatis) initialized");
    }

    private void ensureGraphLoaded() throws ServiceException {
        if (graph == null || fw == null) {
            logger.info("Graph or FloydWarshall not initialized, reloading graph...");
            reloadGraph();
        }
    }

    @Override
    public synchronized void reloadGraph() throws ServiceException {
        try (SqlSession sqlSession = SESSION_FACTORY.openSession()) {
            logger.info("Reloading graph from database using MyBatis...");

            GraphDao dao = sqlSession.getMapper(GraphDao.class);

            List<Node> nodes = dao.getAllNodes();
            List<Edge> edges = dao.getAllEdges();

            logger.debug("Nodes loaded: {}", nodes);
            logger.debug("Edges loaded: {}", edges);

            this.graph = new Graph(nodes, edges);
            this.fw = new FloydWarshall(graph);

            logger.info("Computing all-pairs shortest paths with Floyd-Warshall...");
            fw.compute();

            logger.info("Graph loaded successfully with {} nodes and {} edges", nodes.size(), edges.size());
        } catch (Exception e) {
            logger.error("Error reloading graph", e);
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
        ensureGraphLoaded();
        logger.info("Fetching all nodes from graph");
        List<Node> nodes = graph.getNodes();
        logger.debug("Nodes retrieved: {}", nodes);
        return nodes;
    }

    @Override
    public List<Edge> getAllEdges() throws ServiceException {
        ensureGraphLoaded();
        logger.info("Fetching all edges from graph");
        List<Edge> edges = graph.getEdges();
        logger.debug("Edges retrieved: {}", edges);
        return edges;
    }

    @Override
    public PathResult shortestPath(String startName, String endName) throws ServiceException {
        ensureGraphLoaded();
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

        var pathIdx = fw.reconstructPath(si, ti);
        var names = pathIdx.stream()
                .map(i -> graph.getNodes().get(i).name)
                .toList();

        logger.info("Shortest path from {} to {}: distance={}, path={}", startName, endName, dist, names);
        logger.debug("Path indices: {}", pathIdx);

        return new PathResult(dist, names);
    }
}
