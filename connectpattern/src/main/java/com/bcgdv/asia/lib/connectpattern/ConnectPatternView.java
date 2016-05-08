package com.bcgdv.asia.lib.connectpattern;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by magdamyka on 2/05/2016.
 */
public class ConnectPatternView extends View {

    private final long ANIMATION_DURATION = 300;
    private final int ANIMATION_TYPE_NONE = 0;
    private final int ANIMATION_TYPE_MIDDLE = 1;
    private final int ANIMATION_TYPE_BOTTOM = 2;
    private int numbersOfConnectors = 9;
    private int circleColor = Color.BLACK;
    private int lineColor = Color.LTGRAY;
    private int lineWidth = 7; //in dp
    private int radius = 14; //in dp
    private int diameter = 28; //in dp
    private int dp48 = 48; //in dp
    private int animationType = ANIMATION_TYPE_MIDDLE;
    private Drawable drawable;

    private int leftX;
    private int topY;
    private int rightX;
    private int bottomY;
    private int centerX;
    private int centerY;

    private Paint pCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint pLine = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Rect touchPoint = new Rect();

    /**
     * Array of all possible circles to draw
     */
    private Rect[] circles = new Rect[9];

    /**
     * Array of indexes of circles based on the number of circles that were chosen
     */
    private int[] indexes;

    /**
     * Array of indexes of circles in order they were connected
     */
    private ArrayList<Integer> connectionOrder = new ArrayList<>();

    private OnConnectPatternListener mPatternListener;

    /**
     * Constructor for ConnectPatternView
     *
     * @param context Context
     */
    public ConnectPatternView(Context context) {
        super(context);
        init(context, null);
    }

    /**
     * Constructor for ConnectPatternView
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    public ConnectPatternView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    /**
     * Constructor for ConnectPatternView
     *
     * @param context      Context
     * @param attrs        AttributeSet
     * @param defStyleAttr Style
     */
    public ConnectPatternView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ConnectPatternView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    /**
     * Initialize the ConnectPatternView widget
     *
     * @param context Context
     * @param attrs   AttributeSet
     */
    private void init(Context context, AttributeSet attrs) {
        float multi = context.getResources().getDisplayMetrics().density;
        lineWidth *= multi;
        radius *= multi;
        diameter *= multi;
        dp48 *= multi;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ConnectPatternView);
        try {
            numbersOfConnectors = typedArray.getInt(R.styleable.ConnectPatternView_connectPatternNumber, numbersOfConnectors);
            circleColor = typedArray.getColor(R.styleable.ConnectPatternView_connectPatternCircleColor, circleColor);
            radius = (int) typedArray.getDimension(R.styleable.ConnectPatternView_connectPatternCircleRadius, radius);
            diameter = radius * 2;
            lineColor = typedArray.getColor(R.styleable.ConnectPatternView_connectPatternLineColor, lineColor);
            lineWidth = (int) typedArray.getDimension(R.styleable.ConnectPatternView_connectPatternLineWidth, lineWidth);
            drawable = typedArray.getDrawable(R.styleable.ConnectPatternView_connectPatternDrawable);
            animationType = typedArray.getInt(R.styleable.ConnectPatternView_connectPatternAnimationType, animationType);
        } finally {
            typedArray.recycle();
        }

        switch (numbersOfConnectors) {
            case 2:
                indexes = new int[]{0, 2};
                break;
            case 3:
                indexes = new int[]{0, 2, 4};
                break;
            case 5:
                indexes = new int[]{0, 2, 4, 6, 8};
                break;
            case 9:
                indexes = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8};
                break;
            default:
                break;
        }

        pCircle.setColor(circleColor);
        pLine.setColor(lineColor);
        pLine.setStrokeWidth(lineWidth);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        leftX = getPaddingLeft();
        topY = getPaddingTop();
        rightX = w - getPaddingRight();
        bottomY = h - getPaddingBottom();

        centerX = leftX + (rightX - leftX) / 2;
        centerY = topY + (bottomY - topY) / 2;

        setupCircles();
    }

    /**
     * Setup circles on a screen
     */
    private void setupCircles() {
        circles[0] = new Rect(leftX, topY, leftX + diameter, topY + diameter);
        circles[1] = new Rect(centerX - radius, topY, centerX + radius, topY + diameter);
        circles[2] = new Rect(rightX - diameter, topY, rightX, topY + diameter);
        circles[3] = new Rect(leftX, centerY - radius, leftX + diameter, centerY + radius);
        circles[4] = new Rect(centerX - radius, centerY - radius, centerX + radius, centerY + radius);
        circles[5] = new Rect(rightX - diameter, centerY - radius, rightX, centerY + radius);
        circles[6] = new Rect(leftX, bottomY - diameter, leftX + diameter, bottomY);
        circles[7] = new Rect(centerX - radius, bottomY - diameter, centerX + radius, bottomY);
        circles[8] = new Rect(rightX - diameter, bottomY - diameter, rightX, bottomY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLines(canvas);
        drawLineToTouchPoint(canvas);
        drawCircles(canvas);
    }

    /**
     * Setup circles on a screen
     */
    private void drawLineToTouchPoint(Canvas canvas) {
        if (touchPoint.height() > 0 && touchPoint.width() > 0) {
            drawLine(canvas, circles[connectionOrder.get(connectionOrder.size() - 1)], touchPoint);
        }
    }

    /**
     * Draw lines between the connectors
     *
     * @param canvas
     */
    private void drawLines(Canvas canvas) {
        for (int i = 0; i < connectionOrder.size() - 1; i++) {
            drawLine(canvas, circles[connectionOrder.get(i)], circles[connectionOrder.get(i + 1)]);
        }
    }

    /**
     * Draw the line between the touch point and previous point
     *
     * @param canvas
     */
    private void drawCircles(Canvas canvas) {
        for (int i = 0; i < indexes.length; i++) {
            int circleNumber = indexes[i];
            if (drawable == null) {
                canvas.drawCircle(circles[circleNumber].centerX(), circles[circleNumber].centerY(), radius, pCircle);
            } else {
                drawable.setBounds(circles[circleNumber]);
                drawable.draw(canvas);
            }
        }
    }

    /**
     * Draw line between two Rect objects
     *
     * @param canvas
     * @param start
     * @param end
     */
    private void drawLine(Canvas canvas, Rect start, Rect end) {
        canvas.drawLine(start.centerX(), start.centerY(), end.centerX(), end.centerY(), pLine);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                setTouchPoint(event);
                for (int i = 0; i < indexes.length; i++) {
                    int circleNumber = indexes[i];
                    if (touchPoint.intersect(circles[circleNumber])) {
                        connectionOrder.add(circleNumber);
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                setTouchPoint(event);
                for (int i = 0; i < indexes.length; i++) {
                    int circleNumber = indexes[i];
                    if (touchPoint.intersect(circles[circleNumber])) {
                        if (!connectionOrder.contains(circleNumber)) {
                            connectionOrder.add(circleNumber);
                        }
                    }
                }
                invalidate();
                return true;

            case MotionEvent.ACTION_UP:
                setEnabled(false);
                touchPoint.left = 0;
                touchPoint.right = 0;
                touchPoint.top = 0;
                touchPoint.bottom = 0;

                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setEnabled(true);
                        if (mPatternListener != null) {
                            if (connectionOrder.size() > 1) {
                                mPatternListener.onPatternEntered(connectionOrder);
                            } else {
                                mPatternListener.onPatternAbandoned();
                            }
                        }
                        connectionOrder.clear();
                        invalidate();
                    }
                }, ANIMATION_DURATION);

                return true;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return false;
    }

    /**
     * Set the position of the touch point
     *
     * @param event
     */
    private void setTouchPoint(MotionEvent event) {
        touchPoint.left = (int) event.getX();
        touchPoint.top = (int) event.getY();
        touchPoint.bottom = touchPoint.top + dp48;
        touchPoint.right = touchPoint.left + dp48;
    }

    /**
     * Animate the widget in
     */
    public void animateIn() {
        animateIn(0);
    }

    public void animateIn(final long delay) {
        if (!isEnabled()) {
            return;
        }
        if (getVisibility() == GONE) {
            setVisibility(INVISIBLE);
        }
        if (circles[0] == null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateIn(delay);
                }
            }, ANIMATION_DURATION);
            return;
        }

        List<Animator> animators = (animationType == ANIMATION_TYPE_MIDDLE)?
                                            animateInFromMiddle() : animateInFromBottom();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(animationType == ANIMATION_TYPE_NONE? 0 : ANIMATION_DURATION);
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
                setVisibility(VISIBLE);
                if (mPatternListener != null) {
                    mPatternListener.animateInStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(true);
                if (mPatternListener != null) {
                    mPatternListener.animateInEnd();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
        set.setStartDelay(delay);
        set.start();
    }

    private List<Animator> animateInFromMiddle() {
        List<Animator> animators = new ArrayList<>();

        for (int i = 0; i < indexes.length; i++) {
            int circleNumber = indexes[i];
            ValueAnimator leftAnim = ValueAnimator.ofInt(circles[4].left, circles[circleNumber].left);
            leftAnim.setInterpolator(new OvershootInterpolator());
            leftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int left = (Integer) animation.getAnimatedValue();
                    circle.left = left;
                    circle.right = left + diameter;
                }
                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(leftAnim);

            ValueAnimator topAnim = ValueAnimator.ofInt(circles[4].top, circles[circleNumber].top);
            topAnim.setInterpolator(new OvershootInterpolator());
            topAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;
                public void onAnimationUpdate(ValueAnimator animation) {
                    int top = (Integer) animation.getAnimatedValue();
                    circle.top = top;
                    circle.bottom = top + diameter;
                    invalidate();
                }
                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(topAnim);
        }
        return animators;
    }

    private List<Animator> animateInFromBottom() {
        List<Animator> animators = new ArrayList<>();
        int height = getHeight();
        if (height <= 0) {
            height = getResources().getDisplayMetrics().heightPixels;
        }
        for (int i = 0; i < indexes.length; i++) {
            int circleNumber = indexes[i];
            int originalTop = circles[circleNumber].top;
            circles[circleNumber].top = height;
            circles[circleNumber].bottom = height + diameter;
            ValueAnimator topAnim = ValueAnimator.ofInt(height, originalTop);
            topAnim.setInterpolator(new DecelerateInterpolator());
            topAnim.setStartDelay((ANIMATION_DURATION / indexes.length) * (i%3));
            topAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;
                public void onAnimationUpdate(ValueAnimator animation) {
                    int top = (Integer) animation.getAnimatedValue();
                    circle.top = top;
                    circle.bottom = top + diameter;
                    invalidate();
                }
                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(topAnim);
        }
        invalidate();
        return animators;
    }

    /**
     * Animate the widget out
     */
    public void animateOut() {
        animateOut(0);
    }

    public void animateOut(final long delay) {
        if (!isEnabled()) {
            return;
        }
        if (circles[0] == null) {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateOut(delay);
                }
            }, ANIMATION_DURATION);
            return;
        }

        List<Animator> animators = (animationType == ANIMATION_TYPE_MIDDLE)?
            animateOutToMiddle() : animateOutToBottom();
        AnimatorSet set = new AnimatorSet();
        set.playTogether(animators);
        set.setDuration(animationType == ANIMATION_TYPE_NONE? 0 : ANIMATION_DURATION);
        set.setStartDelay(delay);
        set.start();
        set.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                setEnabled(false);
                if (mPatternListener != null) {
                    mPatternListener.animateOutStart();
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                setEnabled(true);
                if (mPatternListener != null) {
                    mPatternListener.animateOutEnd();
                }
                setupCircles();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                onAnimationEnd(animation);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private List<Animator> animateOutToBottom() {
        List<Animator> animators = new ArrayList<>();
        int height = getHeight();
        if (height <= 0) {
            height = getResources().getDisplayMetrics().heightPixels;
        }
        int totalLength = indexes.length;
        for (int i = 0; i < totalLength; i++) {
            int circleNumber = indexes[i];
            ValueAnimator topAnim = ValueAnimator.ofInt(circles[circleNumber].top, height);
            topAnim.setInterpolator(new AccelerateInterpolator());
            if (animationType != ANIMATION_TYPE_NONE) {
                topAnim.setStartDelay((ANIMATION_DURATION / indexes.length) * (i%3));
            }
            topAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;
                public void onAnimationUpdate(ValueAnimator animation) {
                    int top = (Integer) animation.getAnimatedValue();
                    circle.top = top;
                    circle.bottom = top + diameter;
                    invalidate();
                }
                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(topAnim);
        }
        return animators;
    }

    private List<Animator> animateOutToMiddle() {
        List<Animator> animators = new ArrayList<>();
        for (int i = 0; i < indexes.length; i++) {
            final int circleNumber = indexes[i];
            ValueAnimator leftAnim = ValueAnimator.ofInt(circles[circleNumber].left, circles[4].left);
            leftAnim.setInterpolator(new DecelerateInterpolator());
            if (animationType != ANIMATION_TYPE_NONE) {
                leftAnim.setStartDelay((ANIMATION_DURATION / indexes.length) * i);
            }
            leftAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;

                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int left = (Integer) animation.getAnimatedValue();
                    circle.left = left;
                    circle.right = left + diameter;
                }

                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(leftAnim);

            ValueAnimator topAnim = ValueAnimator.ofInt(circles[circleNumber].top, circles[4].top);
            topAnim.setInterpolator(new DecelerateInterpolator());
            if (animationType != ANIMATION_TYPE_NONE) {
                topAnim.setStartDelay((ANIMATION_DURATION / indexes.length) * i);
            }
            topAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                Rect circle = null;
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int top = (Integer) animation.getAnimatedValue();
                    circle.top = top;
                    circle.bottom = top + diameter;
                    invalidate();
                }
                public ValueAnimator.AnimatorUpdateListener init(Rect rect) {
                    circle = rect;
                    return this;
                }
            }.init(circles[circleNumber]));
            animators.add(topAnim);
        }
        return animators;
    }

    /**
     * An interface for the animation events of the widget
     */
    public interface OnConnectPatternListener {
        void onPatternEntered(ArrayList<Integer> result);
        void onPatternAbandoned();
        void animateInStart();
        void animateInEnd();
        void animateOutStart();
        void animateOutEnd();
    }

    /**
     * Set a callback when animations starts/ends for the widget
     *
     * @param l OnConnectPatternListener
     */
    public void setOnConnectPatternListener(OnConnectPatternListener l) {
        mPatternListener = l;
    }
}