
package com.navigator.db.dao;

import com.navigator.model.Edge;
import com.navigator.model.Node;

import java.util.List;
import java.util.Optional;

public interface GraphDao {
    List<Node> getAllNodes();
    List<Edge> getAllEdges();
    Optional<Node> getNodeById(int id);
    Optional<Edge> getEdgeById(int fromId, int toId);
    Optional<Node> getNodeByName(String name);

}
