setwd("/data/rudjer/code/kpe/KpeLab/data_analysis/esa_page_rank")
adjm <- as.matrix(read.table("H-83_matrix.txt"))
adjme <- exp(adjm)-1
terms <- read.table("H-83_terms.txt")
g <- graph.adjacency(adjme, mode="undirected", weighted=TRUE, diag=FALSE,
                     add.colnames=NA, add.rownames=NA)
pr <- page.rank(g, directed=FALSE)
ord <- order(pr$vector, decreasing=TRUE)
print(terms$V1[ord])