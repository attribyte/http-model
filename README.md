##About

An HTTP model that presents an immutable interface and allows client implementations/models to be easily swapped.

##Build

The build uses [Apache Ant](http://ant.apache.org/) and
[Apache Ivy](https://ant.apache.org/ivy/) to resolve dependencies. The following ant tasks
are available:

* compile - Compiles the source
* dist - Resolves dependencies, compiles the source, and creates a jar in dist/lib. This is the default task.
* full-dist - Resolves dependencies, compiles the source, creates a jar in dist/lib, and copies dependencies to dist/extlib
* clean - Removes all build files and jars.
 
##Dependencies


* [Attribyte shared-base](https://github.com/attribyte/shared-base)
* [Apache commons-codec](http://commons.apache.org/proper/commons-codec/)
* [Google Guava](https://code.google.com/p/guava-libraries/)
* Various client implementations. TODO: Allow selective inclusion/exclusion of these.

##Documentation

* [Javadoc](https://www.attribyte.org/projects/http-model/javadoc/index.html)
 
##License

Copyright 2014 [Attribyte, LLC](https://attribyte.com)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and limitations under the License.
