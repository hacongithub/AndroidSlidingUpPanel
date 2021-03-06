Android Sliding Up Panel
=========================

This library provides a simple way to add a draggable sliding up panel to your Android application.

As seen in many of our Trip Planner and Travel Companion apps.

![SlidingUpPanelLayout](https://github.com/ims-hacon/AndroidSlidingUpPanel/raw/master/slidinguppanel.png)

### Importing the Library

As the library is not published to any maven repository yet, you need to download the source code and include the library in your project as a module

**Alternative:** You can also compile it yourself and use the .aar file.

### Usage

* Include `de.hafas.slidinguppanel.SlidingUpPanelLayout` as the root element in your activity layout.
* Make sure that it has at least two children. The first child is your main layout. The second child is your layout for the sliding up panel.
* A sticky footer view can be added as 3rd child in the layout. **NOTE:**
* The main layout should have the width and the height set to `match_parent`.
* The sliding layout should have the width set to `match_parent` and the height set to either `match_parent`, `wrap_content` or the max desireable height. If you would like to define the height as the percetange of the screen, set it to `match_parent` and also define a `layout_weight` attribute for the sliding view.
* By default, the whole panel will act as a drag region and will intercept clicks and drag events. You can restrict the drag area to a specific view by using the `setDragView` method or `hafasDragView` attribute.

For more information, please refer to the sample code.

```xml
<de.hafas.slidinguppanel.SlidingUpPanelLayout
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/sliding_layout"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:hafasPanelHeight="68dp"
    app:hafasShadowHeight="4dp">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:text="Main Content"
        android:textSize="16sp" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center|top"
        android:text="The Awesome Sliding Up Panel"
        android:textSize="16sp" />

    <TextView
        android:layout_width="match_parent"
        android:layout_height="100dp"
        android:text="This is an awesome footer view!"
        android:textSize="16sp" />
</de.hafas.slidinguppanel.SlidingUpPanelLayout>
```
For smooth interaction with the ActionBar, make sure that `windowActionBarOverlay` is set to `true` in your styles:
```xml
<style name="AppTheme">
    <item name="android:windowActionBarOverlay">true</item>
</style>
```
However, in this case you would likely want to add a top margin to your main layout of `?android:attr/actionBarSize`
or `?attr/actionBarSize` to support older API versions.

### Caveats, Additional Features and Customization

* If you are using a custom `hafasDragView`, the panel will pass through the click events to the main layout. Make your second layout `clickable` to prevent this.
* You can change the panel height by using the `setPanelHeight` method or `hafasPanelHeight` attribute.
* If the panel should adjust its height to match a specific view, this view can be set using the `setHeaderView` method or the `hafasHeaderView` attribute. This won't have an effect though, until the panel height is set to `auto`.
* If you would like to hide the shadow above the sliding panel, set `shadowHeight` attribute to 0.
* Use `setEnabled(false)` to completely disable the sliding panel (including touch and programmatic sliding)
* Use `setTouchEnabled(false)` to disables panel's touch responsiveness (drag and click), you can still control the panel programatically
* Use `getPanelState` to get the current panel state
* Use `setPanelState` to set the current panel state
* You can add parallax to the main view by setting `hafasParallaxOffset` attribute (see demo for the example).
* You can set a anchor point in the middle of the screen using `setAnchorPoint` to allow an intermediate expanded state for the panel (similar to Google Maps).
* You can set a `PanelSlideListener` to monitor events about sliding panes.
* You can provide a scroll interpolator for the panel movement by setting `hafasScrollInterpolator` attribute. For instance, if you want a bounce or overshoot effect for the panel.
* By default, the panel pushes up the main content. You can make it overlay the main content by using `setOverlayed` method or `hafasOverlay` attribute. This is useful if you would like to make the sliding layout semi-transparent. You can also set `hafasClipPanel` to false to make the panel transparent in non-overlay mode.
* By default, the main content is dimmed as the panel slides up. You can change the dim color by changing `hafasFadeColor`. Set it to `"@android:color/transparent"` to remove dimming completely.

### Scrollable Sliding Views

If you have a scrollable view inside of the sliding panel, make sure that it supports nested scrolling (i.e. use `NestedScrollView` instead of `ScrollView` and `RecyclerView` instead of `ListView`).
The panel will then interact with the scrolling view, consuming scrolls to expand or collapse the panel first before allowing the user to scroll the content. You can disable this behaviour
by setting `hafasNestedScrolling` to `false` (or use `setNestedScrollingEnabled(false)`).

### Credit

This library is forked from https://github.com/umano/AndroidSlidingUpPanel which was initially based on the opened-sourced [SlidingPaneLayout](http://developer.android.com/reference/android/support/v4/widget/SlidingPaneLayout.html) component from the r13 of the Android Support Library.

### Requirements

Minimum api version 14 (4.0.1)

### Changelog
* 5.0.0
  * Removed the ability to specify a gravity, the panel will now always slide up
  * Removed the ability to specify a scrollview in favor of the more generic nested scrolling concept of Android
  * made the panel a little more resistant against race conditions due to size changes during animations
* 4.1.0
  * Added support for auto panel height.
  * Changed the value range of `slideOffset` to properly reflect the slideable range and the hidden state.
* 4.0.0
  * Migrated the project to AndroidX.
  * Changed the packagename to `de.hafas.` prefix to prevent collisions
 with the existing library.
  * Renamed attribute prefix to `hafas`.
  * Added support for a sticky footer view.
* 3.4.0
  * Use the latest support library 26 and update the min version to 14.
  * Bug fixes
* 3.3.1
  * Lots of bug fixes from various pull requests.
  * Removed the nineoldandroids dependency. Use ViewCompat instead.
* 3.3.0
  * You can now set a `FadeOnClickListener`, for when the faded area of the main content is clicked.
  * `PanelSlideListener` has a new format (multiple of them can be set now
  * Fixed the setTouchEnabled bug
* 3.2.1
  * Add support for `umanoScrollInterpolator`
  * Add support for percentage-based sliding panel height using `layout_weight` attribute
  * Add `ScrollableViewHelper` to allow users extend support for new types of scrollable views.
* 3.2.0
  * Rename `umanoParalaxOffset` to `umanoParallaxOffset`
  * RecyclerView support.
* 3.1.0
  * Added `umanoScrollableView` to supported nested scrolling in children (only ScrollView and ListView are supported for now)
* 3.0.0
  * Added `umano` prefix for all attributes
  * Added `clipPanel` attribute for supporting transparent panels in non-overlay mode.
  * `setEnabled(false)` - now completely disables the sliding panel (touch and programmatic sliding)
  * `setTouchEnabled(false)` - disables panel's touch responsiveness (drag and click), you can still control the panel programatically
  * `getPanelState` - is now the only method to get the current panel state
  * `setPanelState` - is now the only method to modify the panel state from code
* 2.0.2 - Allow `wrap_content` for sliding view height attribute. Bug fixes. 
* 2.0.1 - Bug fixes. 
* 2.0.0 - Cleaned up various public method calls. Added animated `showPanel`/`hidePanel` methods. 
* 1.0.1 - Initial Release 

### Licence

> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this work except in compliance with the License.
> You may obtain a copy of the License in the LICENSE file, or at:
>
>  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)
>
> Unless required by applicable law or agreed to in writing, software
> distributed under the License is distributed on an "AS IS" BASIS,
> WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
> See the License for the specific language governing permissions and
> limitations under the License.

