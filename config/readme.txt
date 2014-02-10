** konfiguracija

U folderu config nalazi se readme.txt

Za Javu toplo preporučam Netbeans, zadnju verziju, Jave SE ili Java EE bundle.
Dolazi sa podrškom za Maven, prepoznat će folder kao projekt u File->Open Project.

Projekt je definiran datotekom pom.xml

Maven će automatski skinuti sve librarije iz pom.xml koji se nalaze u centralnom repozitoriju, 
sve osim rJava (Java-R sučelje) i GNU Trove (brze strukture podataka).
Moraju se ručno skinuti fileovi:
JRI.jar sa: http://rforge.net/JRI/files/
Trove sa: https://bitbucket.org/robeden/trove/downloads
Jar fajlovi se dodaju u lokalni Maven repozitorij pomoću skripte maven_install_local_jar.sh
Najbrže je koristiti iste vrijednosti za groupId i artifactId koje već pišu u pom.xml


Konfiguracija vanjskih resursa:


** folderi

- config - konfiguracijske i help datoteke
- resources - datasets, drugi resursi
- data_analysis - skripte i exportani podaci za analizu
- bugs - evidencija o bagovima
- src - kod

** logika koda

