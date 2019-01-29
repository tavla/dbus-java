# dbus-java [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hypfvieh/dbus-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hypfvieh/dbus-java)
Improved version of [Java-DBus library provided by freedesktop.org](https://dbus.freedesktop.org/doc/dbus-java/) with compatibility to Java 8.

Please note this version is not compatible with 2.7.x versions as classes have been moved in other packages or were completly removed.
Most import issues should be easily fixable by using 'Organize Imports'. 
Using this version as replacement for 2.7.x however, will not work without changing your code as well.

#### Changes

##### Changes in 3.0.1
  - New tool (org.freedesktop.dbus.utils.generator.InterfaceCodeGenerator) to create apropriate java classes/interfaces from introspection XML (beta, will replace org.freedesktop.dbus.bin.CreateInterface)
  - Some smaller fixes for old interface creation tool (org.freedesktop.dbus.bin.CreateInterface) (see [#34](https://github.com/hypfvieh/dbus-java/issues/34),[#35](https://github.com/hypfvieh/dbus-java/issues/35),[#36](https://github.com/hypfvieh/dbus-java/issues/36), thanks to [sshort](https://github.com/sshort))
  - Added support for reading dbus machine-id from /etc (thanks to [michivi](https://github.com/michivi))
  - Fixed some issues with different Locale settings (thanks to [littlefreaky](https://github.com/littlefreaky))
  - Fixed marshalling issue ([#21](https://github.com/hypfvieh/dbus-java/issues/21)/[#26](https://github.com/hypfvieh/dbus-java/issues/26), (thanks to [littlefreaky](https://github.com/littlefreaky)))

##### Changes in 3.0
  - Requires Java 8
  - Refactored AbstractConnection and all classes depending on it
  - Use ThreadPoolExecutor instead of HashMap of Threads
  - Use ConcurrentHashMap and friends where possible
  - Removed lots of synchronized blocks (when not required)
  - Better encapsulation in AbstractConnection
  - Use BlockingQueue instead of home grown solution
  - Removed EfficientMap and EfficientList, as they are not really efficient or better than the JDK provided Maps/Lists
  - Merged UnixSocket stuff of libmatthew to this library, native library libunix-java.so is still compatible with older libmatthew;
    Main reason is, that the unix socket library of libmatthew is very DBUS specific 
    (like providing special features for SASL/DBUS\_COOKIE\_SHA)

#### Older changes

  - Fixed lots of Java warnings (Generics, unclosed resources)
  - Removed broken 'Gettext' feature used for exception messages, english exception messages should be good enough
  - Renamed some classes/methods/variables to comply with Java naming scheme
  - Removed proprietary logger and replaced it with slf4j
  - Renamed/refactored some parts to be more 'Java' like (e.g. naming, shadowing)
  - Fixed problems with DbusConnection.getConnection(SESSION) when using display export (e.g. SSH X11 forward)
  
##### Changes by others:
  - Thanks to [thjomnx](https://github.com/thjomnx) for support of PropertiesChanged signal class
  - Thanks to [RafalSumislawski](https://github.com/RafalSumislawski) for support of org.freedesktop.DBus.ObjectManager
  - Thanks to [lbeuster](https://github.com/lbeuster) for improvments and Mac support
