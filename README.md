# Android Link Preview

Android Link Preview is a lightweight and easy-to-use library for generating link previews in Android applications. It extracts metadata such as title, description, and image from a provided URL and presents them in a formatted preview component.

## Features

* Extracts metadata including title, description, and image from any URL.
* Supports preview generation for webpages, videos, and profiles.
* Handles redirects and partial downloads using OkHttp.
* Ignores restricted platforms based on regional settings (e.g., platforms banned in India).

---

## Installation

### Step 1: Add JitPack repository

In your root `settings.gradle` file, add the JitPack repository:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url 'https://jitpack.io' }
    }
}
```

### Step 2: Add the library dependency

In your module-level `build.gradle` file, add the following dependency:

```gradle
dependencies {
    implementation 'com.github.vishalkumarsinghvi:Android-Link-Preview:1.1'
}
```

Replace `Tag` with the latest release tag from the [Releases](https://github.com/vishalkumarsinghvi/Android-Link-Preview/releases).

---

## Usage

### Basic Usage

To generate a link preview, simply pass the URL to the `LinkPreviewGenerator`:

```kotlin
import com.vishalkumarsinghvi.linkpreview.LinkPreviewGenerator
import com.vishalkumarsinghvi.linkpreview.model.LinkPreviewData

fun generateLinkPreview(url: String) {
    val previewData: LinkPreviewData? = LinkPreviewGenerator.generate(url)
    previewData?.let {
        println("Title: ${it.title}")
        println("Description: ${it.description}")
        println("Image URL: ${it.imageUrl}")
    }
}
```

### Advanced Usage

* Customize request headers
* Handle restricted platforms
* Optimize metadata extraction

Refer to the [Wiki](https://github.com/vishalkumarsinghvi/Android-Link-Preview/wiki) for advanced usage and configuration.

---

## License

This library is licensed under the MIT License. See the [LICENSE](LICENSE) file for more information.
