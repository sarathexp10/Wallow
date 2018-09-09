## Wallow
Wallpapers for nerds

## Changelog
[CHANGELOG.md](/CHANGELOG.md)

## Building from source
#### Requireiments
- Android Studio 3.2RC02+ with Kotlin Plugin installed
- A Firebase Project (needed for the google-services.json file)
- GitHub OAuth Client ID and Client Secret placed in app/credentials.gradle file)
- AdMob Account + App (place App ID and Banner ID in app/credentials.gradle file)
- Basic knowledge of Android and Kotlin

#### Building
You can directly build the apk from Android Studio or run the following command in terminal

- Windows (cmd)
```cmd
gradlew assembleDebug
```

- Linux/Mac (Terminal)
```cmd
./gradlew assembleDebug
```

