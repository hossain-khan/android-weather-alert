# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [2.18.0] - 2026-07-25

### Changed
- Upgraded target SDK (`targetSdk`) to API level 37 (Android 17).
- Audited codebase against Android 17 (API 37) behavior changes, confirming compliance with BAL hardening, Certificate Transparency defaults, native code loading read-only checks, background audio/alarm foreground service rules, RFCOMM BluetoothSocket behavior, and Contacts Provider 2 SQL restrictions.
- Optimized WorkManager initialization in `AndroidManifest.xml` and resolved manifest merger warnings.
- Configured Kotlin compiler arguments (`-Xannotation-default-target=param-property`) for Kotlin 2.2+ constructor annotation compatibility.
- Cleaned up deprecated AGP configuration flags in `gradle.properties`.
