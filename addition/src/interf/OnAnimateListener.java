package badtzmarupekkle.littlethings.interf;

import com.nineoldandroids.animation.Animator;

public interface OnAnimateListener {
    public Animator getAnimator();

    public void nullifySet();

    public void onAnimationEnd();

    public void onAnimationStart();
}
