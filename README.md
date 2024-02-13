# FlickBoard

FlickBoard is a flicking-style keyboard, in the style of [MessagEase] and [Thumb-Key].

Type the letters in the center of a key by tapping it. Type the keys in the corners by swiping in
that direction. That's it!

## How do I install it?

| <a href="https://android.izzysoft.de/repo/apk/se.nullable.flickboard"><img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" alt="Get it on IzzyOnDroid"/></a> | <a href="https://play.google.com/store/apps/details?id=se.nullable.flickboard"><img src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play"/></a> |
| - | - |

The latest release builds are also available from [GitHub Releases](https://github.com/nightkr/flickboard/releases), but I recommend downloading from one of the above instead to get automatic updates.

You can also build it yourself, by running the following on a computer that is connected to your
phone:

```bash
$ ./gradlew installDebug
```

This requires the phone to have USB debugging enabled, and your computer to have the [Android SDK]
installed.

## What does it look like?

| ![Screenshot of the keyboard in use](fastlane/metadata/android/en-US/images/phoneScreenshots/2.png) | ![Screenshot of settings panel](fastlane/metadata/android/en-US/images/phoneScreenshots/1.png) |
| - | - |

## Why not MessagEase?

It's proprietary, and is currently threatening everyone's existing installations unless they
subscribe.

## Why not Thumb-Key?

Thumb-Key intentionally doesn't support some gestures that MessagEase supports.

[MessagEase]: https://www.exideas.com/ME/index.php

[Thumb-Key]: https://github.com/dessalines/thumb-key

[Android SDK]: https://developer.android.com/studio
