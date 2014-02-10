# exploratory graphs for ws353 similarity measures

# read data and setup environment
library(plyr)
setwd("/data/rudjer/code/kpe/KpeLab/data_analysis")
# f is scaling factor for computer similarity
fileName <- "ws353esa.txt"; f <- 10
#fileName <- "ws353lsi_cosscaled.txt"; f <- 1
wssim <- read.table(fileName, sep=",", header=TRUE)
# remove pairs with similarity 1 (same words in a pair)
wssim <- wssim[wssim$vsim < 1,] 
# scale similarities to [0,1]
wssim$sim <- wssim$sim / max(wssim$sim)
wssim$vsim <- wssim$vsim / max(wssim$vsim)

# set graphic params
par(mfrow=c(1,1), cex=1, pch=19)

# both similarity measures sorted
plotSorted <- function() {
  wssim <- arrange(wssim, sim);
  plot(wssim$sim[order(wssim$sim)], col="red", cex=0.4);
  points(wssim$vsim[order(wssim$vsim)], col="blue", cex=0.4);  
}

#scatterplot
humanVsComputer <- function() {
    plot(wssim$sim, wssim$vsim, col="red", cex=0.4)
}

# human measure sorted vs computer unsorted
plotUnsorted <- function() {
  wssim <- arrange(wssim, sim);
  plot(wssim$sim, col="red",cex=0.4);
  points(wssim$vsim,col="blue",cex=0.4);
}

histograms <- function() {
  hist(wssim$sim, col=rgb(1,0,0,0.6), breaks = 30, 
       ylim=c(0,150), xlim=c(0,1));
  hist(wssim$vsim, col=rgb(0,0,1,0.6), breaks = 30, add=T);
}



#print(cor(wssim$sim, wssim$vsim, method="spearman"))