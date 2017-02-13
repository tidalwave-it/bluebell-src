![Maven Central](https://img.shields.io/maven-central/v/it.tidalwave.bluebell/bluebell.svg)
[![Build Status](https://img.shields.io/jenkins/s/http/services.tidalwave.it/ci/job/_Build_from_Scratch.svg)](http://services.tidalwave.it/ci/view/)
[![Test Status](https://img.shields.io/jenkins/t/http/services.tidalwave.it/ci/job/.svg)](http://services.tidalwave.it/ci/view/)
[![Coverage](https://img.shields.io/jenkins/c/http/services.tidalwave.it/ci/job/.svg)](http://services.tidalwave.it/ci/view/)

blueBell (master)
================================

[![Build Status](https://drone.io/bitbucket.org/tidalwave/tidalwave-superpom-src/status.png)](https://drone.io/bitbucket.org/tidalwave/tidalwave-superpom-src/latest)

The SuperPOM for all Tidalwave projects. It is not designed for being used by others, as it contains some corporate-specific
configurations, but its ancestor [TheseFooolishThings SuperPOM](http://bitbucket.org/tidalwave/thesefoolishthings-superpom-src) has been designed to be reusable.
Please have a look at it.


Bootstrapping
-------------

In order to build the project, run from the command line:

```mvn -DskipTests```

The project can be opened and built by a recent version of the NetBeans, Eclipse or Idea IDEs.


Documentation
-------------

More information can be found on the [homepage](http://tidalwave.tidalwave.it/bluebell) of the project.


Where can I get the latest release?
-----------------------------------

You can download source and binaries from the [download page](${scm.repo.browse.url}/bluebell).

Alternatively you can pull it from the central Maven repositories:

```xml
<dependency>
    <groupId>it.tidalwave.bluebell<groupId>
    <artifactId>bluebell</artifactId>
    <version>-- version --</version>
</dependency>
```


Contributing
------------

We accept pull requests via Bitbucket or GitHub.

There are some guidelines which will make applying pull requests easier for us:

* No tabs! Please use spaces for indentation.
* Respect the code style.
* Create minimal diffs - disable on save actions like reformat source code or organize imports. If you feel the source
  ode should be reformatted create a separate PR for this change.
* Provide TestNG tests for your changes and make sure your changes don't break any existing tests by running
```mvn clean test```.

If you plan to contribute on a regular basis, please consider filing a contributor license agreement. Contact us for
 more information


License
-------

Code is released under the [Apache Licence v2](https://www.apache.org/licenses/LICENSE-2.0.txt).


Additional Resources
--------------------

* [Tidalwave Homepage](http://tidalwave.it)
* [Project Issue Tracker (Jira)](http://services.tidalwave.it/jira/browse/${tidalwave.issues})
* [Project Continuous Integration (Jenkins)](http://services.tidalwave.it/ci/view/)
