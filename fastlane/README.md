# F-Droid Fastlane Metadata

This directory contains metadata for F-Droid app store submission following the [Fastlane format](https://docs.fastlane.tools/actions/supply/) that F-Droid supports.

## Structure

```
fastlane/metadata/android/
└── en-US/
    ├── title.txt                 # App title (30 chars max)
    ├── short_description.txt     # Brief description (80 chars max)  
    ├── full_description.txt      # Detailed description (4000 chars max)
    ├── changelogs/
    │   └── 18.txt               # Changelog for version code 18
    └── images/
        └── phoneScreenshots/     # App screenshots (minimum 2, maximum 8)
            ├── 1.png
            ├── 2.png
            └── 3.png
```

## Usage

This metadata is used by F-Droid to populate the app listing. The F-Droid build system can optionally use this metadata during the app publication process.

## Guidelines

- **Title**: Keep concise and descriptive
- **Short Description**: Single sentence summary
- **Full Description**: Use basic formatting, avoid excessive markup
- **Screenshots**: Use phone screenshots in portrait orientation
- **Changelogs**: Keep focused on user-facing changes

## References

- [F-Droid App Metadata](https://f-droid.org/docs/Build_Metadata_Reference/)
- [Fastlane Supply Documentation](https://docs.fastlane.tools/actions/supply/)
- [Triple-T Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)