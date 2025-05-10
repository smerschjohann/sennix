# Sennix

Sennix is a small system app that makes the Nixplay motion sensor available. It's main purpose is to provide the functionality to turn off the screen when no one is around.

It can call *ImmichFrame* with the appropiate `/dim` and `/undim` API-calls, but can also work on its own by acquiring a Wakelock. Both functionalities can be enabled or disabled in the application settings..

## Build

1. Clone the repository:
  ```bash
  git clone https://github.com/smerschjohann/sennix.git
  ```
2. Open the project in Android Studio.
3. Build and install the app on your Android device by following the Installation guidelines.


## Installation

**Disclaimer**: This guide is based on the W10E model. Please note that improper handling or software errors may damage your device. You are the only one responsible for any damages caused by your modification to either hardware or software.

To open the device, you can use a credit card or the IFixit opener, which worked well and left no visible marks after reassembly.

Behind the display, there is a USB port on a circuit board. The display is lightly taped to the metal backing, which can be carefully removed for better access to the controller.

You may need to unscrew the board to connect a USB cable to the port. The frame will automatically power on when connected to a PC.

As soon as you have access to ADB over USB you can start replacing the software.

1. Download `sennix.apk`, [ImmichFrame](https://github.com/immichFrame/ImmichFrame_Android) and [scrcpy](https://github.com/Genymobile/scrcpy) which allows you to interact with the nixplay screen remotely.
2. Disable the original software on the frame.
```bash
adb shell
$ su
$ pm disable com.kitesystems.nix.prod & pm disable com.kitesystems.nix.frame
```
3. Enable ADB over network to ensure that you can access the device later on, even without opening the frame again.
```bash
adb shell
$ su
$ setprop persist.adb.tcp.port 5555 # or any other port you want
```
4. Install `sennix.apk`. **Note**: It must be a system app (which it is by putting it in /system/app) to access the motion sensor.
```bash
adb root && adb remount && adb push sennix.apk /system/app/sennix.apk && adb reboot
```
5. Install `immichframe.apk` (>= v41):
```bash
adb install ImmichFrame_v41.apk
```
5. Setup your credentials. You can use [scrcpy](https://github.com/Genymobile/scrcpy) to screen share the Nixplay screen to your PC.

Fill in your ImmichFrame server settings, you can do so with the help of `adb`:
```bash
adb shell input text http://192.168.0.42:1234
```
6. Setup *Sennix* the way you want, you can open the settings by
```bash
adb shell am start dev.sennix/.MainActivity
```

7. Go to Android Settings (using settings menu or by `adb shell am start -a android.settings.SETTINGS`). Set the display to turn off after shortest time, disable screensaver if any.
   use `adb shell settings put system screen_off_timeout 1` to set to the absolute minimal time. If you only want to use the dimming function of *ImmichFrame* without turning off the Android screen, you have to set it to never turn off instead.

*Enjoy!*

## License

This project is licensed under the [MIT License](LICENSE).
