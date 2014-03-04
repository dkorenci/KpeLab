setwd("/data/rudjer/code/kpe/KpeLab/data_analysis/esa_page_rank")
library("igraph")
#adjm <- as.matrix(read.table("H-83_matrix.txt"))
#terms <- read.table("H-83_terms.txt")
adjme <- exp(adjm)-1
adjm01 <- adjm
adjm01[adjm01 != 0] <- 1

ge <- graph.adjacency(adjme, mode="undirected", weighted=TRUE, diag=FALSE,
                     add.colnames=NA, add.rownames=NA)
g <- graph.adjacency(adjm01, mode="undirected", weighted=TRUE, diag=FALSE,
                      add.colnames=NA, add.rownames=NA)

pr <- page.rank(g, directed=FALSE)
pre <- page.rank(ge, directed=FALSE)

ord <- order(pr$vector, decreasing=TRUE)
orde <- order(pre$vector, decreasing=TRUE)

t <- terms$V1[ord]
te <- terms$V1[orde]

rf<-file("ranks.txt")
lines <- c()
for (i in 1:length(t)) {
  lines <- append(lines, paste(t[i], " ",(pr$vector[ord])[i], " " ,te[i], " ",(pre$vector[orde])[i]))
}
writeLines(paste(lines,collapse="\n"), rf)
close(rf)