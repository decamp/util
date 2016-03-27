### BITS Utility Library
Various utilities. Primarily a personal toolkit for code that used in many other projects.

There is unfortunately a lot of overlap between this library and Google Guava. 

This library contains:
- a bunch of static convenience methods
- some reflection utilities
- a reference counting package
- an sync/async event bus
- very basic tools for dealing with runtime platform

### Build:
gradle  
util-common - Contains code for all archs
util-standard - Code for standard Java Runtime Environment only.
util-android - Code for Android Runtime Environtment only.


### Runtime:
After build, add all jars in **lib** and **target** directories to your project.

---
Author: Philip DeCamp

