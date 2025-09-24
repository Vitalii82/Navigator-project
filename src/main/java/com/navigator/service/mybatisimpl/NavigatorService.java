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
    private final GraphDao graphDao;


    private Graph graph;
    private FloydWarshall fw;

    public NavigatorService(GraphDao graphDao) {
        this.graphDao = graphDao;
    }

    private void ensureGraphLoaded() throws ServiceException {
        if (graph == null || fw == null) {
            reloadGraph();
        }
    }

    @Override
    public synchronized void reloadGraph() throws ServiceException {
        try (SqlSession sqlSession = SESSION_FACTORY.openSession()) {
            GraphDao dao = sqlSession.getMapper(GraphDao.class);

            List<Node> nodes = dao.getAllNodes();
            List<Edge> edges = dao.getAllEdges();

            this.graph = new Graph(nodes, edges);
            this.fw = new FloydWarshall(graph);
            fw.compute();

            logger.info("Graph loaded with {} nodes and {} edges", nodes.size(), edges.size());
        } catch (Exception e) {
            logger.error("Error reloading graph", e);
            throw new ServiceException("Error reloading graph", e);
        }
    }

    @Override
    public List<Node> getAllNodes() throws ServiceException {
        ensureGraphLoaded();
        return graph.getNodes();
    }

    @Override
    public List<Edge> getAllEdges() throws ServiceException {
        ensureGraphLoaded();
        return graph.getEdges();
    }

    @Override
    public PathResult shortestPath(String startName, String endName) throws ServiceException {
        ensureGraphLoaded();

        Node s = graph.getNodes().stream()
                .filter(n -> n.name.equalsIgnoreCase(startName))
                .findFirst()
                .orElseThrow(() -> new ServiceException("Start node not found: " + startName));

        Node t = graph.getNodes().stream()
                .filter(n -> n.name.equalsIgnoreCase(endName))
                .findFirst()
                .orElseThrow(() -> new ServiceException("End node not found: " + endName));

        int si = graph.indexOf(s.id);
        int ti = graph.indexOf(t.id);

        double dist = fw.getDistanceMatrix()[si][ti];
        if (Double.isInfinite(dist)) {
            throw new ServiceException("No path found between nodes: " + startName + " -> " + endName);
        }

        var pathIdx = fw.reconstructPath(si, ti);
        var names = pathIdx.stream()
                .map(i -> graph.getNodes().get(i).name)
                .toList();

        logger.info("Shortest path from {} to {}: distance={}, path={}", startName, endName, dist, names);
        return new PathResult(dist, names);
    }
}
