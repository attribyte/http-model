#!/bin/sh
VERSION="0.5.3"
cp pom.xml target/attribyte-http-${VERSION}.pom
cd target
gpg -ab attribyte-http-${VERSION}.pom
gpg -ab attribyte-http-${VERSION}.jar
gpg -ab attribyte-http-${VERSION}-sources.jar
gpg -ab attribyte-http-${VERSION}-javadoc.jar
jar -cvf ../bundle.jar attribyte-http-${VERSION}*

