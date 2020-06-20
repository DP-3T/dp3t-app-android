# DP3T Android Demo App

[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://github.com/DP-3T/dp3t-app-android/blob/master/LICENSE)
![Android Build](https://github.com/DP-3T/dp3t-app-android/workflows/Android%20Build/badge.svg)


## DP3T
The Decentralised Privacy-Preserving Proximity Tracing (DP-3T) project is an open protocol for COVID-19 proximity tracing using Bluetooth Low Energy functionality
on mobile devices that ensures personal data and computation stays entirely on an individual's phone. It was produced by a core team of over 25 scientists
and academic researchers from across Europe. It has also been scrutinized and improved by the wider community.

DP-3T is a free-standing effort started at EPFL and ETHZ that produced this protocol and that is implementing it in an open-sourced app and server.

## Introduction

This is a COVID-19 tracing demo client using the [DP3T Android SDK](https://github.com/DP-3T/dp3t-sdk-android). It shows how the SDK can be used in a real, but simplified
app. For a more complete solution, check the [DP3T Android App Switzerland](https://github.com/DP-3T/dp3t-app-android-ch), which also contains certificate pinning and other
additional security features.

## Contribution Guide

The DP3T App is not yet complete. It has not yet been reviewed or audited for security and compatibility. We are both continuing the development and have started a
security review. This project is truly open-source and we welcome any feedback on the code regarding both the implementation and security aspects.

Bugs or potential problems should be reported using Github issues. We welcome all pull requests that improve the quality the source code or the demo app itself.

## Repositories
* Android SDK & Calibration app: [dp3t-sdk-android](https://github.com/DP-3T/dp3t-sdk-android)
* iOS SDK & Calibration app: [dp3t-sdk-ios](https://github.com/DP-3T/dp3t-sdk-ios)
* Android Demo App: [dp3t-app-android](https://github.com/DP-3T/dp3t-app-android)
* Android App Switzerland: [dp3t-app-android-ch](https://github.com/DP-3T/dp3t-app-android-ch)
* iOS Demo App: [dp3t-app-ios](https://github.com/DP-3T/dp3t-app-ios)
* iOS App Switzerland: [dp3t-app-ios-ch](https://github.com/DP-3T/dp3t-app-ios-ch)
* Backend SDK: [dp3t-sdk-backend](https://github.com/DP-3T/dp3t-sdk-backend)

## Further Documentation
The full set of documents for DP3T is at https://github.com/DP-3T/documents. Please refer to the technical documents and whitepapers for a description
of the implementation.

## Installation and Building

The project can be opened with Android Studio 3.6.1 or later or you can build the project with Gradle using
```sh
$ ./gradlew assembleDevRelease
```
The APK is generated under app/build/outputs/apk/prod/release/package-prod-release.apk

## License
This project is licensed under the terms of the MPL 2 license. See the [LICENSE](LICENSE) file.
