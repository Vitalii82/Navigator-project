package com.navigator.service.intarfaces;

import com.navigator.db.exceptions.ServiceException;
import com.navigator.model.Edge;
import com.navigator.model.Node;
import com.navigator.service.PathResult;


import java.util.List;

public interface IGraphService {
    void reloadGraph() throws ServiceException;
    List<Node> getAllNodes() throws ServiceException;
    List<Edge> getAllEdges() throws ServiceException;
    PathResult shortestPath(String startName, String endName) throws ServiceException;
}
