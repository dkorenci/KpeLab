# ws353 mapping of human similarities -> distributinal similarities 
# use ridge regression to map vector of functions of human -> dist

# read data and setup environment
library(parcor)
setwd("/data/rudjer/code/kpe/KpeLab/data_analysis")
#fileName <- "ws353esa.dat"; f <- 10
#fileName <- "ws353lsi_cosscaled.dat"; f <- 1
#wssim <- read.table(fileName, sep=",", header=TRUE)

getFunctions <- function() {
  c(    
    sqrt, 
    function(x) {x},
    function(x) {x*x},
    function(x) {x*x*x}
  )
}

applyFunctions <- function(x, func) {
  v <- c();
  for (f in func) { v <- append(v,f(x)); }
  v
}

createMatrix <- function(x) {
  v <- c(); 
  f <- getFunctions();
  for (val in x) {
    v <- append(v, applyFunctions(val, f));
  }
  matrix(v, nrow = length(x), ncol = length(f), byrow=T)
}

doRidge <- function() {
  X <- createMatrix(wssim$vsim);
  ridge.cv(X, wssim$sim, plot.it=F)
}

mapRidge <- function(x, r) {
  val <- r$intercept; f <- getFunctions();
  for (i in 1:length(f)) {
    val <- val + f[[i]](x) * r$coefficients[i];
  }
  val;
}
  
# map human sim to distr. sim 
r <- doRidge();
wssim$tvsim <- laply(wssim$vsim, mapRidge, r);
