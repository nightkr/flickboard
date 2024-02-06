# FlickBoard

FlickBoard is a flicking-style keyboard, in the style of [MessagEase] and [Thumb-Key].

## How do I install it?

There are currently no binary builds. You need to build it yourself, by running the following on a
computer that is connected to your phone:

```bash
$ ./gradlew installDebug
```

This requires the phone to have USB debugging enabled, and your computer to have the [Android SDK]
installed.

## What does it look like?

![Screenshot](screenshot.png)

## Why not MessagEase?

It's proprietary, and is currently threatening everyone's existing installations unless they
subscribe.

## Why not Thumb-Key?

Thumb-Key intentionally doesn't support some gestures that MessagEase supports.

[MessagEase]: https://www.exideas.com/ME/index.php

[Thumb-Key]: https://github.com/dessalines/thumb-key

[Android SDK]: https://developer.android.com/studio