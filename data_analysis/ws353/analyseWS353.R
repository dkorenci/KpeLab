library(plyr)
setwd("/data/rudjer/code/kpe/KpeLab/data_analysis")
fileName <- "ws353esa.dat"; f <- 10
#fileName <- "ws353lsi_cosscaled.dat"; f <- 1
wssim <- read.table(fileName, sep=",", header=TRUE)
wssim <- arrange(wssim, sim)
par(mfrow=c(2,1))
plot(wssim$sim[order(wssim$sim)]*0.1, pch=19, col="red")
points(wssim$vsim[order(wssim$vsim)]*f, pch=19, col="blue")
plot(wssim$sim*0.1, pch=19, col="red")
points(wssim$vsim*f, pch=19, col="blue")
print(cor(wssim$sim, wssim$vsim, method="spearman"))