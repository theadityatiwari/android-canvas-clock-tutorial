# 🕐 Android Canvas Clock — From Zero to Custom View

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white"/>
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>
  <img src="https://img.shields.io/badge/Canvas%20API-FF6B35?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/No%20Libraries-00C853?style=for-the-badge"/>
  <img src="https://img.shields.io/badge/Beginner%20Friendly-blue?style=for-the-badge"/>
</p>

<p align="center">
  <b>A complete, beginner-friendly tutorial on Android Canvas API.</b><br/>
  Build a fully working analog clock from absolute scratch — one step at a time.<br/>
  No third-party libraries. No XML drawables. Pure Canvas, Paint, and Path.
</p>

<p align="center">
  ⭐ <b>If this tutorial helped you understand Canvas — please star this repo. It helps others find it.</b> ⭐
</p>

---

## 📖 What You Will Learn

- What a Custom View is and how Android renders it
- The Canvas coordinate system and how to think about drawing
- Every important Canvas draw method — with short examples
- Paint and all its properties — what each one does
- How `onMeasure` works — negotiating size with Android
- Canvas transformations — `save`, `restore`, `translate`, `rotate`
- How to draw complex shapes with `Path`
- How to animate a View without any library
- How to properly manage View lifecycle

---

## 🗂️ What Is In This Repository

```
├── README.md              ← you are here — full tutorial
└── CanvasPlayground.kt    ← final complete clock code
```

---

## 🧱 Part 1 — What Is a Custom View?

Every UI element in Android — `Button`, `TextView`, `ImageView` — is a `View`. It's just a rectangular area on screen that knows how to draw itself.

When you create a **Custom View**, you subclass `View` and take ownership of three things:

| Method | Responsibility |
|---|---|
| `onMeasure` | How big should I be? |
| `onDraw` | What do I look like? |
| `onTouchEvent` | How do I respond to touch? |

The skeleton of every custom view looks like this:

```kotlin
class ClockView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // everything you draw goes here
    }
}
```

### ⚠️ The Most Important Rule

**Never call `onDraw()` yourself.**

Android owns the rendering loop. You call `invalidate()` which signals Android that the view needs to be redrawn — Android then calls `onDraw()` at the right time.

```kotlin
// Wrong ❌
onDraw(canvas)

// Correct ✅
invalidate()
```

---

## 📐 Part 2 — The Canvas Coordinate System

Before touching a single draw method, you must understand how Canvas thinks about space. It is **not** like school math coordinates.

```
(0,0) ──────────────────────── X increases →
  │
  │
  │
  ↓
Y increases downward
```

- **Top-left** = `(0, 0)`
- **Top-right** = `(width, 0)`
- **Bottom-left** = `(0, height)`
- **Bottom-right** = `(width, height)`
- **Center** = `(width / 2f, height / 2f)`

### Never Hardcode Pixel Values

Always use the view's `width` and `height` properties so your view works on every screen size.

```kotlin
// Wrong ❌ — breaks on different screen sizes
canvas.drawCircle(300f, 300f, 200f, paint)

// Correct ✅ — works on every device
canvas.drawCircle(width / 2f, height / 2f, radius, paint)
```

---

## 🎨 Part 3 — Paint: Your Drawing Brush

**Canvas** says *what to draw and where*. **Paint** says *how it looks*. You always need both.

### The #1 Performance Rule

> **Never create Paint objects inside `onDraw`.**

`onDraw` runs up to 60 times per second. Creating a `Paint()` object inside it allocates memory on every frame — causing garbage collection and dropped frames.

```kotlin
// Wrong ❌ — creates a new Paint object 60 times per second
override fun onDraw(canvas: Canvas) {
    val paint = Paint() // GC pressure every frame
}

// Correct ✅ — created once as a class field, reused forever
private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.RED
    strokeWidth = 8f
}
```

### Paint Properties Reference

```kotlin
// Smooth edges on curves and diagonals — always set this
paint.isAntiAlias = true

// FILL = solid shape, STROKE = outline only, FILL_AND_STROKE = both
paint.style = Paint.Style.STROKE

// Thickness of lines — only matters when style is STROKE
paint.strokeWidth = 8f

// Color — use Color constants or hex strings
paint.color = Color.RED
paint.color = Color.parseColor("#C9A84C")

// Transparency — 0 = invisible, 255 = fully opaque
paint.alpha = 128

// For drawing text
paint.textSize = 42f
paint.textAlign = Paint.Align.CENTER  // LEFT, CENTER, RIGHT

// Shape of line endings
paint.strokeCap = Paint.Cap.ROUND   // rounded
paint.strokeCap = Paint.Cap.BUTT    // flat, no extension
paint.strokeCap = Paint.Cap.SQUARE  // flat, extends slightly
```

---

## 🖌️ Part 4 — Canvas Draw Methods

Here is every draw method you will use regularly, with short examples.

### `drawColor` — Fill the entire canvas

```kotlin
canvas.drawColor(Color.BLACK)
canvas.drawColor(Color.parseColor("#0D0D1A"))
```

Use this as the first call in `onDraw` to set the background color.

---

### `drawPoint` / `drawPoints` — Single dots

```kotlin
// One dot
canvas.drawPoint(100f, 200f, paint)

// Many dots efficiently — pairs of (x, y)
val points = floatArrayOf(100f, 200f, 300f, 400f, 500f, 600f)
canvas.drawPoints(points, paint)
```

`paint.strokeWidth` controls how large each dot appears.

---

### `drawLine` / `drawLines` — Straight lines

```kotlin
// One line from (x1,y1) to (x2,y2)
canvas.drawLine(0f, 0f, 100f, 100f, paint)

// Many lines efficiently — groups of 4 floats (x1, y1, x2, y2)
val lines = floatArrayOf(
    0f, 0f, 100f, 100f,     // line 1
    200f, 0f, 300f, 100f    // line 2
)
canvas.drawLines(lines, paint)
```

`drawLines` batches multiple lines into one native call — more efficient than a loop of `drawLine`.

---

### `drawRect` / `drawRoundRect` — Rectangles

```kotlin
// Sharp corners
canvas.drawRect(left, top, right, bottom, paint)

// Rounded corners
canvas.drawRoundRect(left, top, right, bottom, cornerRadiusX, cornerRadiusY, paint)
```

With `Paint.Style.FILL` — solid rectangle. With `Paint.Style.STROKE` — just the border.

---

### `drawCircle` — Circles

```kotlin
canvas.drawCircle(
    centerX,   // x coordinate of center
    centerY,   // y coordinate of center
    radius,    // radius in pixels
    paint
)
```

---

### `drawArc` — Partial circles and pie slices

This is one of the most important methods — used for progress rings, BMI arcs, gauges.

```kotlin
canvas.drawArc(
    left, top, right, bottom,  // bounding box of the full circle
    startAngle,                // 0° = 3 o'clock position. Goes clockwise.
    sweepAngle,                // how many degrees to draw
    useCenter,                 // true = pie slice, false = arc only
    paint
)
```

Example — a 75% progress arc starting from 12 o'clock:

```kotlin
canvas.drawArc(
    100f, 100f, 300f, 300f,
    -90f,   // start at 12 o'clock (subtract 90 from default 3 o'clock)
    270f,   // 75% of 360 degrees
    false,  // arc only, no lines to center
    paint
)
```

---

### `drawText` — Text on canvas

```kotlin
canvas.drawText("Hello", x, y, paint)
```

> ⚠️ **Important:** `y` is the **text baseline**, not the top edge. This trips everyone up.

`paint.textAlign` controls what `x` means:
- `LEFT` — x is the left edge of the text
- `CENTER` — x is the horizontal center of the text
- `RIGHT` — x is the right edge of the text

---

### `drawPath` — Any custom shape

For triangles, arrows, clock hands — any shape that doesn't fit the above methods. Covered in depth in Part 7.

---

### Quick Reference

| Method | Draws |
|---|---|
| `drawColor` | Solid background fill |
| `drawPoint` / `drawPoints` | Dots |
| `drawLine` / `drawLines` | Straight lines |
| `drawRect` | Rectangle |
| `drawRoundRect` | Rounded rectangle |
| `drawCircle` | Circle |
| `drawArc` | Arc or pie slice |
| `drawText` | Text string |
| `drawBitmap` | Image |
| `drawPath` | Any custom shape |

---

## 📏 Part 5 — onMeasure: Claiming Your Size

`onMeasure` is where Android negotiates size with your view. It calls this method with two parameters that each contain a **mode** and a **size** packed into one integer.

```kotlin
val widthMode = MeasureSpec.getMode(widthMeasureSpec)
val widthSize = MeasureSpec.getSize(widthMeasureSpec)
```

### The Three Modes

| Mode | Triggered by | Meaning |
|---|---|---|
| `EXACTLY` | `match_parent` or fixed dp | You must be exactly this size |
| `AT_MOST` | `wrap_content` | You can be up to this size |
| `UNSPECIFIED` | Inside `ScrollView` | Take whatever you need |

### You Must Always Call `setMeasuredDimension`

Skipping this crashes your app. It's how you tell Android the final size you've decided on.

```kotlin
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val desiredSize = (radius * 2 + 20f).toInt()

    val w = when (MeasureSpec.getMode(widthMeasureSpec)) {
        MeasureSpec.EXACTLY  -> MeasureSpec.getSize(widthMeasureSpec)
        MeasureSpec.AT_MOST  -> minOf(desiredSize, MeasureSpec.getSize(widthMeasureSpec))
        else                 -> desiredSize
    }

    setMeasuredDimension(w, w)  // square clock — same for both dimensions
}
```

`resolveSize(desired, measureSpec)` is a built-in helper that does this mode logic for you:

```kotlin
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val size = resolveSize((radius * 2 + 20f).toInt(), widthMeasureSpec)
    setMeasuredDimension(size, size)
}
```

---

## 🔄 Part 6 — Canvas Transformations

This is the most powerful concept in Canvas drawing. Instead of calculating rotated coordinates manually with `sin` and `cos` — you rotate the canvas itself, then draw simple shapes.

**Think of it like a physical drawing board. You tilt the board first, then draw a straight line — the result appears rotated on screen.**

### `translate(dx, dy)` — Move the origin

```kotlin
canvas.translate(540f, 960f)
// Now (0, 0) refers to the point (540, 960) on screen
// All subsequent draw calls are relative to this new origin
```

### `rotate(degrees)` — Rotate clockwise around origin

```kotlin
canvas.rotate(45f)
// The coordinate system is now rotated 45° clockwise
// A line drawn "upward" now points upper-right
```

### `scale(sx, sy)` — Scale around origin

```kotlin
canvas.scale(2f, 2f)
// Everything drawn after this is twice as large
```

### Transformations Stack

```kotlin
canvas.translate(540f, 960f)  // move origin to center
canvas.rotate(45f)             // rotate that center
canvas.drawLine(0f, 0f, 0f, -200f, paint)
// Result: a line starting at (540, 960) pointing upper-right at 45°
```

### `save()` and `restore()` — Isolating Transformations

Every transformation affects everything drawn after it. Wrap transformations in `save()` / `restore()` to isolate them.

```kotlin
canvas.save()           // 📸 snapshot current state
canvas.rotate(45f)
canvas.drawLine(...)    // drawn at 45°
canvas.restore()        // ↩️ undo the rotation

canvas.drawCircle(...)  // drawn normally — unaffected by the rotation above
```

> Think of `save()` as opening a bracket `{` and `restore()` as closing it `}`.
> **Every save must have a matching restore.**

### Advanced: `restoreToCount`

`canvas.save()` returns an integer — the stack depth. You can jump back to any depth:

```kotlin
val checkpoint = canvas.save()
// ... many nested save/restore calls ...
canvas.restoreToCount(checkpoint)  // jump directly back to this state
```

Useful as a safety net in `onDraw`:

```kotlin
override fun onDraw(canvas: Canvas) {
    val checkpoint = canvas.save()
    try {
        // all your drawing
    } finally {
        canvas.restoreToCount(checkpoint)  // always cleans up, even on exceptions
    }
}
```

---

## ✏️ Part 7 — Path: Drawing Any Shape

`Path` is how you draw anything that isn't a standard shape — triangles, clock hands, arrows, custom outlines.

Think of Path like a pen on paper:

```kotlin
val path = Path()
path.moveTo(x, y)    // pick up pen, place it here. No line drawn.
path.lineTo(x, y)    // draw a straight line to this point
path.close()         // draw a line back to the first moveTo — closes the shape
```

### Drawing a Triangle

```kotlin
private val trianglePath = Path()  // declared outside onDraw

// inside onDraw:
trianglePath.reset()              // clear previous frame's shape
trianglePath.moveTo(200f, 0f)     // top point
trianglePath.lineTo(400f, 400f)   // bottom right
trianglePath.lineTo(0f, 400f)     // bottom left
trianglePath.close()              // line back to top
canvas.drawPath(trianglePath, paint)
```

> ⚠️ Same rule as Paint — **never create `Path()` inside `onDraw`**. Declare it as a class field and call `reset()` to reuse it.

### Curves with Path

```kotlin
// Quadratic bezier — one control point
path.quadTo(controlX, controlY, endX, endY)

// Cubic bezier — two control points
path.cubicTo(cx1, cy1, cx2, cy2, endX, endY)
```

Used for smooth wave animations, speech bubbles, smooth progress curves.

---

## 🏗️ Part 8 — Building the Clock Step by Step

Now we build the clock incrementally. Each step compiles and runs — you can see the result at every stage.

---

### Step 1 — Create the View and Draw the Clock Face

**What we're building:** A dark circle on a dark background — the clock face.

**New concepts introduced:** `onMeasure`, `drawColor`, `drawCircle`, Paint with `FILL` style.

```kotlin
class CanvasPlayground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val radius = 300f

    // Paint for the clock face — filled dark navy circle
    private val clockFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#14142B")
        style = Paint.Style.FILL       // solid fill, not outline
    }

    // Paint for the gold border ring
    private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")
        style = Paint.Style.STROKE     // outline only
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // resolveSize respects EXACTLY and AT_MOST modes automatically
        val size = resolveSize((radius * 2 + 20f).toInt(), widthMeasureSpec)
        setMeasuredDimension(size, size)   // square view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height / 2f

        // Step 1: Fill background
        canvas.drawColor(Color.parseColor("#0D0D1A"))

        // Step 2: Draw filled clock face circle
        canvas.drawCircle(cx, cy, radius, clockFacePaint)

        // Step 3: Draw gold border ring on top
        canvas.drawCircle(cx, cy, radius, borderPaint)
    }
}
```

**Why draw border after face?** Canvas draws in order — later calls sit on top of earlier ones. The border must be drawn after the face so it frames it cleanly.

**Result:** A dark navy circle with a gold border on a deep navy background.

---

### Step 2 — Add 12 Hour Tick Marks

**What we're building:** 12 gold tick marks at the hour positions around the clock.

**New concepts introduced:** `translate`, `rotate`, `save`/`restore` in a loop.

```kotlin
// Add this Paint as a class field
private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#C9A84C")   // gold
    strokeWidth = 8f
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
}

private val tickLength = 40f
```

Add this inside `onDraw`, after drawing the face and before drawing the border:

```kotlin
canvas.save()
canvas.translate(cx, cy)   // move origin to clock center — all ticks now relative to center

for (i in 0..11) {
    canvas.save()
    canvas.rotate(i * 30f)   // rotate to this hour's position (360/12 = 30° per hour)

    // Draw a line from near the edge inward
    // Negative Y = upward from center (remember: Y decreases going up)
    canvas.drawLine(
        0f, -(radius - 5f),          // start: just inside the border
        0f, -(radius - tickLength),  // end: 40px inward
        tickPaint
    )

    canvas.restore()   // undo this tick's rotation before next iteration
}

canvas.restore()   // undo the translate
```

**Why `save`/`restore` inside the loop?** Each iteration rotates the canvas. Without `restore`, rotation accumulates — tick 2 would be at 60°, tick 3 at 90°, etc. `restore` resets to the translated state (not fully clean state) before each rotation.

**Why negative Y for the tick start?** After `translate(cx, cy)`, the origin is at center. Moving upward means decreasing Y. `-(radius - 5f)` places us near the top of the clock.

**Result:** 12 evenly spaced gold tick marks around the clock edge.

---

### Step 3 — Add Hour Number Labels

**What we're building:** The numbers 1–12 at each hour position, always upright regardless of rotation.

**New concepts introduced:** `drawText`, undoing rotation for text, `textAlign`.

```kotlin
// Add this Paint as a class field
private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#E8DFC8")   // warm cream
    textSize = 42f
    textAlign = Paint.Align.CENTER        // x coordinate = horizontal center of text
}
```

Extend the tick loop to also draw the label:

```kotlin
for (i in 0..11) {
    canvas.save()
    canvas.rotate(i * 30f)

    // Draw tick mark
    canvas.drawLine(0f, -(radius - 5f), 0f, -(radius - tickLength), tickPaint)

    // Move origin to where we want the label
    canvas.translate(0f, -(radius - tickLength - 40f))

    // Undo the rotation so text appears upright
    // Without this, "3" at the 3 o'clock position would be rotated 90°
    canvas.rotate(-i * 30f)

    canvas.drawText(if (i == 0) "12" else i.toString(), 0f, 0f, textPaint)

    canvas.restore()
}
```

**Why `canvas.rotate(-i * 30f)` before drawing text?**

After rotating for the tick position, the coordinate system is tilted. If you draw text now, the number itself would appear rotated. Applying the inverse rotation `(-i * 30f)` brings the coordinate system back to upright — so every number reads normally regardless of where it sits on the clock.

**Why `translate` before the counter-rotation?**

`translate` moves the origin to the label position while the coordinate system is still rotated — so "upward" in the rotated system is exactly the right spot. Then we counter-rotate for upright text. The order matters.

**Result:** Numbers 1–12 correctly positioned and all perfectly upright.

---

### Step 4 — Draw the Clock Hands with Path

**What we're building:** Hour, minute, and second hands — each a tapered trapezoid shape.

**New concepts introduced:** `Path`, `moveTo`, `lineTo`, `close`, `Calendar` for current time.

The hand shape is a trapezoid — wider at the base (pivot), narrower at the tip:

```
    ──   ← handWidthTop (narrow tip)
   /  \
  /    \
 ────────  ← handWidthBottom (wide base at center)
```

```kotlin
// Class fields
private val handPath = Path()   // reused every frame — never created inside onDraw
private val innerDialRadius = 10f

private val secondLength = 190f
private val minuteLength = 170f
private val hourLength = 140f
private val handWidthBottom = innerDialRadius - 2f   // wide base
private val handWidthTop = 5f                         // narrow tip
private val handWidthBottomSecond = innerDialRadius - 6f
private val handWidthTopSecond = 2f

private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#E63946")   // vivid red
    style = Paint.Style.FILL
}
private val minHourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#E8DFC8")   // warm cream
    style = Paint.Style.FILL
}
// Gold filled center pin — drawn last so it sits above all hands
private val innerDPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.parseColor("#C9A84C")
    style = Paint.Style.FILL
}
```

Add a private helper function:

```kotlin
private fun drawHand(
    canvas: Canvas,
    handWidthTop: Float,
    handWidthBottom: Float,
    handLength: Float,
    paint: Paint,
    angle: Float = 0f
) {
    canvas.save()
    canvas.rotate(angle)   // rotate to this hand's current time position

    handPath.reset()
    // Build trapezoid shape centered at origin
    // Base: from (-handWidthBottom, 0) to (handWidthBottom, 0) — horizontal line at center
    // Tip:  from (-handWidthTop, -handLength) to (handWidthTop, -handLength) — narrow end pointing up
    handPath.moveTo(-handWidthBottom, 0f)
    handPath.lineTo(handWidthBottom, 0f)
    handPath.lineTo(handWidthTop, -handLength)
    handPath.lineTo(-handWidthTop, -handLength)
    handPath.close()   // connects back to (-handWidthBottom, 0)

    canvas.drawPath(handPath, paint)
    canvas.restore()
}
```

Get the current time and draw the hands inside `onDraw`:

```kotlin
val calendar = Calendar.getInstance()
val second = calendar.get(Calendar.SECOND)
val minute = calendar.get(Calendar.MINUTE)
val hour   = calendar.get(Calendar.HOUR)   // 12-hour format

// Angles — each unit maps to degrees
// Second: 60 seconds = 360° → 1 second = 6°
val secondAngle = second * 6f

// Smooth minute hand — also advances slightly as seconds pass
// 60 minutes = 360° → 1 minute = 6°  |  each second = 6/60 = 0.1°
val minuteAngle = minute * 6f + second * 0.1f

// Smooth hour hand — also advances as minutes pass
// 12 hours = 360° → 1 hour = 30°  |  each minute = 30/60 = 0.5°
val hourAngle = hour * 30f + minute * 0.5f

// Inside the canvas.save()/translate(cx,cy) block:
drawHand(canvas, handWidthTopSecond, handWidthBottomSecond, secondLength, secondPaint, secondAngle)
drawHand(canvas, handWidthTop, handWidthBottom, minuteLength, minHourPaint, minuteAngle)
drawHand(canvas, handWidthTop, handWidthBottom, hourLength, minHourPaint, hourAngle)
```

Draw the center pin last — after `canvas.restore()` from the translate block, back to screen coordinates:

```kotlin
canvas.drawCircle(cx, cy, innerDialRadius, innerDPaint)
```

**Why draw center pin last?** It must sit visually above all three hands. Drawing order = visual stack order.

**Result:** All three clock hands correctly positioned showing the current time.

---

### Step 5 — Make It Tick: Animation

**What we're building:** The clock updates every second automatically.

**New concepts introduced:** `postDelayed`, named `Runnable`, `onDetachedFromWindow`.

```kotlin
// Class field — named Runnable, not a lambda
// Must be named so removeCallbacks can find it by reference
private val ticker = Runnable { invalidate() }
```

At the end of `onDraw`:

```kotlin
removeCallbacks(ticker)          // cancel any pending callback first
postDelayed(ticker, 1000L)       // schedule next redraw in 1 second
```

And always clean up when the view is destroyed:

```kotlin
override fun onDetachedFromWindow() {
    super.onDetachedFromWindow()
    removeCallbacks(ticker)   // stop the loop — prevent memory leaks
}
```

**Why a named `Runnable` instead of a lambda?**

`removeCallbacks(runnable)` matches by **reference**. A lambda `{ invalidate() }` creates a new object each time — `removeCallbacks` can never find and cancel it. The named field `ticker` always refers to the same object, so cancellation works correctly.

**Why `removeCallbacks` before `postDelayed` inside `onDraw`?**

If `onDraw` is called multiple times rapidly (e.g. during a layout change), multiple `postDelayed` calls would stack up, causing the clock to update more than once per second. Cancelling first ensures only one callback is ever pending at a time.

**Result:** A fully ticking analog clock.

---

### Complete Final Code

The complete, production-quality file is in [`CanvasPlayground.kt`](./CanvasPlayground.kt) in this repository.

---

## 🎓 Part 9 — Assignments

You now understand every concept used in this clock. Here are six challenges to deepen your knowledge — each builds on the last.

---

### 🟢 Assignment 1 — Minor Tick Marks *(Beginner)*

Real clocks have 60 minute ticks — 4 smaller ticks between each major hour tick.

**What to do:**
- Change the hour loop from `0..11` to `0..59`
- Every 5th tick (`i % 5 == 0`) → draw a long tick with a number label (existing behavior)
- All other ticks → draw a shorter, thinner tick with no label

**Hint:** You'll need a second tick paint with smaller `strokeWidth` and shorter tick length. The rotation per tick becomes `i * 6f` (360° / 60 ticks).

---

### 🟢 Assignment 2 — Smooth Sweeping Second Hand *(Beginner)*

The second hand currently jumps once per second. Make it sweep smoothly like a luxury watch.

**What to do:**
- Extract milliseconds: `val ms = calendar.get(Calendar.MILLISECOND)`
- Factor them into `secondAngle`: `val secondAngle = second * 6f + ms * 0.006f`
- Change `postDelayed(ticker, 1000L)` to `postDelayed(ticker, 16L)` for ~60fps updates

**Think about:** What is `0.006f` and where does it come from?

---

### 🟡 Assignment 3 — AM/PM Indicator *(Intermediate)*

Draw a small "AM" or "PM" text label inside the clock face, below center.

**What to do:**
- Use `Calendar.HOUR_OF_DAY` (0–23) to determine AM or PM
- Draw the text at approximately `(0f, radius * 0.45f)` relative to the translated center
- Use a smaller `textSize` and a muted color so it doesn't compete with the hands

**Hint:** This draw call goes inside the `canvas.save()/translate(cx, cy)` block.

---

### 🟡 Assignment 4 — Hand Shadows *(Intermediate)*

Add a subtle drop shadow to each clock hand to create depth.

**What to do:**
- Use `paint.setShadowLayer(blurRadius, dx, dy, shadowColor)` on each hand paint
- Enable hardware acceleration in your Activity: `window.addFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED)`

**Important:** `setShadowLayer` only works with hardware acceleration enabled — without it, shadows are silently ignored.

---

### 🔴 Assignment 5 — Custom XML Attributes *(Advanced)*

Make the clock configurable directly from XML without touching Kotlin code.

**What to do:**
- Create `res/values/attrs.xml` with attributes: `clockBorderColor`, `clockFaceColor`, `secondHandColor`, `showNumbers`
- Read them in the constructor using `context.obtainStyledAttributes`
- Always call `ta.recycle()` in a `finally` block

**Target usage:**
```xml
<com.example.CanvasPlayground
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    app:clockBorderColor="#FF0000"
    app:secondHandColor="#00FF00"
    app:showNumbers="false" />
```

---

### 🔴 Assignment 6 — Bezier Curved Hands *(Advanced)*

Replace the straight trapezoid hands with smooth curved hands using `Path.quadTo`.

**What to do:**
- Instead of 4 straight `lineTo` calls in `drawHand`, use `quadTo` for the sides
- The control point should be slightly offset from the center line to create a gentle outward curve
- The tip of the hand should taper to a fine point

**Hint:** `path.quadTo(controlX, controlY, endX, endY)` — experiment with the control point offset.

---

## 💡 Key Takeaways

| Concept | Rule |
|---|---|
| Paint | Always declare outside `onDraw` — never inside |
| Path | Always declare outside `onDraw` — call `reset()` to reuse |
| Redrawing | Call `invalidate()`, never `onDraw()` directly |
| Size | Use `width` / `height`, never hardcoded pixels |
| Transformations | Every `save()` must have a matching `restore()` |
| Lifecycle | Always cancel callbacks in `onDetachedFromWindow` |
| Object pools | Use `VelocityTracker.obtain()` / `recycle()` pattern for pooled objects |

---

## 📚 What to Learn Next

Once you're comfortable with this clock, these are the natural next steps:

- **`clipRect` and `clipPath`** — restrict drawing to specific regions (used in before/after image sliders)
- **`PorterDuff modes`** — advanced compositing for masking and blending
- **`ValueAnimator` with Canvas** — smooth property-based animations
- **`RenderEffect` (API 31+)** — GPU-accelerated blur and visual effects
- **`Jetpack Compose Canvas`** — the Compose equivalent using `DrawScope`

---

<p align="center">
  <b>⭐ If this tutorial helped you — star this repo. It takes one second and helps other developers find it. ⭐</b>
</p>

<p align="center">
  Built with ❤️ using Android Canvas API — zero libraries.
</p>
