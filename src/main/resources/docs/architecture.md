                  ┌──────────────┐
                  │     CLI      │
                  │ (User I/O)   │
                  └──────┬───────┘
                         │
                         ▼
                ┌────────────────── ┐
                │ NavigatorService  │
                │   (coordinates    │
                │     logic)        │
                └────── ┬───────────┘
           ┌────────────┴─────────────┐
           │                          │
           ▼                          ▼
  ┌─────────────┐                   ┌──────────────┐
  │  Database   │                   │ FloydWarshall│
  │ (Nodes &    │ ◄────────────►    │ (Math module │
  │  Edges)     │                   │ shortest path│
  │ DAO / Pool) │                   │ calculation) │
  └─────────────┘                   └──────────────┘
          ▲                           ▲
          │                           │
          └───────── Data returns─────┘
