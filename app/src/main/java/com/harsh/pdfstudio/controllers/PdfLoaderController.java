package com.harsh.pdfstudio.controllers;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.harsh.pdfstudio.R;

public class PdfLoaderController {

    private final ImageView loaderView;   // e.g. loader_ring_animated or loader_page_animated
    private final View[] dots;            // optional: staggered views — dots OR page-stack rects
    private final int staggerAnimatorRes; // R.animator.loader_dot_pulse or R.animator.loader_page_stack_bounce

    /** Default constructor — uses the pulsing-dot animator for staggered views. */
    public PdfLoaderController(ImageView loaderView, View... dots) {
        this(loaderView, R.animator.loader_dot_pulse, dots);
    }

    /** Use this constructor to drive the page-stack bounce instead of dots:
     *  new PdfLoaderController(loaderRing, R.animator.loader_page_stack_bounce, page1, page2, page3) */
    public PdfLoaderController(ImageView loaderView, int staggerAnimatorRes, View... dots) {
        this.loaderView = loaderView;
        this.staggerAnimatorRes = staggerAnimatorRes;
        this.dots = dots;
    }

    public  PdfLoaderController(int staggerAnimatorRes , View...  dots){
        this.staggerAnimatorRes = staggerAnimatorRes;
        this.dots = dots;
        this.loaderView =  null;
    }

    /** Makes the loader visible and starts its animation. */
    public void showLoader() {


        if (loaderView != null) {

            loaderView.setVisibility(View.VISIBLE);

            Drawable drawable = loaderView.getDrawable();

            if (drawable instanceof Animatable) {
                ((Animatable) drawable).start();
            }
        }

        if (dots != null) {

            long[] delays = {0L, 150L, 300L};

            for (int i = 0; i < dots.length; i++) {

                final View dot = dots[i];

                if (dot == null) continue;

                dot.setVisibility(View.VISIBLE);

                dot.postDelayed(() -> {

                    Animator animator = AnimatorInflater.loadAnimator(
                            dot.getContext(), staggerAnimatorRes
                    );

                    animator.setTarget(dot);
                    animator.start();

                    }, delays[i]);
            }
        }
    }

    /** Stops the animation and hides the loader. */
    public void hideLoader() {

        if (loaderView != null ) {

            Drawable drawable = loaderView.getDrawable();
            if (drawable instanceof Animatable) {
                ((Animatable) drawable).stop();
            }
            loaderView.setVisibility(View.GONE);

        }

        if (dots != null) {
            for (View dot : dots) {
                if (dot != null) dot.setVisibility(View.GONE);
            }
        }
    }


}

