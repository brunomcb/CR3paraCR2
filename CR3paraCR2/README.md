# RAW Rename - App icon patch

1. Copy the `app/src/main/res` folder from this patch over the same folder in the Android project.
2. In `app/src/main/AndroidManifest.xml`, add these attributes to `<application>`:

```xml
android:icon="@mipmap/ic_launcher"
android:roundIcon="@mipmap/ic_launcher_round"
```

Example:

```xml
<application
    android:name=".RawRenameApp"
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:label="RAW Rename"
    android:theme="@style/AppTheme">
```

The patch includes legacy icons for mdpi through xxxhdpi plus Android 8+ adaptive icon resources.
