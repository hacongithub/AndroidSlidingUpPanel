package de.hafas.slidinguppanel;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PointF;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.Interpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;

/**
 * Helper class that processes touch events and nested scrolling to calculate the slide offset of
 * the panel.
 * <p>
 * It has several Methods that must be called from the View methods with the same signature.
 * <p>
 * The basic idea is to only use the relative slide offset, never absolute pixel coordinates to
 * track the position during animation or dragging. This prevents race conditions with size changes.
 */
class ViewSlideHelper {
    @NonNull
    private final Callback callback;
    private final int touchSlop;

    @NonNull
    private final Interpolator snapInterpolator;
    @Nullable
    private ValueAnimator snapAnimator;

    private boolean nestedScrollingEnabled = true;

    /**
     * How far the panel is offset from its expanded position.
     * range [-1, 0, 1] where -1 = hidden, 0 = collapsed, 1 = expanded.
     */
    private float mSlideOffset = 0;

    private int trackedPointerId = MotionEvent.INVALID_POINTER_ID;
    private PointF touchStart = new PointF();
    private PointF lastDragPoint = new PointF();
    private VelocityTracker velocityTracker = null;
    private boolean dragging = false;

    private int scrollDistance;
    private int consumedScrollDistance;
    private long scrollStartTime;

    ViewSlideHelper(@NonNull Context context, @NonNull Callback callback, @Nullable Interpolator snapInterpolator) {
        touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.callback = callback;
        this.snapInterpolator = snapInterpolator != null ? snapInterpolator : new DefaultSnapInterpolator();
    }

    boolean isNestedScrollingEnabled() {
        return nestedScrollingEnabled;
    }

    void setNestedScrollingEnabled(boolean nestedScrollingEnabled) {
        this.nestedScrollingEnabled = nestedScrollingEnabled;
    }

    boolean onInterceptTouchEvent(MotionEvent event) {
        return processTouchEvent(event);
    }

    boolean onTouchEvent(MotionEvent event) {
        return processTouchEvent(event);
    }

    boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes, int type) {
        if (!nestedScrollingEnabled) {
            stopTouchTracking();
            return false;
        }
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL;
    }

    void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            consumedScrollDistance = 0;
            scrollDistance = 0;
            scrollStartTime = SystemClock.uptimeMillis();
        }
        // do not intercept any more touch events and let the child scroll
        stopTouchTracking();
        callback.onDragStarted();
    }

    void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            scrollDistance += dy;
        }
        if (type == ViewCompat.TYPE_NON_TOUCH && consumedScrollDistance != 0) {
            consumed[1] = dy;
        }
        if (type == ViewCompat.TYPE_TOUCH && dy > 0 && mSlideOffset < 1.0f) {
            // a child wants to scroll down but the panel is not fully expanded, we consume the
            // scroll to open the panel first
            movePanelByScroll(dy, consumed);
        }
    }

    void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type, @NonNull int[] consumed) {
        if (dyUnconsumed < 0 && mSlideOffset > 0.0f && type == ViewCompat.TYPE_TOUCH) {
            // a child reported that it was scrolled up, but has reached the upper edge
            // of its content. The panel can use the remaining scroll motion to collapse.
            movePanelByScroll(dyUnconsumed, consumed);
        }
    }

    private void movePanelByScroll(int dy, @NonNull int[] consumed) {
        int movedDistance = movePanelRelative(-dy);
        // because the panel is moving up (towards smaller Y values), the moved distance will be
        // negative and needs to be inverted
        consumed[1] += -movedDistance;
        consumedScrollDistance += -movedDistance;
    }

    void onStopNestedScroll(@NonNull View target, int type) {
        if (type == ViewCompat.TYPE_TOUCH) {
            long now = SystemClock.uptimeMillis();
            if (scrollDistance != 0 && now != scrollStartTime && consumedScrollDistance != 0) {
                float scrollDurationS = (now - scrollStartTime) / 1000.0f;
                float scrollVelocity = scrollDistance / scrollDurationS;
                snapAnimator = createSnapAnimator(-scrollVelocity);
                if (snapAnimator != null) {
                    snapAnimator.start();
                }
            }
        }
    }

    private boolean processTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (callback.isDraggable(event.getRawX(), event.getRawY())) {
                    trackedPointerId = event.getPointerId(0);
                    touchStart.set(event.getX(), event.getY());
                    velocityTracker = VelocityTracker.obtain();
                    velocityTracker.addMovement(event);
                }
                return false;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerId(event.getActionIndex()) != trackedPointerId) {
                    return false;
                }
                // fall though
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                // todo: technically speaking, cancel should snap the panel back to its starting position...
                if (trackedPointerId == MotionEvent.INVALID_POINTER_ID) {
                    // no drag in progress, ignore event
                    return false;
                } else {
                    boolean wasDragging = dragging;
                    if (dragging) {
                        velocityTracker.computeCurrentVelocity(1000);
                        float flingVelocity = velocityTracker.getYVelocity(trackedPointerId);
                        snapAnimator = createSnapAnimator(flingVelocity);
                        if (snapAnimator != null) {
                            snapAnimator.start();
                        }
                        dragging = false;
                    }
                    stopTouchTracking();
                    // only consume the UP event if the gesture had a dragging motion. We do not
                    // want to steal normal clicks from our child views
                    return wasDragging;
                }

            case MotionEvent.ACTION_MOVE: {
                if (trackedPointerId == MotionEvent.INVALID_POINTER_ID) {
                    // no drag in progress, ignore event
                    return false;
                }
                velocityTracker.addMovement(event);

                int trackedPointerIndex = event.findPointerIndex(trackedPointerId);
                if (dragging) {
                    float deltaYPixels = event.getY(trackedPointerIndex) - lastDragPoint.y;
                    movePanelRelative(deltaYPixels);
                    lastDragPoint.set(event.getX(trackedPointerIndex), event.getY(trackedPointerIndex));
                    return true;
                } else {
                    float verticalDistance = Math.abs(event.getY(trackedPointerIndex) - touchStart.y);
                    if (verticalDistance > touchSlop) {
                        dragging = true;
                        callback.onDragStarted();
                        lastDragPoint.set(event.getX(trackedPointerIndex), event.getY(trackedPointerIndex));
                        return true;
                    }
                }
                return false;
            }

            default:
                return false;
        }
    }

    private int movePanelRelative(float deltaYPixels) {
        float deltaOffset = -deltaYPixels / callback.getViewVerticalDragRange();
        float newSlideOffset = MathUtils.clamp(mSlideOffset + deltaOffset, 0f, 1f);
        int previousPosition = callback.getSlideableView().getTop();
        setSlideOffset(newSlideOffset);
        return callback.getSlideableView().getTop() - previousPosition;
    }

    void slideTo(float slideOffset) {
        if (snapAnimator != null) {
            snapAnimator.cancel();
        }
        callback.onDragStarted();
        snapAnimator = createAnimator(slideOffset);
        snapAnimator.start();
    }

    float getSlideOffset() {
        return mSlideOffset;
    }

    void setSlideOffset(float slideOffset) {
        mSlideOffset = slideOffset;
        int panelTop = callback.computePanelTopPosition(mSlideOffset);
        callback.getSlideableView().offsetTopAndBottom(panelTop - callback.getSlideableView().getTop());
        callback.onViewPositionChanged(slideOffset);
    }

    private void stopTouchTracking() {
        trackedPointerId = MotionEvent.INVALID_POINTER_ID;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            velocityTracker = null;
        }
    }

    @Nullable
    private ValueAnimator createSnapAnimator(float flingVelocity) {
        boolean flingUp = flingVelocity < 0 && callback.isFling(flingVelocity);
        boolean flingDown = flingVelocity > 0 && callback.isFling(flingVelocity);

        float snapPoint = callback.calculateSnapPoint(mSlideOffset, flingUp, flingDown);

        if (snapPoint == mSlideOffset) {
            callback.onViewSettled(mSlideOffset);
            return null;
        }

        return createAnimator(snapPoint);
    }

    private ValueAnimator createAnimator(float destinationSlideOffset) {
        ValueAnimator snapAnimator = ValueAnimator.ofFloat(mSlideOffset, destinationSlideOffset);
        snapAnimator.setInterpolator(snapInterpolator);
        snapAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                setSlideOffset((Float) animation.getAnimatedValue());
            }
        });
        snapAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                callback.onViewSettled(mSlideOffset);
            }
        });
        return snapAnimator;
    }

    interface Callback {
        View getSlideableView();

        int getViewVerticalDragRange();

        boolean isFling(float velocity);

        boolean isDraggable(float screenX, float screenY);

        /**
         * Called before the panel may start moving due to user interaction or a call to {@link #slideTo(float)}
         */
        void onDragStarted();

        int computePanelTopPosition(float slideOffset);

        /**
         * Called when the panel position changed due to user interaction or programmatically.
         */
        void onViewPositionChanged(float slideOffset);

        float calculateSnapPoint(float slideOffset, boolean flingUp, boolean flingDown);

        /**
         * Called after the view stopped moving due to snapping after a drag or reaching the
         * destination in {@link #slideTo(float)}
         */
        void onViewSettled(float slideOffset);
    }

    private static class DefaultSnapInterpolator implements Interpolator {
        public float getInterpolation(float t) {
            t -= 1.0f;
            return t * t * t * t * t + 1.0f;
        }
    }
}
