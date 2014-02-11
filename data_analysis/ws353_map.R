# ws353 mapping of human similarities -> distributinal similarities 

# read data and setup environment
library(parcor)
setwd("/data/rudjer/code/kpe/KpeLab/data_analysis")
fileName <- "ws353esa.txt"; f <- 10
#fileName <- "ws353lsi_cosscaled.txt"; f <- 1
wssim <- read.table(fileName, sep=",", header=TRUE)
