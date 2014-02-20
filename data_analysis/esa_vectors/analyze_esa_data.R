# functions for reading and manipulating data
library("Matrix")

setwd("/data/rudjer/code/kpe/KpeLab/data_analysis/esa_vectors")
# read data
if (exists("tab") == F) {
  tab <- read.table(file="train_C-79.txt",sep="_",quote="|",
          fileEncoding="utf-8",header=TRUE,stringsAsFactors=FALSE)  
  # create empty column of parsed vectors, for caching
  tab$vector <- as.list(rep(NA,length(tab$type)))
}
# transform string with (index,title,value) triples into a list of three vectors
parse.vector <- function(vstr) {
  tokens <- strsplit(vstr,"#")
  len <- length(tokens[[1]])/3
  # init vectors and reserve space
  n <- c(); n[len] <- ""
  ind <- c(); ind[len] <- 1L
  val <- c(); val[len] <- 1
  i = 0; j <- 0
  for (tok in tokens[[1]]) {    
    if (i %% 3 == 0) j <- j + 1
    if (i %% 3 == 0) ind[j] <- as.integer(tok);
    if (i %% 3 == 1) n[j] <- tok;
    if (i %% 3 == 2) val[j] <- as.numeric(tok);    
    i <- i + 1    
  }
  # create sparse vector
  vec <- sparseVector(val, ind, .Machine$integer.max) 
  # create sparse vector with val 1 at non-zero concept  coordinates
  vec01 <- sparseVector(rep(1,len), ind, .Machine$integer.max) 
  return(list(names=n, indices=ind, values=val, vector=vec, vector01=vec01))
} 

# for the raw dataset, return index of the row with specified string value
get.index <- function(arg) {
  if (is.numeric(arg) || is.integer(arg)) 
    return(arg)
  else {
    i <- 1
    for (n in tab$string) {
      if (n == arg) return(i);
      i <- i + 1;
    }
    return (-1)
  }  
}

# get parsed vector for tab row with tab$string == arg
# or with index arg
get.vector <- function(arg) {  
  i <- get.index(arg) 
  if (i == -1) return(NULL)
  # check if vector is already constructed
  if (is.list(tab$vector[[i]]) == FALSE) {
    tab$vector[[i]] <<- parse.vector(tab$rawvector[i])
  }
  return(tab$vector[[i]])
}

# parse all string vectors in the dataset and add list of parsed values 
# as a new column in the dataset
parse.all <- function(ds) {
  l <- list() 
  i <- 1
  for (vstr in ds$rawvector) {
    l[[i]] <- parseVectorString(vstr)
    i <- i + 1
    print(i)
  }
  ds$parsed <- l
  return(ds)
}

val4name <- function(name) {
  (parse.vector(tab[get.index(tab, name), 2]))$values
}

# dataframe that has in each row: word, vector size, sum of vector values
get.word.weights <- function() {
  d <- list()  
  d$words <- list.words()
  l <- length(d$words)
  d$len <- c(NA); length(d$len) <- l
  d$sum <- c(NA); length(d$sum) <- l  
  for (i in 1:l) {
    v <- get.vector(d$words[i])
    d$len[i] <- length(v$values)
    d$sum[i] <- sum(v$values)
  }  
  d
}

list.words <- function() { tab$string[tab$type=="word"] }
list.phrases <- function() { tab$string[tab$type=="phrase"] }
# print document string (for fetching)
document <- function() { tab$string[tab$type=="document"][1] }
