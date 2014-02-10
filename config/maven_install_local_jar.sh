MAVEN="/home/dam1root/netbeans-7.3.1/java/maven/bin/mvn";
GROUP_ID="gnu.trove";
ART_ID="gnu.trove.all";
VERSION="3.0.3";
LOCATION="/home/dam1root/software/trove-3.0.3/3.0.3/lib/trove-3.0.3.jar"
$MAVEN install:install-file -DgroupId=$GROUP_ID -DartifactId=$ART_ID \
 -Dpackaging=jar -Dversion=$VERSION -Dfile=$LOCATION
