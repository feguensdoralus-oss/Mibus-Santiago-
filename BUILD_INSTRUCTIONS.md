# MiBus Santiago - Build Instructions

## Prerequisites

- **Android Studio** 2024.1+ or Gradle CLI
- **JDK 11+** (temurin recommended)
- **Gradle 9.3.1** (included via wrapper)
- **Keystore file** for release signing (optional for debug builds)

## Quick Start: Debug Build

### 1. Clone the Repository
```bash
git clone https://github.com/feguensdoralus-oss/Mibus-Santiago-.git
cd Mibus-Santiago-
```

### 2. Build Debug APK
```bash
chmod +x gradlew
./gradlew assembleDebug
```

**Output:** `app/build/outputs/apk/debug/app-debug.apk`

### 3. Install on Device/Emulator
```bash
./gradlew installDebug
```

---

## Release Build (Signed APK)

### 1. Set Up Signing Credentials

Create a `.env` file in the project root:

```bash
cp .env.example .env
```

Then edit `.env` with your keystore details:
```properties
KEYSTORE_PATH=./keystore/mibussantiago-release.jks
STORE_PASSWORD=your_keystore_password
KEY_PASSWORD=your_key_password
```

**Alternative:** Use environment variables directly
```bash
export KEYSTORE_PATH="./keystore/mibussantiago-release.jks"
export STORE_PASSWORD="your_password"
export KEY_PASSWORD="your_password"
```

### 2. Build Release APK
```bash
./gradlew assembleRelease
```

**Output:** `app/build/outputs/apk/release/app-release.apk`

---

## Firebase Configuration

### 1. Download google-services.json

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Project Settings → Your apps → Android**
4. Download `google-services.json`

### 2. Place in Project
```bash
cp google-services.json app/
```

The build system will automatically apply Firebase configuration.

---

## Build Variants

### Debug Build (Development)
```bash
./gradlew assembleDebug
```
- **Signing:** Built-in Android debug keystore
- **Optimization:** Disabled (faster builds)
- **Debuggable:** Yes

### Release Build (Production)
```bash
./gradlew assembleRelease
```
- **Signing:** Your keystore (via environment variables)
- **Optimization:** R8 minification + resource shrinking enabled
- **Debuggable:** No
- **ProGuard Rules:** Applied from `app/proguard-rules.pro`

---

## GitHub Actions CI/CD

### Set Up GitHub Secrets

1. Go to **Settings → Secrets and variables → Actions**
2. Add these secrets:
   - `KEYSTORE_PATH` – Path to your keystore (e.g., `./keystore/mibussantiago-release.jks`)
   - `STORE_PASSWORD` – Keystore password
   - `KEY_PASSWORD` – Key password

### Workflow Trigger

The build workflow runs automatically on:
- Push to `main` branch
- Pull requests to `main`
- Manual trigger via **Actions tab**

---

## Troubleshooting

### Build Fails: `gradle.properties` Missing
**Solution:** Create `gradle.properties` (already configured to ignore passwords)
```bash
touch gradle.properties
```

### Error: "Missing google-services.json"
**Solution:** Download from Firebase Console and place in `app/` directory
- Build will continue with warning (specified in `build.gradle.kts`)

### Signing Config Error
**Solution:** Verify environment variables are set:
```bash
echo $KEYSTORE_PATH
echo $STORE_PASSWORD
echo $KEY_PASSWORD
```

### Out of Memory During Build
**Solution:** Increase Gradle heap size in `gradle.properties`:
```properties
org.gradle.jvmargs=-Xmx4g -Dfile.encoding=UTF-8
```

### Clean Build
```bash
./gradlew clean assembleDebug
```

---

## APK Size & Optimization

- **Debug APK:** ~50-100 MB (with debugging symbols)
- **Release APK:** ~25-40 MB (minified + optimized)

Optimization features enabled:
- ✅ R8 code shrinking & obfuscation
- ✅ Resource pruning (removes unused drawables, strings, etc.)
- ✅ ProGuard rules for Firebase, Retrofit, OkHttp
- ✅ Language filtering (es, en only)

---

## Next Steps

1. ✅ Build debug APK and test locally
2. ✅ Configure Firebase Console
3. ✅ Set up keystore & signing credentials
4. ✅ Build release APK
5. ✅ Distribute via Firebase App Distribution or Google Play

For more info, see the official [Android Build Documentation](https://developer.android.com/build).
