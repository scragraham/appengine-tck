Google App Engine TCK
=====================

License
-------

The content of this repository is released under the <PUT LICENCE TYPE HERE>
as provided in the LICENSE file that accompanied this code.

By submitting a "pull request" or otherwise contributing to this repository, you
agree to license your contribution under the license mentioned above.

License terms for 3rd Party Works
---------------------------------

This software uses a number of other works, the license terms of which are
documented in the NOTICE file that accompanied this code.

Directory structure
-------------------

* `common`      - common test code; base tests and multisuite support
* `env`         - custom environment hooks; GAE SDK, Appspot, CapeDwarf, AppScale, ...
* `ext`         - external (useful) tests; e.g. DataNucleus, MapReduce, Objectify, ...
* `tests`       - the main TCK API tests
* `utils`       - helper utils; code coverage plugin, bytecode transforemers

Requirements
------------

Java JDK6+ and Maven 3.x+.
And of course the environment you want to test.

Running the build
-----------------

The whole project is fully Mavenized, so for the simple build the process is obvious:

    mvn clean install

This builds the default profiles, which includes compiling the tests, but not running them.
And we also output the API code coverage metrics; see console output.

Building the tests
------------------

All tests are build using [Arquillian](http://www.arquillian.org) and [ShrinkWrap](http://www.jboss.org/shrinkwrap).
Each different environment has a custom Arquillian container implementation.

GAE container implementations can be found here: https://github.com/alesj/arquillian-container-gae
CapeDwarf uses plain JBossAS Arquillian remote container implementation.

GAE API code coverage
---------------------

For each set of tests we want to get code coverage, needs a coverage.txt file,
where we list all the classes / interfaces whose usage we track.

e.g.

# TaskQueue code covereage
com.google.appengine.api.taskqueue.Queue

This will print out all Queue interface usage in our TaskQueue tests.

You can either see the results in console while the build is running,
or at the end open index.html file in TCK root.

Running the tests
-----------------

Running the tests is same as building the project,
but you also need to specify the environment you want to test.

This are the current enviroments:

1) GAE SKD

    mvn clean install -Psdk -Dappengine.sdk.root=<PATH_TO_SDK>

2) GAE Appspot

    mvn clean install -Pappspot -Dappengine.sdk.root=<PATH_TO_SDK> -Dappengine.userId=<USER_ID> -appengine.password=<USER_PASSWORD>

And you need to make sure that the app-id in appengine-web.xml file is the one pointing to your appspot instance.

3) JBoss CapeDwarf

    mvn clean install -Pcapedwarf

Where you need a running instance of CapeDwarf environment to be present.

Deploying all tests at once
---------------------------

We learned how to run the tests.
But that will deploy one test class per deployment,
which can be time consuming for enviroments like Appspot.

In order to handle all tests within one deployment, we added `multisuite` profile.

    mvn clean install -Psdk,multisuite -Dappengine.sdk.root=<PATH_TO_SDK>

This will deploy all tests (per API) in a single .war file, and then test them as usual.

Note: as we create an uber .war from all tests, not all tests can be included.
Tests that are not capable of running inside uber .war, *must* be marked with @IgnoreMultisuite.

Each set of tests that we want to run -Pmultisuite on them, requires a multisuite.marker file in tests' root directory.
In this marker we can override test class regexp patter or scanning strategy; see code for more details.

Running a single test
---------------------

Since it's all Mavenized, this is trivial, as Surefire plugin already supports this.

    mvn clean install -Psdk -Dappengine.sdk.root=<PATH_TO_SDK> -Dtest=<TEST_SIMPLE_NAME>

Adapting the environment wrt tests
----------------------------------

During the tests we can fire TestLifecycle events.
Each enviroment can add (via ServiceLoader pattern) multiple TestLifecycle implementations; e.g. one per particular event type.

e.g.

    public class CapeDwarfTestContextEnhancer implements TestLifecycle {
        public void before(TestLifecycleEvent event) {
            if (event instanceof TestContextLifecycleEvent) {
                TestContextLifecycleEvent tcle = (TestContextLifecycleEvent) event;
                enhance(tcle.getTestContext());
            }
        }

External tests
--------------

Here we try to gather any useful tests that will help make GAE API implementation environments better.

Current external tests:

* GAE DataNucleus Plugin
* GAE MapReduce Library

As we don't want to overload the testing, each custom external set of tests should be under unique profile.

e.g. in the case of DataNucleus GAE plugin we use -Pdatanucleus

    mvn clean install -Pcapedwarf,datanucleus

This will run all DataNucleus tests against CapeDwarf environment.

e.g. running MapReduce tests against SDK

    mvn clean install -Psdk,mapreduce -Dappengine.sdk.root=<PATH_TO_SDK>

Note: adapting the tests to run against real environment can sometime be a huge (bytecode) hack. :-)

Writing the test
----------------

As we already mentioned, all tests are Arquillian and ShrinkWrap based,
with JUnit being the actual test framework used, and Maven Surefire plugin to run it all.

But there are a few guidelines we need to follow, specially with regard to "multisuite".

Few basic Arquillian rules:
* each test class needs to have @RunWith(Arquillian.class) in its class hierarchy
* there must be exactly one (in whole class hierarchy) static method marked with @Deployment, returning WebArchive instance

Few basic ShrinkWrap rules:
 * do not forget to add base test classes to deployment, only actual test class is added by default
 * make sure .war's META-INF is in WEB-INF/classes/META-INF, as per web spec

Few basic "multisuite" rules:
* we merge all WebArchive instances from all tests into one single uber WebArchive instance
* luckily there is a notification filter, which spits out warning by default if resources overlap
* notifications can be changed to failure if needed, for more strict resource usage in tests
* test classes can be excluded from "multisuite" with @IgnoreMultisuite

Why all this fuss about "multisuite"?
Imagine two different web.xml files, where one test needs servlet A, and the other one needs servlet B.
In multisuite, there is only one web.xml - first come, first serve - so one test could miss its servlet.
Which means we need to carefully craft test recources.
This mostly results in more middle / abstract classes, holding common resources.

To make things a bit easier, we already introduced a few abstract / helper classes and methods.
* each test class should extend TestBase class
* to get WebArchive instance use TestBase::getTckDeployment with proper TestContext instance
* if any custom behavior needs to be added per test environment, add this via TestLifecycles::before or TestLifecycles::after
* LibUtils class can be used to grab whole libraries / jars from Maven and attach them to .war as library (WEB-INF/lib)
* one can add its own multisuite ScanStrategy and NotificationFilter; see ScanMultiProvider class for more details
