# functions for data analysis of esa vectors
# execute analyze_esa_data.R first

# basic stats for a vector (word, phrase, document) with id str
vector.stats <- function(str) {
  v <- get.vector(str)
  print(paste("num. concepts: ",length(v$value)))
  print(summary(v$values))
  par(mfrow=c(2,1), cex=0.8, pch=19)
  hist(v$values, breaks="Scott", main=paste("values of \"", str,"\""))
  plot(v$values[order(v$values)], ylab="values")
}

vector.boxplot <- function(str) {
  v <- get.vector(str)
  boxplot(v$values)
}

# for tokens in a string, print weigth (given by att = sum | length)
print.word.vec.weight <- function(words, att) {
  tok <- strsplit(words, "[[:blank:][:punct:]]+")
  d <- get.word.weights()
  if (att=="length") arr <- d$len else arr <- d$sum 
  for (t in tok) {
    i <- match(t,d$words)
    print(paste(t," ",arr[i]))
  }
}

# sort words by vector attribute (length or sum)
sort.words.by.vec <- function(att) {
  d <- get.word.weights()  
  if (att=="length") arr <- d$len else arr <- d$sum 
  o <- order(arr, decreasing=T)
  for (i in o) {
    w <- d$words[i];
    a <- arr[i]    
    print(paste(w," ",a))
  }
  print(summary(arr))
  hist(arr, breaks="Scott")
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

# number of non-zero entries in the word vector 
vector.length <- function(arg) {
  vec <- get.vector(arg)
  return (length(vec$values))  
}

# distribution of various vector attributes accross words
word.vector.stats <- function(stat) {
  words <- list.words() 
  s <- c(NA); length(s) <- length(words)  
  for (i in 1:length(words)) {     
    v <- get.vector(words[i])
    if (stat == "num_concepts") s[i] <- length(v$values)
    else if (stat == "mean_value") s[i] <- mean(v$values)
    else if (stat == "median_value") s[i] <- median(v$values)
    else if (stat == "spread") {
      sum <- summary(v$values)
      s[i] <- (sum["3rd Qu."] - sum["1st Qu."])
    }
    else s[i] <- NA
  }
  print(summary(s))
  hist(s, breaks="Scott", main=paste("words - ",stat))
}


# number of shared non-zero concepts for a pair of strings
concept.match.num <- function(w1, w2) {
  v1 = get.vector(w1)
  v2 = get.vector(w2)
#   m1 = as(v1$vector01,"CsparseMatrix")
#   m2 = as(v2$vector01,"CsparseMatrix")
#   sum(m1 * m2)
  sum(v1$vector01 * v2$vector01)
}



# list of shared non-zero concepts for a pair of strings
concept.match <- function(w1, w2) {
  v1 = get.vector(w1)
  v2 = get.vector(w2)  
  shared <- intersect(v1$indices, v2$indices)
  print(paste("num shared: ", length(shared)))
  if (length(v1$indices) < length(v2$indices)) smaller <- v1
  else smaller <- v2
  for (i in 1:length(smaller$indices)) {
    if (is.element(smaller$indices[i], shared)) {
      print(smaller$names[i])
    }
  }  
}


# given a word, produce summary of concept overlaps with other words
concept.match.summary <- function(w) {
  wl <- list.words()
  cm <- c(NA); length(v) <- length(wl); i <- 1
  for (word in wl) { 
    cm[1] <- concept.match(w,word)
    i <- i + 1
  }
  summary(cm)
}

# analysis of concept match matrix
concept.match.analysis <- function(cmm) {  
  v <- as.vector(cmm)
  print(paste("number of pairs: ", sum(!is.na(v))))  
  print(paste("number of zeros: ", sum(v[!is.na(v)]==0)))  
  print(summary(v))
  #hist(v, breaks="Scott",xlim=c(1,100),ylim=c(0,1),freq=F)  
  hist(v, breaks="Scott",freq=F)  
}

# cosine similarity between two sparse vectors
cos.sim <- function(v1, v2) {
  prod <- sum(v1 * v2)
  n1 <- sqrt(sum(v1 * v1))
  n2 <- sqrt(sum(v2 * v2))
  prod / (n1*n2)
}

print.clusters <- function(hclustering, hval) {
  cut <- cutree(hclustering, h=hval)
  words <- tab$string[which(tab$type == "word")]
  numClust <- max(cut)
  print(numClust)
  cluster <- list(); length(cluster) <- numClust
  print(cluster[[49]])
  i <- 1
  for (ci in cut) {    
    if (is.null(cluster[[ci]])) cluster[[ci]] <- words[i]
    else cluster[[ci]] <- c(cluster[[ci]], words[i])
    i <- i + 1
  }
  cluster
}

# return matrix that for each pair of words (indices of words in tab)
# holds number of common non-zero concepts
concept.match.matrix <- function(method) {
  # select word indices
  wind <- which(tab$type == "word")
  l <- length(wind)
  mtrx <- matrix(rep(NA,l*l),nrow=l,ncol=l)
  r <- 1
  for (i1 in wind) {    
    c <- 1
    for (i2 in wind) {
      if (method == "concept.match") {
        if (r < c) {          
          v1 <- get.vector(i1)
          v2 <- get.vector(i2)
          mtrx[r,c] <- sum(v1$vector01 * v2$vector01)
          }      
      }
      else if (method == "cosine") {
        if (r < c) { 
          v1 <- get.vector(i1)
          v2 <- get.vector(i2)
          #mtrx[r,c] <- (1 - cos.sim(v1$vector, v2$vector))
          prod <- sum(v1$vector * v2$vector)
          n1 <- sqrt(sum(v1$vector * v1$vector))
          n2 <- sqrt(sum(v2$vector * v2$vector))        
          mtrx[r,c] <- prod / (n1*n2)          
        }
        else if (r == c) mtrx[r,c] <- 1
        else mtrx[r,c] <- mtrx[c,r]
      }
      else stop("unknown method")
      c <- c + 1
    }
    print(r)
    r <- r + 1    
  }
  return(mtrx)
}
