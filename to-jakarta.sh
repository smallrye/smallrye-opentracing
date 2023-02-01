#!/usr/bin/env bash

# move to jakarta parent
find . -type f -name 'pom.xml' -exec sed -i '' 's/smallrye-parent/smallrye-jakarta-parent/g' {} +
# java sources
find . -type f -name '*.java' -exec sed -i '' 's/javax./jakarta./g' {} +
# service loader files
find . -path "*/src/main/resources/META-INF/services/javax*" | sed -e 'p;s/javax/jakarta/g' | xargs -n2 git mv

mvn build-helper:parse-version versions:set -DnewVersion=\${parsedVersion.nextMajorVersion}.0.0-SNAPSHOT

mvn -ntp versions:set-property -Dproperty=version.microprofile.opentracing -DnewVersion=3.0 -N
mvn -ntp versions:set-property -Dproperty=version.microprofile.config -DnewVersion=3.0 -N
mvn -ntp versions:set-property -Dproperty=version.microprofile.restclient -DnewVersion=3.0 -N
mvn -ntp versions:set-property -Dproperty=version.jakarta.servlet -DnewVersion=5.0.0 -N

mvn -ntp versions:set-property -Dproperty=version.smallrye.testing -DnewVersion=2.2.0
mvn -ntp versions:set-property -Dproperty=version.smallrye.config -DnewVersion=3.1.1
mvn -ntp versions:set-property -Dproperty=version.resteasy -DnewVersion=6.0.3.Final
mvn -ntp versions:set-property -Dproperty=groupId.resteasy.client -DnewVersion=org.jboss.resteasy.microprofile
mvn -ntp versions:set-property -Dproperty=artifactId.resteasy.client -DnewVersion=microprofile-rest-client
mvn -ntp versions:set-property -Dproperty=version.resteasy.client -DnewVersion=2.0.0.Final
