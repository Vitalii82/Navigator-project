DROP TABLE IF EXISTS edges;
DROP TABLE IF EXISTS nodes;

CREATE TABLE nodes (
  id INT PRIMARY KEY,
  name VARCHAR(64) NOT NULL UNIQUE,
  x DOUBLE NOT NULL,
  y DOUBLE NOT NULL
);

CREATE TABLE edges (
  from_id INT NOT NULL,
  to_id INT NOT NULL,
  weight DOUBLE NOT NULL,
  CONSTRAINT fk_from FOREIGN KEY (from_id) REFERENCES nodes(id),
  CONSTRAINT fk_to FOREIGN KEY (to_id) REFERENCES nodes(id)
);

-- Seed: square A-B-C-D with diagonals
INSERT INTO nodes (id, name, x, y) VALUES
  (1,'A',0,0),
  (2,'B',1,0),
  (3,'C',1,1),
  (4,'D',0,1);

INSERT INTO edges (from_id, to_id, weight) VALUES
  (1,2,1.0),(2,1,1.0),
  (2,3,1.0),(3,2,1.0),
  (3,4,1.0),(4,3,1.0),
  (4,1,1.0),(1,4,1.0),
  (1,3,1.41421356237),(3,1,1.41421356237),
  (2,4,1.41421356237),(4,2,1.41421356237);
