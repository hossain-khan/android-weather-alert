# Android - Circuit App Template
An Android App template that is preconfigured with Circuit UDF architecture.

> [!TIP]
> Google has an official template available. Have you checked it out yet?  
> https://github.com/android/architecture-templates

## What do you get in this template? 📜
* ✔️ [Circuit](https://github.com/slackhq/circuit) library setup for the app
* ✔️ Dependency Injection for all Circuit Screens & Presenter combo
* ✔️ GitHub Actions for CI
* ✔️ [Google font](https://github.com/hossain-khan/android-compose-app-template/blob/main/app/src/main/java/app/example/ui/theme/Type.kt#L9-L14) for choosing different app font.

> [!WARNING]  
> _This template is only for Android app setup. If you are looking for a multi-platform supported template,_
> _look at the official [Circuit](https://github.com/slackhq/circuit) example apps included in the repository._

### Post-process after cloning 🧑‍🏭
Unfortunately, you do have to do some manual work after using the template, namely:

* [ ] Rename the package from **`app.example`** to your preferred app package name.
* [ ] Update directory structure based on package name update
* [ ] Update app name in XML and Gradle
* [ ] Update your app theme colors (_use [Theme Builder](https://material-foundation.github.io/material-theme-builder/)_)
* [ ] Generate your app icon (_use [Icon Kitchen](https://icon.kitchen/)_)
* [ ] Rename `ComposeApp***` to preferred file names
* [ ] Remove `Example***` files that were added to showcase example usage of app and Circuit.
* [ ] Choose [Google font](https://github.com/hossain-khan/android-compose-app-template/blob/main/app/src/main/java/app/example/ui/theme/Type.kt#L16-L30) for your app, or remove it.


## Demo 📹
Here is a demo of the template app containing screens shown in the 📖 [circuit tutorial](https://slackhq.github.io/circuit/tutorial/) doc.

https://github.com/user-attachments/assets/56d6f28b-5b46-4aac-a30e-80116986589e
