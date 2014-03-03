** konfiguracija

Najbolje je koristiti Netbeans, zadnju verziju, Jave SE ili Java EE bundle.
Dolazi sa podrškom za Maven, prepoznat će folder kao projekt (File->Open Project).

Projekt je definiran datotekom pom.xml

Maven će automatski skinuti sve librarije iz pom.xml koji se nalaze u centralnom repozitoriju, 
sve osim rJava (Java-R sučelje) i GNU Trove.

Jar fajlovi se dodaju u lokalni Maven repozitorij pomoću skripte maven_install_local_jar.sh
tako da se promjene vrijednosti odgovarajućih varijabli u skripti nakon čega se skripta pokrene, 
u repozitoriju je samo šablona: maven_install_local_jar.sh.example, 
sama skripta nije da ne dođe do sukoba.
Najbrže je koristiti iste vrijednosti za groupId i artifactId koje već pišu u pom.xml, 
te vrijednosti se već nalaze u skripti.

Gnu Trove konfiguracija:
skinuti sa: https://bitbucket.org/robeden/trove/downloads
dodati pomoću maven_install_local_jar.sh

rJava konfiguracija:
Instalirati R i rJava paket R naredbom install.packages("rJava")
Postaviti varijablu R_HOME (npr. u /etc/environment se doda linija R_HOME="path" + 
log-in log-out za enforcanje promjene) 
na vrijednost koja se dobiva izvršavanjem R naredbe Sys.getenv("R_HOME")
Dodati putanju od JRI.jar ("locate JRI.jar") u Maven lokalni repozitorij 
pomoću skripte maven_install_local_jar.sh.
Postaviti java path varijablu na *folder* u kojem je libjri.so ("locate libjri.so"):
u NetBeansu, desni klik na projekt -> properties -> run, pod VM options nadodati
-Djava.library.path="putanja"

Konfiguracija vanjskih resursa:
Napraviti tekstnu datoteku config/kpelab.properties, 
config/kpelab.properties mora biti u radnom direktoriju pokrenutog programa, 
ako se pokreće iz NetBeans-a, biti će.
Datoteka je u .gitignore da ne bi bilo sukoba.
Primjer je u config/kpelab.properties.example

Za prvu ruku su najbtniji "cache.folder", datasetovi i wikiesa.*
Za test i kreiranje keširanih podataka najbolje je pokrenti SimilarityExperiments.expWS353ESA();

** folderi

- config - konfiguracijske i help datoteke
- resources
- data_analysis - skripte i exportani podaci za analizu
- bugs - evidencija o bagovima
- src - kod

