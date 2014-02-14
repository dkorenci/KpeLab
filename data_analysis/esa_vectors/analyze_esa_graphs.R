# functions for data analysis of esa vectors
# execute analyze_esa_data.R first

vector.hist <- function(str) {
  v <- get.vector(str)
  hist(v$values)
}

vector.boxplot <- function(str) {
  v <- get.vector(str)
  boxplot(v$values)
}

# print first N titles of pages for string, desc. ordered by frequency
freq.pages <- function(str, N = -1) {
  v <- get.vector(str)
  ord <- order(v$values, decreasing=T)
  vord <- v$values[ord]
  nord <- v$names[ord]
  if (N == -1) N <- length(vord)
  for (i in 1:N) {
    cat(nord[i]," ",vord[i],"\n")
  }
}