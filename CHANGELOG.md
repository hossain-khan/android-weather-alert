# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Changed
- Upgraded target SDK (`targetSdk`) to API level 37 (Android 17).
- Audited codebase against Android 17 (API 37) behavior changes, confirming compliance with BAL hardening, Certificate Transparency defaults, native code loading read-only checks, background audio/alarm foreground service rules, RFCOMM BluetoothSocket behavior, and Contacts Provider 2 SQL restrictions.
