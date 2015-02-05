#!/bin/sh
cp attribyte-http-0.5.0.pom dist/lib
cd dist/lib
gpg -ab attribyte-http-0.5.0.pom
gpg -ab attribyte-http-0.5.0.jar
gpg -ab attribyte-http-0.5.0-sources.jar
gpg -ab attribyte-http-0.5.0-javadoc.jar
jar -cvf ../bundle.jar *

