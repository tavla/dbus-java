# dbus-java
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.hypfvieh/dbus-java/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.hypfvieh/dbus-java)
[![Javadoc](https://javadoc.io/badge2/com.github.hypfvieh/dbus-java/javadoc.svg)](https://javadoc.io/doc/com.github.hypfvieh/dbus-java)

Improved version of [Java-DBus library provided by freedesktop.org](https://dbus.freedesktop.org/doc/dbus-java/) with compatibility to Java 8.

Please note this version is not compatible with 2.7.x versions as classes have been moved in other packages or were completly removed.
Most import issues should be easily fixable by using 'Organize Imports'. 
Using this version as replacement for 2.7.x however, will not work without changing your code as well.

#### Changes

##### Changes in 3.32 (not yet released):
   - Fixed regression not allowing to use classes directly implementing `DBusInterface` to be exported on the bus ([#157](https://github.com/hypfvieh/dbus-java/issues/157))


##### Changes in 3.3.1 (Released: 2021-10-23):
   - Fixed some issues in InterfaceCodeGenerator related to signal constructors, thanks to [poeschel](https://github.com/poeschel) ([PR#146](https://github.com/hypfvieh/dbus-java/pull/146))
   - Fixed some issues in InterfaceCodeGenerator related to usage of Tuples, thanks to [poeschel](https://github.com/poeschel) ([PR#147](https://github.com/hypfvieh/dbus-java/pull/147))
   - Improved SASL authentication with domain sockets on OS X, thanks to [brett-smith](https://github.com/brett-smith) ([PR#148](https://github.com/hypfvieh/dbus-java/pull/148))
   - Fixed some issues related to marshalling/unmarshalling of Tuples, thanks to [poeschel](https://github.com/poeschel) ([PR#149](https://github.com/hypfvieh/dbus-java/pull/149))
   - Fixed various issues in Introspection generation introduced with [PR#143](https://github.com/hypfvieh/dbus-java/pull/143) and in marshalling caused by  [PR#149](https://github.com/hypfvieh/dbus-java/pull/149)
   - Fixed issue with interupted status of thread got lost after a synchronous method call was interrupted [#150](https://github.com/hypfvieh/dbus-java/issues/150)
   
##### Changes in 3.3.0 (Released: 2021-03-17):
  *CAUTION* - This version may contain breaking changes!!
  - Removed usage of java-utils
  - Moved Hexdump class from org.freedesktop to org.freedesktop.dbus.utils
  - Moved DBus interface from org.freedesktop to org.freedesktop.dbus.interfaces
  - Modulize dbus-java ([PR#125](https://github.com/hypfvieh/dbus-java/pull/125), thanks to [brett-smith](https://github.com/brett-smith))
  - DBusConnection.getConnection(DBusBusType _bustype) will no longer throw RuntimeException but DBusConnectionException if something went wrong establishing DBus connection [#128](https://github.com/hypfvieh/dbus-java/issues/128)
  - Support annotation to specify properties provided by an exported object (DBusProperty annotation) ([PR#131](https://github.com/hypfvieh/dbus-java/pull/131)/[#130](https://github.com/hypfvieh/dbus-java/issues/130)), thanks to [mk868](https://github.com/mk868)
  - Added support for the new DBusProperty annotations in InterfaceCodeGenerator Util ([PR#132](https://github.com/hypfvieh/dbus-java/pull/132)), thanks to [mk868](https://github.com/mk868)
  - License changed from LGPLv2 to MIT

##### Changes in 3.2.4:
  - Improved logging usage of Arrays.deepToString(Object[]), so this message will not be called in log statements if the loglevel is not enabled
  - Improved usage of regex and length checks [#119](https://github.com/hypfvieh/dbus-java/issues/119) & [PR#120](https://github.com/hypfvieh/dbus-java/pull/120) (thanks to [OlegAndreych](https://github.com/OlegAndreych))
  - Improvements in InterfaceCodeGenerator Util regarding creation of Struct classes ([#121](https://github.com/hypfvieh/dbus-java/issues/121))
  - Fixed possible race-conditions/dead-locks when disconnecting from DBus ([#123](https://github.com/hypfvieh/dbus-java/pull/123))
  
##### Changes in 3.2.3:
  - Fixed regression introduced with [#110](https://github.com/hypfvieh/dbus-java/pull/110) ([#114](https://github.com/hypfvieh/dbus-java/pull/114))

##### Changes in 3.2.2:
  - Fixed issue with introspection caused by changes in [#80](https://github.com/hypfvieh/dbus-java/issues/80) ([#103](https://github.com/hypfvieh/dbus-java/issues/103)), thanks to [AsamK](https://github.com/AsamK)
  - Added support for FreeBSD, ([#105](https://github.com/hypfvieh/dbus-java/pull/105)) thanks to [grembo](https://github.com/grembo)
  - Fixed SASL authentication may get stuck when using TCP ([#106](https://github.com/hypfvieh/dbus-java/pull/106)) thanks to [brett-smith](https://github.com/brett-smith)
 - Fixed issues when dealing with multiple signals of the same name but different signatures ([#110](https://github.com/hypfvieh/dbus-java/pull/110))
 - Dependency updates

##### Changes in 3.2.1
  - Fixed leaking signal handlers when using addSigHandler/removeSigHandler a lot ([#76](https://github.com/hypfvieh/dbus-java/issues/76))
  - Fixed unexported objects shown in introspection output ([#80](https://github.com/hypfvieh/dbus-java/issues/80))
  - Added support for providing alternative implementations of IMessageReader/IMessageWriter ([#81](https://github.com/hypfvieh/dbus-java/issues/81))
  - Removed support for file descriptor passing, as it is not working with jnr-unixsocket ([#81](https://github.com/hypfvieh/dbus-java/issues/81))
  - Fixed issue dealing with handled signals ([#97](https://github.com/hypfvieh/dbus-java/issues/97))
  - Fixes issue with InterfaceCodeGenerator util ([#95](https://github.com/hypfvieh/dbus-java/issues/95))
  - Fixes issue with InterfaceCodeGenerator util using incorrect type ([#83](https://github.com/hypfvieh/dbus-java/issues/83))

##### Changes in 3.2.0 
  - Replaced libmatthew with jnr-unixsocket
  - Removed all native library files and sources 
  - Added new transport system to use jnr-unixsocket (replaces old Transport class)
  - Added support for empty collections/arrays (thanks to [chris-melman](https://github.com/chris-melman))
  - Added support of DBUS_MACHINE_ID_LOCATION environment property to allow usage of dbus-java on Windows (thanks to [chris-melman](https://github.com/chris-melman))
  - Allow change of the default socket timeout (thanks to [chris-melman](https://github.com/chris-melman))
  - Detect system endianness for DBus message instead of using BIG endian all the time (#54)
  - Allow changing the default endianness (DBusConnection.setEndianness())
  - Providing OSGi ready artifact ([#33](https://github.com/hypfvieh/dbus-java/issues/32)) (thanks to [stack-head](https://github.com/stack-head))

##### Changes in 3.0.2
  - Added support for handling various DBus signals without the need to create a specific interface class (thanks to [rm5248](https://github.com/rm5248))
  - Fixed issue with List containing another List (thanks to [rm5248](https://github.com/rm5248))
  - Changed project to multi-module. All tools (except DBusDaemon/DBusEmbeddedDaemon) are now part of the sub-module dbus-java-utils.
    The dbus-java-utils sub-module is not required for dbus-java to work, it only contains standalone helper classes (like DBus interface class creation tool)
  - Logback dependency for dbus-java is now test, so exclusions for logback are no longer required when using a different logger implementation
  - Logback dependency for dbus-java-utils is runtime, as most of the helper classes need logging to show progress or issues. 
    If you don't want to use logback when using dbus-java-utils, please specify an exclusion rule and add another slf4j logger in your pom 
  - Added support sending/receiving FileDescriptor (UNIX_FD, DBus type 'h') (see [#42](https://githincludeub.com/hypfvieh/dbus-java/issues/42))

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
  - Thanks to [lbeuster](https://github.com/lbeuster) for improvements and Mac support
  - Thanks to [littlefreaky](https://github.com/littlefreaky) for various bug fixes and charset issue fixes
  - Thanks to [michivi](https://github.com/michivi) for adding support of dbus-machine-id file on different locations
  - Thanks to [sshort](https://github.com/sshort) for fixing some issues in old CreateInterface tool 
  - Thanks to [rm5248](https://github.com/rm5248) for fixing issues with Lists containing Lists and adding support of handling various signals on DBus
