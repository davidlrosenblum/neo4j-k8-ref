helm install nba-core-1 neo4j-experimental/neo4j-cluster-core -f core1.yaml
helm install nba-core-2 neo4j-experimental/neo4j-cluster-core -f core2.yaml
helm install nba-core-3 neo4j-experimental/neo4j-cluster-core -f core3.yaml
#helm install nba-core-4 neo4j-experimental/neo4j-cluster-core -f rr1.yaml
#helm install nba-core-5 neo4j-experimental/neo4j-cluster-core -f rr2.yaml
#helm install nba-rr-1  neo4j-experimental/neo4j-cluster-read-replica -f rr1.yaml
#helm install nba-rr-2  neo4j-experimental/neo4j-cluster-read-replica -f rr2.yaml
#helm install nba-svc-headless neo4j-experimental/neo4j-cluster-headless-service --set neo4j.name=nba-cluster 
