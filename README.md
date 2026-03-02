# mapton
Some kind of map application

![alt tag](https://mapton.org/files/screenshots/appstream01.png)

![alt tag](https://mapton.org/files/screenshots/appstream02.png)

# Building Mapton
## JDK setup
Building mapton requires a JDK >= 25 with bundled JavaFX modules.
Azul provides their Zulu https://www.azul.com/downloads/zulu-community/ and BellSoft their Liberica https://bell-sw.com/pages/downloads/.

Both can also be installed with https://sdkman.io/.
## Dependencies
Clone and build the following projects in order, before building mapton.

- https://github.com/trixon/netbeans-platform
- https://github.com/trixon/trv-traffic-information
- https://github.com/trixon/almond
- (optional) https://github.com/trixon/quakeml4j
- (optional) https://github.com/trixon/quake
- https://github.com/trixon/netbeans-plugins
- https://github.com/WorldWindEarth/WorldWindJava

Install the generated artifact in the last step with

`mvn install:install-file -Dfile=./build/libs/worldwind-2.3.1.jar -DgroupId=earth.worldwind -DartifactId=wwj-ce -Dversion=2.3.1 -Dpackaging=jar`

## Build Mapton
