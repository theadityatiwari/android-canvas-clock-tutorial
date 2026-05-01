# android-canvas-clock-tutorial
# Building an Analog Clock from Scratch Using Android Canvas API

### No libraries. No XML drawables. Just pure Canvas, Paint, and Path.

---

If you've ever wondered how apps like Zomato draw custom progress rings, how Swiggy builds animated loaders, or how finance apps render beautiful arc charts — the answer is always the same: **Android Canvas API**.

This tutorial will teach you the Canvas fundamentals by building something real — a fully working analog clock, from a blank View subclass to a ticking, beautiful timepiece. Every line of code is explained. No hand-waving.

By the end, you'll understand:

- How `onDraw`, `onMeasure`, and `Canvas` actually work
- How `Paint`, `Path`, and transformations fit together
- How to animate a View without a single external library
- Where most developers go wrong with custom views (and how to avoid it)

---

## Prerequisites

- Kotlin basics
- Android Studio installed
- A project with an empty Activity

---

## Part 1 — The Foundation: What is a Custom View?

Every UI element in Android — `Button`, `TextView`, `ImageView` — is a `View`. When you build a **Custom View**, you subclass `View` and take ownership of three things:

- **`onMeasure`** — how big should I be?
- **`onDraw`** — what do I look like?
- **`onTouchEvent`** — how do I respond to touch?

The simplest custom view looks like this:

```kotlin
class ClockView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // you draw here
    }
}
```

**One rule to never break:** Never call `onDraw()` yourself. Call `invalidate()` instead. `invalidate()` tells Android "I need to be redrawn" — Android then calls `onDraw` at the right time in the rendering loop.

---

## Part 2 — The Canvas Coordinate System

Before drawing anything, understand the coordinate system. It is **not** like school math.

```
(0,0) ─────────────── X increases →
  │
  │
  ↓
Y increases downward
```

- Top-left = `(0, 0)`
- Bottom-right = `(width, height)`
- Center = `(width / 2f, height / 2f)`

**Always use `width` and `height` properties** instead of hardcoded values. Hardcoded pixel values are an immediate red flag in any production codebase.

```kotlin
// Wrong
canvas.drawCircle(300f, 300f, 200f, paint)

// Correct
canvas.drawCircle(width / 2f, height / 2f, radius, paint)
```

---

## Part 3 — Paint: The Brush

Canvas says *what and where*. Paint says *how it looks*. You always need both.

**Critical rule: Never create Paint objects inside `onDraw`.** `onDraw` runs 60 times per second. Creating objects inside it causes garbage collection pressure and dropped frames — the #1 custom view mistake.

```kotlin
// Wrong — creates a new object 60 times per second
override fun onDraw(canvas: Canvas) {
    val paint = Paint() // ❌
}

// Correct — created once, reused forever
private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    color = Color.RED
    strokeWidth = 8f
    style = Paint.Style.STROKE
}
```

The key Paint properties you'll use constantly:

```kotlin
paint.isAntiAlias = true          // smooth edges — always set this for curves
paint.style = Paint.Style.FILL    // solid fill
paint.style = Paint.Style.STROKE  // outline only
paint.strokeWidth = 8f            // line thickness in pixels
paint.color = Color.parseColor("#C9A84C")
paint.textSize = 42f
paint.textAlign = Paint.Align.CENTER
paint.strokeCap = Paint.Cap.ROUND // rounded line ends
```

---

## Part 4 — onMeasure: Negotiating Size

`onMeasure` is where Android asks your view: *"how big do you want to be?"*

The two parameters — `widthMeasureSpec` and `heightMeasureSpec` — are packed integers containing both a **mode** and a **size**. You unpack them like this:

```kotlin
val mode = MeasureSpec.getMode(widthMeasureSpec)
val size = MeasureSpec.getSize(widthMeasureSpec)
```

Three modes:
- `EXACTLY` — fixed size or `match_parent`. Respect it.
- `AT_MOST` — `wrap_content`. Use your desired size but don't exceed the given size.
- `UNSPECIFIED` — take whatever you need.

For our clock, we want it to be a perfect square. `resolveSize` handles the mode logic for us:

```kotlin
override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    val size = resolveSize((radius * 2 + 20f).toInt(), widthMeasureSpec)
    setMeasuredDimension(size, size)
}
```

**You must always call `setMeasuredDimension`** at the end of `onMeasure`. Skipping it crashes your app.

---

## Part 5 — Canvas Transformations: The Most Powerful Tool

This is where most tutorials skip ahead too fast. Transformations are what make complex custom views manageable.

Instead of calculating rotated coordinates manually using `sin` and `cos` for every clock hand and tick mark — you **rotate the canvas itself**, then draw simple straight lines.

Think of it like a physical drawing board. You tilt the board, then draw straight — the result appears at an angle.

Three core transformations:

```kotlin
canvas.translate(dx, dy)   // move the origin point
canvas.rotate(degrees)     // rotate clockwise around origin
canvas.scale(sx, sy)       // scale around origin
```

**Transformations stack.** Rotate after translate and both effects combine.

### save() and restore() — Isolation

Every transformation affects everything drawn after it. To isolate a transformation to just one element, wrap it in `save()` / `restore()`:

```kotlin
canvas.save()           // snapshot current state
canvas.rotate(45f)
canvas.drawLine(...)    // drawn at 45 degrees
canvas.restore()        // undo the rotation

canvas.drawCircle(...)  // drawn normally, unaffected
```

Think of `save()` as opening a bracket `{` and `restore()` as closing it `}`. **Every save must have a matching restore.**

For advanced cases, `canvas.save()` returns an integer stack level, and `canvas.restoreToCount(level)` lets you jump directly back to that state — useful for defensive drawing in complex views.

---

## Part 6 — Path: Drawing Any Shape

`drawLine`, `drawCircle`, `drawRect` only handle fixed shapes. For anything custom — triangles, arrows, trapezoids, clock hands — you use `Path`.

Think of Path like a pen on paper:

```kotlin
val path = Path()
path.moveTo(x, y)   // pick up pen, place at point. No drawing.
path.lineTo(x, y)   // draw line to this point
path.close()        // draw line back to the first moveTo — closes the shape
```

**Same rule as Paint: never create Path inside `onDraw`.** Declare it as a class field, call `path.reset()` before reusing it.

```kotlin
private val handPath = Path()  // declared once

// inside drawHand():
handPath.reset()               // clear previous shape
handPath.moveTo(...)
handPath.lineTo(...)
handPath.close()
canvas.drawPath(handPath, paint)
```

---

## Part 7 — Building the Clock: Full Code Walkthrough

Now let's put it all together. Here is the complete implementation:

```kotlin
package com.theadityatiwari.slovar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import java.util.Calendar

class CanvasPlayground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val radius = 300f
    private val innerDialRadius = 10f
    private val tickLength = 40f
    private val secondLength = 190f
    private val minuteLength = 170f
    private val hourLength = 140f
    private val handWidthBottom = innerDialRadius - 2f
    private val handWidthTop = 5f
    private val handWidthBottomSecond = innerDialRadius - 6f
    private val handWidthTopSecond = 2f
    private val handPath = Path()

    // Named Runnable — required for removeCallbacks to work correctly
    private val ticker = Runnable { invalidate() }

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")   // gold border
        style = Paint.Style.STROKE
        strokeWidth = 20f
        strokeCap = Paint.Cap.ROUND
    }
    private val clockFacePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#14142B")   // dark navy face
        style = Paint.Style.FILL
    }
    private val innerDPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")   // gold center pin
        style = Paint.Style.FILL
    }
    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#C9A84C")   // gold ticks
        strokeWidth = 8f
        style = Paint.Style.FILL
    }
    private val secondPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E63946")   // vivid red second hand
        style = Paint.Style.FILL
    }
    private val minHourPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8DFC8")   // warm cream hands
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#E8DFC8")   // warm cream numbers
        textSize = 42f
        textAlign = Paint.Align.CENTER
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val size = resolveSize((radius * 2 + 20f).toInt(), widthMeasureSpec)
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.drawColor(Color.parseColor("#0D0D1A"))  // deep navy background

        val cx = width / 2f
        val cy = height / 2f

        val calendar = Calendar.getInstance()
        val second = calendar.get(Calendar.SECOND)
        val minute = calendar.get(Calendar.MINUTE)
        val hour   = calendar.get(Calendar.HOUR)

        val secondAngle = second * 6f
        val minuteAngle = minute * 6f + second * 0.1f   // smooth minute hand
        val hourAngle   = hour * 30f + minute * 0.5f    // smooth hour hand

        // Draw filled face first
        canvas.drawCircle(cx, cy, radius, clockFacePaint)

        canvas.save()
        canvas.translate(cx, cy)  // move origin to clock center

        // Draw hands
        drawHand(canvas, handWidthTopSecond, handWidthBottomSecond, secondLength, secondPaint, secondAngle)
        drawHand(canvas, handWidthTop, handWidthBottom, minuteLength, minHourPaint, minuteAngle)
        drawHand(canvas, handWidthTop, handWidthBottom, hourLength, minHourPaint, hourAngle)

        // Draw 12 tick marks and labels
        for (i in 0..11) {
            canvas.save()
            canvas.rotate(i * 30f)                                              // rotate to tick position
            canvas.drawLine(0f, -(radius - 5f), 0f, -(radius - tickLength), tickPaint)
            canvas.translate(0f, -(radius - tickLength - 40f))                  // move to label position
            canvas.rotate(-i * 30f)                                             // undo rotation so text is upright
            canvas.drawText(getCurrTimeLabel(i), 0f, 0f, textPaint)
            canvas.restore()
        }

        canvas.restore()

        // Gold border ring drawn on top
        canvas.drawCircle(cx, cy, radius, paint)
        // Gold center pin drawn last so it sits above hands
        canvas.drawCircle(cx, cy, innerDialRadius, innerDPaint)

        removeCallbacks(ticker)
        postDelayed(ticker, 1000L)
    }

    private fun getCurrTimeLabel(i: Int): String = if (i == 0) "12" else i.toString()

    private fun drawHand(
        canvas: Canvas,
        handWidthTop: Float,
        handWidthBottom: Float,
        handLength: Float,
        paint: Paint,
        angle: Float = 0f
    ) {
        canvas.save()
        canvas.rotate(angle)
        handPath.reset()
        handPath.moveTo(-handWidthBottom, 0f)
        handPath.lineTo(handWidthBottom, 0f)
        handPath.lineTo(handWidthTop, -handLength)
        handPath.lineTo(-handWidthTop, -handLength)
        handPath.close()
        canvas.drawPath(handPath, paint)
        canvas.restore()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        removeCallbacks(ticker)  // stop ticking when view is destroyed
    }
}
```

---

## Part 8 — Key Design Decisions Explained

### Why `postDelayed` instead of an `ObjectAnimator`?

The clock only needs to update once per second. Using `postDelayed(ticker, 1000L)` is the most efficient approach — no animation overhead, no value interpolation, just a precise 1-second callback. For smooth sub-second animations (like a sweeping second hand), you'd use `invalidate()` inside `computeScroll()` with a `ValueAnimator` instead.

### Why a named `Runnable` for `ticker`?

```kotlin
private val ticker = Runnable { invalidate() }
```

`removeCallbacks(ticker)` requires the **exact same Runnable reference** to cancel correctly. A lambda `{ invalidate() }` creates a new object each time — `removeCallbacks` would never find it and you'd have a memory leak. The named field ensures the reference is stable and cancellable.

### Why draw the gold border after everything else?

Drawing order matters in Canvas — later draws sit on top. The border ring is drawn after the hands and ticks so it cleanly frames the clock face without being obscured.

### Why rotate labels back with `-i * 30f`?

After rotating the canvas for tick placement, the coordinate system itself is rotated. Drawing text in a rotated state would produce tilted numbers. Applying the inverse rotation `(-i * 30f)` brings the coordinate system back to upright before `drawText` — so every number appears perfectly horizontal regardless of its position on the clock.

---

## Part 9 — Assignments

You now understand the foundation. Here are progressively harder challenges to solidify your understanding:

---

**Assignment 1 — Minor Tick Marks** *(Beginner)*

The current clock only has 12 major hour ticks. Real clocks have 60 minute ticks — 4 smaller ticks between each major tick.

- Change the loop to `0..59`
- For every 5th tick (`i % 5 == 0`) draw a long tick and a number label
- For all other ticks draw a shorter, thinner tick with no label
- Hint: you'll need a separate `minorTickPaint` with a smaller `strokeWidth`

---

**Assignment 2 — Smooth Second Hand** *(Beginner–Intermediate)*

Currently the second hand jumps once per second. Make it sweep smoothly like an analog clock.

- Use `System.currentTimeMillis()` instead of `Calendar`
- Extract milliseconds and factor them into `secondAngle`
- Change `postDelayed(ticker, 1000L)` to use `16L` (60fps)
- Hint: `val ms = calendar.get(Calendar.MILLISECOND)` gives you 0–999

---

**Assignment 3 — AM/PM Indicator** *(Intermediate)*

Draw a small text label inside the clock face showing "AM" or "PM".

- Use `Canvas.HOUR_OF_DAY` to determine AM/PM
- Draw the text at `(0f, radius * 0.4f)` relative to the translated center
- Style it with a smaller `textSize` and a muted color

---

**Assignment 4 — Colored Hand Shadows** *(Intermediate)*

Give each clock hand a subtle shadow to create depth.

- Use `paint.setShadowLayer(radius, dx, dy, color)` on each hand paint
- Enable hardware acceleration in your Activity: `window.setFlags(FLAG_HARDWARE_ACCELERATED, FLAG_HARDWARE_ACCELERATED)`
- Note: `setShadowLayer` only works with hardware acceleration enabled

---

**Assignment 5 — Custom Attribute Support** *(Advanced)*

Make the clock configurable from XML — no code changes needed to customize it.

- Add a `res/values/attrs.xml` with custom attributes: `clockBorderColor`, `clockFaceColor`, `secondHandColor`, `showNumbers`
- Read them in the constructor using `context.obtainStyledAttributes`
- Always call `ta.recycle()` in a `finally` block
- Usage should look like:

```xml
<com.example.CanvasPlayground
    app:clockBorderColor="#FF0000"
    app:showNumbers="false" />
```

---

**Assignment 6 — Bezier Curved Hands** *(Advanced)*

Replace the trapezoid hands with smooth curved hands using `Path.quadTo`.

- Instead of 4 `lineTo` calls, use `quadTo` for the sides of the hand
- Control point should be slightly offset from center to create a gentle curve
- Hint: `path.quadTo(controlX, controlY, endX, endY)`

---

## Closing Thoughts

Every custom view you'll ever build — progress rings, rulers, charts, gauges, sliders — is built on exactly these primitives:

- `onMeasure` to claim the right size
- `Paint` declared outside `onDraw`
- `Canvas` draw methods for shapes
- `Path` for complex shapes
- `save()` / `restore()` to isolate transformations
- `invalidate()` to trigger redraws

The clock you just built uses all of them. From here, the only difference between this and a production-grade custom view is configuration, edge case handling, and performance tuning.

Build the assignments. Then build something of your own. That's when it really clicks.

---

*Built with Android Canvas API — no third-party libraries used.*
