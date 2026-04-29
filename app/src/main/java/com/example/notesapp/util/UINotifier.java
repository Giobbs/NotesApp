package com.example.notesapp.util;

import android.animation.ObjectAnimator;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

public class UINotifier {

    // =========================
    // SUCCESS
    // =========================
    public static void showSuccess(View root, String msg) {
        showToastLike(root, msg,
                android.R.drawable.checkbox_on_background,
                "#2E7D32");
    }

    // =========================
    // INFO
    // =========================
    public static void showInfo(View root, String msg) {
        showToastLike(root, msg,
                android.R.drawable.ic_dialog_info,
                "#1565C0");
    }

    // =========================
    // ERROR
    // =========================
    public static void showError(View root, String msg) {
        showToastLike(root, msg,
                android.R.drawable.ic_delete,
                "#C62828");
    }

    // =========================
    // UNDO (SAFE GMAIL STYLE)
    // =========================
    public static void showUndo(View root, String msg, String action, Runnable undo) {

        Snackbar snackbar = Snackbar.make(root, "", Snackbar.LENGTH_LONG);

        ViewGroup snackbarView = (ViewGroup) snackbar.getView();

        // IMPORTANT: NON rimuovere figli esistenti
        snackbarView.setPadding(0, 0, 0, 0);

        LinearLayout container = createUndoView(root, msg, action, undo);

        snackbarView.setBackgroundColor(Color.TRANSPARENT);

        snackbarView.removeAllViews();
        snackbarView.addView(container);

        animateIn(container);

        snackbar.setDuration(5000);
        snackbar.show();
    }

    // =========================
    // TOAST LIKE
    // =========================
    private static void showToastLike(View root, String msg, int iconRes, String bgColor) {

        Snackbar snackbar = Snackbar.make(root, "", Snackbar.LENGTH_SHORT);

        ViewGroup snackbarView = (ViewGroup) snackbar.getView();
        snackbarView.setBackgroundColor(Color.TRANSPARENT);
        snackbarView.setPadding(0, 0, 0, 0);

        LinearLayout container = new LinearLayout(root.getContext());
        container.setOrientation(LinearLayout.HORIZONTAL);
        container.setPadding(40, 25, 40, 25);
        container.setGravity(Gravity.CENTER_VERTICAL);
        container.setBackgroundColor(Color.parseColor(bgColor));

        ImageView icon = new ImageView(root.getContext());
        icon.setImageResource(iconRes);
        icon.setPadding(0, 0, 30, 0);

        TextView text = new TextView(root.getContext());
        text.setText(msg);
        text.setTextColor(Color.WHITE);

        container.addView(icon);
        container.addView(text);

        snackbarView.removeAllViews();
        snackbarView.addView(container);

        animateIn(container);

        snackbar.show();
    }

    // =========================
    // UNDO VIEW SAFE
    // =========================
    private static LinearLayout createUndoView(View root, String msg, String action, Runnable undo) {

        LinearLayout container = new LinearLayout(root.getContext());
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(40, 25, 40, 25);
        container.setBackgroundColor(Color.parseColor("#202124"));

        LinearLayout row = new LinearLayout(root.getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        TextView text = new TextView(root.getContext());
        text.setText(msg);
        text.setTextColor(Color.WHITE);
        text.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));

        TextView undoBtn = new TextView(root.getContext());
        undoBtn.setText(action);
        undoBtn.setTextColor(Color.parseColor("#FFCA28"));
        undoBtn.setPadding(25, 10, 25, 10);
        undoBtn.setOnClickListener(v -> {
            if (undo != null) undo.run();
        });

        row.addView(text);
        row.addView(undoBtn);

        View progress = new View(root.getContext());
        progress.setBackgroundColor(Color.parseColor("#FFCA28"));

        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        5
                );

        progress.setLayoutParams(lp);

        ObjectAnimator animator = ObjectAnimator.ofFloat(progress, "scaleX", 1f, 0f);
        animator.setDuration(5000);
        animator.start();

        container.addView(row);
        container.addView(progress);

        return container;
    }

    // =========================
    // ANIMATION
    // =========================
    private static void animateIn(View v) {
        v.setAlpha(0f);
        v.setTranslationY(40f);

        v.animate()
                .alpha(1f)
                .alpha(1f)
                .translationY(0f)
                .setDuration(200)
                .start();
    }
}