# calculates page rank
# expects following variables:
# N - size of the graph matrix
# flatMatrix - weighted adjacency matrix as numeric N*N vector, row by row
# persRank - personalized rank values, vector of size N
# returns: numeric vector of ranks assigned to variable result

# test: 
# N = 10
# flatMatrix = rep(1, N*N)
# persRank = rep(1, N)

library("igraph")
adjMatrix <- matrix(flatMatrix,nrow=N,ncol=N,byrow=T)
g <- graph.adjacency(adjMatrix, mode="undirected", weighted=TRUE, diag=FALSE,
                      add.colnames=NA, add.rownames=NA)
pr <- page.rank(g, directed=FALSE, personalized=persRank)
print("hi")
result <- pr$vector