g3m-AR-Engine
=============

##AR engine based on geolocation

###Content

The repository contents the following folders:

* g3m
* Oculus demos
* prototype
* EIFER App

The “g3m” folder has a version of Glob3Mobile engine (purgatory branch, 10.nov.2016). 
The “Oculus demos” has two small demo projects which use Glob3Mobile engine and the Oculus mobile SDK to develop geolocation-based AR and VR applications for Oculus Gear VR.
The "EIFER App" folder contains the XCode iOS project implementing such application.
The "prototype" folder contains a demo project which use Glob3Mobile engine to develop (Glasses) applications.

### Demos

#### VrFlight

This example is intended as an interactive walk through a 65km-long pathway which crosses Gran Canaria island from south to north. The user will see 3D scenarios representing the environment of the places the footwalk traverses. It is also possible to flight over the island in any direction and speed by dragging the Oculus Gear Vr trackbar with a finger.

![vrflight screenshot](/Oculus demos/screenshots/OculusVrFlight.png?raw=true)

The Glob3Mobile engine is used to generate the 3D scenarios and represent some symbology which informs the user about the places he are visiting during his virtual walk. 

#### VrCamera

This example has been desinged as an AR-app. The user will see the real place in which he is standing, so he can walk while using the application. If the user visits an interesting place, the app will show him extra content about it over the image.

![vrflight screenshot](/Oculus demos/screenshots/OculusVrCamera.png?raw=true)

The Glob3Mobile engine is used to draw that geolocated extra content over the camera images.

#### EIFER App

This iOS application prototypes the usage of 3D maps, Virtual Reality and Augmented Reality as a mean to explore urban environments. Such city models are represented on an standarized format, CityGML, which is thereupon enriched with different data sets containing energy per-building parameters. Ultimately, the application displays temporal data-series that are the result of numerical simulations regarding city shadow-casting.

![EIFER App Map Mode screenshot](/Oculus demos/screenshots/EIFER_MAP.png?raw=true)
![EIFER App VR Mono Mode screenshot](/Oculus demos/screenshots/EIFER_VRMONO.png?raw=true)
![EIFER App VR Stereo Mode screenshot](/Oculus demos/screenshots/EIFER_VRSTEREO.png?raw=true)
![EIFER App AR Mode screenshot](/Oculus demos/screenshots/EIFER_AR.png?raw=true)

The Glob3Mobile engine has been used throughout the application, not only to implement the abovementioned visualization modes, but also to perform the tessellation tasks and the procedural coloring and illumination of the city models.

### Oculus demos installation.

It requires [Oculus Mobile SDK 1.0.0.0](https://developer3.oculus.com/downloads/mobile/1.0.0.0/Oculus_Mobile_SDK/) to work.
Here is a [installation tutorial](https://github.com/glob3mobile/retos/wiki/Configuring-a-Oculus-project-with-G3M-submodule-in-Android-Studio) which allows you not only to install these demos but also to create new Oculus-G3M demos.
