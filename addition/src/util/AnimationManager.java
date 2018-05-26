package badtzmarupekkle.littlethings.util;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.TransitionDrawable;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import badtzmarupekkle.littlethings.interf.OnAnimateListener;

public class AnimationManager {

    private static final long DURATION_SCALE = 400;
    private static final long DURATION_TRANSLATION = 400;

    private static final String ALPHA = "alpha";
    private static final String ROTATION = "rotation";
    private static final String SCALE_X = "scaleX";
    private static final String SCALE_Y = "scaleY";
    private static final String TRANSLATION_X = "translationX";
    private static final String TRANSLATION_Y = "translationY";
    private static final String X = "x";
    private static final String Y = "y";

    public static enum Type {
        VIEW_PHOTOS, VIEW_POSTS;
    }

    public static void calculateScaleImageUpBounds(final AnimationManagerInput input) {
        input.landscape = input.image.getWidth() > input.image.getHeight();

        if (input.landscape)
            input.image = ImageManager.rotateBitmap(input.image, 90f);

        int[] location = new int[2];
        input.thumbnailView.getLocationOnScreen(location);

        input.startBounds = new Rect(location[0], location[1], location[0] + input.thumbnailView.getWidth(), location[1] + input.thumbnailView.getHeight());
        input.endBounds = new Rect();
        input.globalOffset = new Point();

        input.containerView.getGlobalVisibleRect(input.endBounds, input.globalOffset);
        input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);
        input.endBounds.offset(-input.globalOffset.x, -input.globalOffset.y);


        float imageWidth = input.image.getWidth();
        float imageHeight = input.image.getHeight();

        if ((float) input.endBounds.width() / input.endBounds.height() > imageWidth / imageHeight) {
            float width = input.endBounds.height() * imageWidth / imageHeight;
            float delta = (input.endBounds.width() - width) / 2;
            input.endBounds.left += delta;
            input.endBounds.right -= delta;
        } else {
            float height = input.endBounds.width() * imageHeight / imageWidth;
            float delta = (input.endBounds.height() - height) / 2;
            input.endBounds.bottom -= delta;
            input.endBounds.top += delta;
        }

        if (input.type == Type.VIEW_POSTS) {
            float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, input.imageView.getResources().getDisplayMetrics());
            input.startBounds.bottom -= px;
            input.startBounds.left += px;
            input.startBounds.right -= px;
            input.startBounds.top += px;

            if (input.landscape) {
                input.scaleX = (float) input.startBounds.height() / input.endBounds.width();
                input.scaleY = (float) input.startBounds.width() / input.endBounds.height();
            } else {
                input.scaleX = (float) input.startBounds.width() / input.endBounds.width();
                input.scaleY = (float) input.startBounds.height() / input.endBounds.height();
            }
        } else {
            if ((float) input.endBounds.width() / input.endBounds.height() > (float) input.startBounds.width() / input.startBounds.height()) {
                input.scaleX = input.scaleY = (float) input.startBounds.height() / input.endBounds.height();
                float startWidth = input.scaleX * input.endBounds.width();
                float deltaWidth = (startWidth - input.startBounds.width()) / 2;
                input.startBounds.left -= deltaWidth;
                input.startBounds.right += deltaWidth;
            } else {
                //TODO: SCALE BACK TO CENTER CROP
                input.scaleX = input.scaleY = (float) input.startBounds.width() / input.endBounds.width();
                float startHeight = input.scaleX * input.endBounds.height();
                float deltaHeight = (startHeight - input.startBounds.height()) / 2;
                input.startBounds.bottom += deltaHeight;
                input.startBounds.top -= deltaHeight;
            }
        }
    }

    public static Animator scaleImageUp(final AnimationManagerInput input) {
        if (input.image == null)
            return null;

        calculateScaleImageUpBounds(input);

        if (input.getAnimator() != null)
            input.getAnimator().cancel();

        ViewHelper.setPivotX(input.imageView, 0f);
        ViewHelper.setPivotY(input.imageView, 0f);

        final AnimatorSet set = new AnimatorSet();
        AnimatorSet preAnimation = new AnimatorSet();
        AnimatorSet animation = new AnimatorSet();
        if (input.landscape) {
            preAnimation.play(ObjectAnimator.ofFloat(input.imageView, X, input.startBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.startBounds.bottom))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, input.scaleX))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, input.scaleY))
                    .with(ObjectAnimator.ofFloat(input.imageView, ROTATION, -90f))
                    .with(ObjectAnimator.ofFloat(input.imageView, ALPHA, 1f));

            animation.play(ObjectAnimator.ofFloat(input.imageView, X, input.endBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.endBounds.top))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, 1f))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, 1f))
                    .with(ObjectAnimator.ofFloat(input.imageView, ROTATION, 0f))
                    .with(ObjectAnimator.ofFloat(input.thumbnailView, ALPHA, 0f));
        } else {
            preAnimation.play(ObjectAnimator.ofFloat(input.imageView, X, input.startBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.startBounds.top))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, input.scaleX))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, input.scaleY))
                    .with(ObjectAnimator.ofFloat(input.imageView, ALPHA, 1f));

            animation.play(ObjectAnimator.ofFloat(input.imageView, X, input.endBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.endBounds.top))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, 1f))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, 1f))
                    .with(ObjectAnimator.ofFloat(input.thumbnailView, ALPHA, 0f));
        }
        preAnimation.setDuration(0);
        preAnimation.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                ViewHelper.setAlpha(input.imageView, 0f);
            }
        });

        animation.setDuration(DURATION_SCALE);
        animation.setInterpolator(new DecelerateInterpolator());

        set.playSequentially(preAnimation, animation);
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.imageView.setImageBitmap(input.image);
                input.imageView.setVisibility(View.VISIBLE);
                if (input.tDrawable != null)
                    input.tDrawable.startTransition((int) DURATION_SCALE);
                input.onAnimationStart();
            }
        });

        return set;
    }

    public static Animator scaleImageUpReverse(final AnimationManagerInput input) {
        if (input.type == Type.VIEW_PHOTOS) {
            input.thumbnailView.getGlobalVisibleRect(input.startBounds);
            input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);
        }

        AnimatorSet set = new AnimatorSet();
        if (input.landscape) {
            set.play(ObjectAnimator.ofFloat(input.imageView, X, input.startBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.startBounds.bottom))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, input.scaleX))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, input.scaleY))
                    .with(ObjectAnimator.ofFloat(input.imageView, ROTATION, 0f, -90f));
        } else {
            set.play(ObjectAnimator.ofFloat(input.imageView, X, input.startBounds.left))
                    .with(ObjectAnimator.ofFloat(input.imageView, Y, input.startBounds.top))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_X, input.scaleX))
                    .with(ObjectAnimator.ofFloat(input.imageView, SCALE_Y, input.scaleY));
        }
        set.setDuration(DURATION_SCALE);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.thumbnailView.clearAnimation();
                input.imageView.clearAnimation();
                input.imageView.setImageBitmap(null);
                input.imageView.setVisibility(View.GONE);
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.thumbnailView.clearAnimation();
                input.imageView.clearAnimation();
                input.imageView.setImageBitmap(null);
                input.imageView.setVisibility(View.GONE);
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (input.tDrawable != null)
                    input.tDrawable.reverseTransition((int) DURATION_SCALE);
                input.onAnimationStart();
            }
        });

        return set;
    }

    public static Animator scaleViewUp(final AnimationManagerInput input) {
        input.startBounds = new Rect();
        input.endBounds = new Rect();
        input.globalOffset = new Point();

        input.view.getGlobalVisibleRect(input.startBounds);
        input.containerView.getGlobalVisibleRect(input.endBounds, input.globalOffset);

        input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);
        input.endBounds.offset(-input.globalOffset.x, -input.globalOffset.y);

        input.scaleX = input.endBounds.width() / input.startBounds.width();
        input.scaleY = input.endBounds.height() / input.startBounds.height();

        ViewHelper.setPivotX(input.view, 0f);
        ViewHelper.setPivotY(input.view, 0f);

        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(input.view, X, input.startBounds.left, input.endBounds.left))
                .with(ObjectAnimator.ofFloat(input.view, Y, input.startBounds.top, input.endBounds.top))
                .with(ObjectAnimator.ofFloat(input.view, SCALE_X, input.scaleX))
                .with(ObjectAnimator.ofFloat(input.view, SCALE_Y, input.scaleY));
        set.setDuration(DURATION_SCALE);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.onAnimationStart();
            }
        });

        return set;
    }

    public static Animator scaleViewUpReverse(final AnimationManagerInput input) {
        AnimatorSet set = new AnimatorSet();
        set.play(ObjectAnimator.ofFloat(input.view, X, input.startBounds.left))
                .with(ObjectAnimator.ofFloat(input.view, Y, input.startBounds.top))
                .with(ObjectAnimator.ofFloat(input.view, SCALE_X, 1f))
                .with(ObjectAnimator.ofFloat(input.view, SCALE_Y, 1f));
        set.setDuration(DURATION_SCALE);
        set.setInterpolator(new DecelerateInterpolator());
        set.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.onAnimationStart();
            }
        });

        return set;
    }

    public static Animator translateDown(final AnimationManagerInput input) {
        input.startBounds = new Rect();
        input.globalOffset = new Point();

        input.view.getGlobalVisibleRect(input.startBounds, input.globalOffset);
        input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);

        ObjectAnimator animator = ObjectAnimator.ofFloat(input.view, TRANSLATION_Y, input.startBounds.top, input.startBounds.bottom);
        animator.setDuration(DURATION_TRANSLATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.setVisibility(View.GONE);
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animator) {
                input.onAnimationStart();
            }
        });

        return animator;
    }

    public static Animator translateDownReverse(final AnimationManagerInput input) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(input.view, TRANSLATION_Y, input.startBounds.bottom, input.startBounds.top);
        animator.setDuration(DURATION_TRANSLATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.setVisibility(View.GONE);
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.view.setVisibility(View.VISIBLE);
                input.onAnimationStart();
            }
        });

        return animator;
    }

    public static Animator translateLeft(final AnimationManagerInput input) {
        input.startBounds = new Rect();
        input.globalOffset = new Point();

        input.view.getGlobalVisibleRect(input.startBounds, input.globalOffset);
        input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);

        ObjectAnimator animator = ObjectAnimator.ofFloat(input.view, TRANSLATION_X, input.startBounds.right, input.startBounds.left);
        animator.setDuration(DURATION_TRANSLATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.setVisibility(View.GONE);
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.onAnimationStart();
            }
        });

        return animator;
    }

    public static Animator translateUp(final AnimationManagerInput input) {
        input.startBounds = new Rect();
        input.globalOffset = new Point();

        input.view.getGlobalVisibleRect(input.startBounds, input.globalOffset);
        input.startBounds.offset(-input.globalOffset.x, -input.globalOffset.y);

        ObjectAnimator animator = ObjectAnimator.ofFloat(input.view, TRANSLATION_Y, input.startBounds.top, input.startBounds.top - input.startBounds.bottom);
        animator.setDuration(DURATION_TRANSLATION);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.setVisibility(View.GONE);
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.onAnimationStart();
            }
        });

        return animator;
    }

    public static Animator translateUpReverse(final AnimationManagerInput input) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(input.view, TRANSLATION_Y, input.startBounds.top - input.startBounds.bottom, input.startBounds.top);
        animator.setDuration(DURATION_TRANSLATION);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                input.view.setVisibility(View.GONE);
                input.view.clearAnimation();
                input.nullifySet();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                input.view.clearAnimation();
                input.nullifySet();
                input.onAnimationEnd();
            }

            @Override
            public void onAnimationStart(Animator animation) {
                input.view.setVisibility(View.VISIBLE);
                input.onAnimationStart();
            }
        });

        return animator;
    }

    public static class AnimationManagerInput implements OnAnimateListener {
        public boolean landscape;

        public float scaleX;
        public float scaleY;

        public Bitmap image;
        public ImageView imageView;
        public ImageView thumbnailView;
        public Point globalOffset;
        public Rect endBounds;
        public Rect startBounds;
        public TransitionDrawable tDrawable;
        public Type type;
        public View containerView;
        public View view;

        private OnAnimateListener listener;

        public AnimationManagerInput(OnAnimateListener listener) {
            this.listener = listener;
        }

        @Override
        public Animator getAnimator() {
            return listener.getAnimator();
        }

        @Override
        public void nullifySet() {
            listener.nullifySet();
        }

        @Override
        public void onAnimationEnd() {
            listener.onAnimationEnd();
        }

        @Override
        public void onAnimationStart() {
            listener.onAnimationStart();
        }
    }
}
