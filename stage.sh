#!/bin/sh
VERSION="0.5.3"
cp attribyte-http-0.5.pom dist/lib/attribyte-http-${VERSION}.pom
cd dist/lib
gpg -ab attribyte-http-${VERSION}.pom
gpg -ab attribyte-http-${VERSION}.jar
gpg -ab attribyte-http-${VERSION}-sources.jar
gpg -ab attribyte-http-${VERSION}-javadoc.jar
jar -cvf ../bundle.jar *

