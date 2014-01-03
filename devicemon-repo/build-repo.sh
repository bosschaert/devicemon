java -jar ./bin/org.osgi.impl.bundle.bindex.jar -r repository.xml local-repo/*.jar

# for var in "$@"
# do
#    echo $var
#    fname="${var##*/}"
#    echo $fname
#    bsn="${fname%-*}"
#    echo $bsn
#    suffix="${fname##*-}"
#    version="${suffix%%.jar}"
#    echo $version
#
#    mvn org.apache.felix:maven-obr-plugin:1.2.0:install-file -Dfile=$var -DgroupId=org.coderthoughs.obr -DartifactId=$bsn -Dversion=$version -Dpackaging=jar -DobrRepository=./repository.xml
#done
