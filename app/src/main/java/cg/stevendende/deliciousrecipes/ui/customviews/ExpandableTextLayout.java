/*
 * Copyright (C) 2017 Steve NDENDE, www.github.com/steve111MV
 *
 * Inspired by (copied from):
 * xiaxveliang@163.com on [2016.09.21}
 * https://github.com/xiaxveliang/Android_ExpandableTextView
 */

package cg.stevendende.deliciousrecipes.ui.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import cg.stevendende.deliciousrecipes.R;

/**
 * Allows to expand AND collapse a Child {@link TextView}
 * according to a defined maxLines(an attribute) value
 */
public class ExpandableTextLayout extends LinearLayout {

    private int mMaxLines = 0;
    private int mDefaultTextSize = 0;
    private boolean isTextChange = false;
    private String txtCollapse;
    private String txtExpand;

    /**
     * UI
     */
    private TextView mMoreTextview;
    private TextView mShowTextview;

    private boolean isExpanded = false;

    private OnExpandStateChangeListener listener;

    public ExpandableTextLayout(Context context) {
        this(context, null);
    }

    public ExpandableTextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttributeSet(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initUI();
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (getVisibility() == GONE || !isTextChange) {
            return;
        }
        isTextChange = false;

        mMoreTextview.setVisibility(GONE);
        mShowTextview.setMaxLines(Integer.MAX_VALUE);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mShowTextview.getLineCount() <= mMaxLines) {
            //As for my linear container in review item,
            // it's better to set visibity to GONE from the 2nd line
            if (mShowTextview.getLineCount() == 3 && ((int) mShowTextview.getTextSize()) == mDefaultTextSize) {
                mShowTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDefaultTextSize + mDefaultTextSize / 6);
            } else if (mShowTextview.getLineCount() == 2 && ((int) mShowTextview.getTextSize()) == mDefaultTextSize) {
                mShowTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDefaultTextSize * 1.5f);
                //Log.e("TextSize :", mShowTextview.getTextSize()+" lines=2  -GONE_expand-  lines:"+mShowTextview.getLineCount());
            } else if (mShowTextview.getLineCount() == 1 && ((int) mShowTextview.getTextSize()) == mDefaultTextSize) {
                mShowTextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, mDefaultTextSize * 2);
                //Log.e("TextSize :", mShowTextview.getTextSize()+" lines=1   -INVISIBLE_expand- "+mShowTextview.getLineCount());
            }

            return;
        }
        mMoreTextview.setVisibility(VISIBLE);

        if (isExpanded == false) {
            mShowTextview.setMaxLines(mMaxLines);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }


    /**
     * AttributeSet
     *
     * @param context
     * @param attrs
     */
    private void initAttributeSet(Context context, AttributeSet attrs) {
        setOrientation(VERTICAL);
        //
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.ExpandableTextViewAttr);
        mMaxLines = array.getInteger(R.styleable.ExpandableTextViewAttr_ExpandableMaxLines, 3);
        array.recycle();

        //Textes
        txtExpand = context.getString(R.string.review_card_expand);
        txtCollapse = context.getString(R.string.review_card_collapse);
    }

    private void initUI() {
        mShowTextview = (TextView) findViewById(R.id.expandable_id_show_textview);
        mMoreTextview = (TextView) findViewById(R.id.expandable_id_more_textview);
        mDefaultTextSize = (int) mShowTextview.getTextSize();

        mMoreTextview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isExpanded = !isExpanded;
                if (isExpanded == false) {
                    collapse();
                } else {
                    expand();
                }
            }
        });
    }


    /**
     * @param text
     */
    public void setText(String text) {
        isTextChange = true;
        mShowTextview.setText(text);
    }

    public void setText(SpannableString textSpanable) {
        isTextChange = true;
        mShowTextview.setText(textSpanable);
    }

    public void collapse() {
        mMoreTextview.setText(txtExpand);
        if (listener != null) {
            listener.onExpandStateChanged(isExpanded);
        }
        mShowTextview.setMaxLines(mMaxLines);

        //Tempo customization to alow scroll on collapse
        mShowTextview.setText(mShowTextview.getText());
    }

    public void expand() {
        if (isExpanded) {
            mMoreTextview.setText(txtCollapse);
            if (listener != null) {
                listener.onExpandStateChanged(isExpanded);
            }
            mShowTextview.setMaxLines(Integer.MAX_VALUE);
        }
    }

    public void setTextMaxLines(int maxLines) {
        mMaxLines = maxLines;
        mShowTextview.setMaxLines(maxLines);
    }

    /**
     * @param text
     * @param isExpanded
     */
    public void setText(String text, boolean isExpanded) {
        this.isExpanded = isExpanded;
        if (!isExpanded) {
            mMoreTextview.setText(txtExpand);
        } else {
            mMoreTextview.setText(txtCollapse);
        }
        setText(text);
    }

    /**
     * 状态变化的回调
     *
     * @param listener
     */
    public void setListener(OnExpandStateChangeListener listener) {
        this.listener = listener;
    }

    public interface OnExpandStateChangeListener {
        void onExpandStateChanged(boolean isExpanded);
    }
}
