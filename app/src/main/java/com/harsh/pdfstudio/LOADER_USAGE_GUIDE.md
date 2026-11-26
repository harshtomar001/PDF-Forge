# PDF Studio — Loader Usage Guide

A reference for every loader built for this app: what each one looks like, which files it needs, and how to wire it up in a Java Activity/Fragment.

---

## 1. Ring Spinner
**Best for:** generic short waits (opening a file, quick saves).

**Files needed:**
- `res/drawable/loader_ring.xml`
- `res/drawable/loader_ring_animated.xml`

**Layout:**
```xml
<ImageView
    android:id="@+id/loaderRing"
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:src="@drawable/loader_ring_animated" />
```

**Java:**
```java
PdfLoaderController controller = new PdfLoaderController(loaderRing);
controller.showLoader();
controller.hideLoader();
```

---

## 2. Page Flip
**Best for:** opening/loading a document — reinforces the "PDF" theme visually.

**Files needed:**
- `res/drawable/loader_page.xml`
- `res/drawable/loader_page_animated.xml`

**Layout:**
```xml
<ImageView
    android:id="@+id/loaderPage"
    android:layout_width="40dp"
    android:layout_height="40dp"
    android:src="@drawable/loader_page_animated" />
```

**Java:**
```java
PdfLoaderController controller = new PdfLoaderController(loaderPage);
controller.showLoader();
controller.hideLoader();
```

---

## 3. Pulsing Dots
**Best for:** background processing where there's no percentage to show (e.g. "thinking", searching, syncing).

**Files needed:**
- `res/drawable/loader_dot.xml` (used 3×)
- `res/animator/loader_dot_pulse.xml`

**Layout:**
```xml
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center">

    <View android:id="@+id/dot1" android:layout_width="8dp" android:layout_height="8dp"
        android:layout_marginEnd="6dp" android:background="@drawable/loader_dot" />
    <View android:id="@+id/dot2" android:layout_width="8dp" android:layout_height="8dp"
        android:layout_marginEnd="6dp" android:background="@drawable/loader_dot" />
    <View android:id="@+id/dot3" android:layout_width="8dp" android:layout_height="8dp"
        android:background="@drawable/loader_dot" />
</LinearLayout>
```

**Java** (default constructor already points at `loader_dot_pulse`):
```java
PdfLoaderController controller = new PdfLoaderController(anyImageView, dot1, dot2, dot3);
controller.showLoader();
controller.hideLoader();
```

---

## 4. Page Stack
**Best for:** multi-page operations — merging, splitting, reordering pages.

**Files needed:**
- `res/drawable/loader_page_stack_rect.xml` (used 3×)
- `res/animator/loader_page_stack_bounce.xml`

**Layout:**
```xml
<LinearLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center">

    <View android:id="@+id/page1" android:layout_width="14dp" android:layout_height="18dp"
        android:layout_marginEnd="6dp" android:background="@drawable/loader_page_stack_rect" />
    <View android:id="@+id/page2" android:layout_width="14dp" android:layout_height="18dp"
        android:layout_marginEnd="6dp" android:background="@drawable/loader_page_stack_rect" />
    <View android:id="@+id/page3" android:layout_width="14dp" android:layout_height="18dp"
        android:background="@drawable/loader_page_stack_rect" />
</LinearLayout>
```

**Java** (must use the 2nd constructor, passing the animator explicitly):
```java
PdfLoaderController controller = new PdfLoaderController(
    anyImageView, R.animator.loader_page_stack_bounce, page1, page2, page3
);
controller.showLoader();
controller.hideLoader();
```

---

## 5. Export Progress Bar
**Best for:** any task where you know real % complete — exporting, compressing, OCR.
This is the only loader that is **determinate** (shows real progress, not a loop) and doesn't use `PdfLoaderController` at all.

**Files needed:**
- `res/drawable/loader_progress_bar.xml`

**Layout:**
```xml
<ProgressBar
    android:id="@+id/exportProgress"
    style="?android:attr/progressBarStyleHorizontal"
    android:layout_width="200dp"
    android:layout_height="8dp"
    android:progressDrawable="@drawable/loader_progress_bar"
    android:max="100" />
```

**Java:**
```java
exportProgress.setProgress(percentComplete); // call as your task reports progress
```

---

## 6. Glass Card (optional wrapper for any loader above)
**Best for:** placing a loader over a busy background (a PDF thumbnail, a page preview) so it reads clearly.

**Files needed:**
- `res/drawable/loader_glass_card.xml`
- Optional real blur: `GlassBlurExample.java` (API 31+) or the BlurView library for older devices

**Layout:**
```xml
<FrameLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/loader_glass_card"
    android:elevation="4dp">

    <ImageView
        android:id="@+id/loaderRing"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:layout_gravity="center"
        android:src="@drawable/loader_ring_animated" />
</FrameLayout>
```

---

## Running real work behind any loader

Use `runWithLoader()` to tie a loader's show/hide to a background task automatically — regardless of which loader you're using, as long as it's built via `PdfLoaderController`:

```java
controller.runWithLoader(
    () -> myPdfTask(),                          // runs off the main thread
    () -> Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show(),
    () -> Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
);
```

Call `controller.shutdown()` in `onDestroy()` to release the background executor.

---

## Which one should I use? Quick decision guide

| Situation | Loader |
|---|---|
| Opening a file, quick save | Ring spinner |
| Loading/importing a document | Page flip |
| Background task, no known duration (search, sync, "thinking") | Pulsing dots |
| Merge, split, reorder — multi-page operations | Page stack |
| Export, compress, OCR — you know % complete | Progress bar |
| Loader sits over a busy/colorful background | Wrap in glass card |

All loaders other than the progress bar are indeterminate loops that animate until you call `hideLoader()` — always pair `showLoader()` with a matching `hideLoader()` (or use `runWithLoader()` so it's automatic) so nothing gets stuck spinning.
