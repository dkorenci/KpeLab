MAVEN="/home/dam1root/netbeans-7.3.1/java/maven/bin/mvn";

# Trove
#GROUP_ID="gnu.trove";
#ART_ID="gnu.trove.all";
#VERSION="3.0.3";
#LOCATION="/home/dam1root/software/trove-3.0.3/3.0.3/lib/trove-3.0.3.jar"

# rJava
GROUP_ID="rJava";
ART_ID="JRI.jar";
VERSION="0.5-0";
LOCATION="/home/dam1root/R/x86_64-pc-linux-gnu-library/3.0/rJava/jri/JRI.jar";

$MAVEN install:install-file -DgroupId=$GROUP_ID -DartifactId=$ART_ID \
 -Dpackaging=jar -Dversion=$VERSION -Dfile=$LOCATION

