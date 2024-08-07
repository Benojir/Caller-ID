package zorro.dimyon.calleridentity.helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.RelativeLayout;

public class SwipeDismissLayout extends RelativeLayout {
    private float startX = 0f;
    private float dX = 0f;
    private OnDismissListener dismissListener;

    public interface OnDismissListener {
        void onDismiss();
    }

    public SwipeDismissLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnDismissListener(OnDismissListener listener) {
        this.dismissListener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getRawX();
                break;
            case MotionEvent.ACTION_MOVE:
                dX = event.getRawX() - startX;
                setTranslationX(dX);
                break;
            case MotionEvent.ACTION_UP:
                if (Math.abs(dX) > (float) getWidth() / 4) {
                    if (dismissListener != null) {
                        dismissListener.onDismiss();
                    }
                } else {
                    animate().translationX(0f).setDuration(100).start();
                }
                break;
        }
        return true;
    }
}
