package com.kmarlow.parallaxrecyclerviewadapter;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

public abstract class ParallaxHeaderRecyclerAdapter<VH extends RecyclerView.ViewHolder> extends
                RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final float DEFAULT_SCROLL_MULTIPLIER = 0.5f;
    private final SparseArray<Integer> mItemViewTypes = new SparseArray<>();

    private class ViewTypes {
        public static final int NORMAL = 100;
        public static final int HEADER = 2;
        public static final int FIRST_VIEW = 3;
    }

    public interface OnClickEvent {
        /**
         * Event triggered when you click on a item of the adapter
         *
         * @param v
         *            view
         * @param position
         *            position on the array
         */
        void onClick(View v, int position);
    }

    public interface OnParallaxScroll {
        /**
         * Event triggered when the parallax is being scrolled.
         *
         * @param percentage
         * @param offset
         * @param parallax
         */
        void onParallaxScroll(float percentage, float offset, View parallax);
    }

    private final Context mContext;

    private HeaderViewWrapper mHeader;
    private OnClickEvent mOnClickEvent;
    private OnParallaxScroll mParallaxScroll;
    private RecyclerView mRecyclerView;
    private float mScrollMultiplier = DEFAULT_SCROLL_MULTIPLIER;

    protected ParallaxHeaderRecyclerAdapter(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public void setScrollMultiplier(float scrollMultiplier) {
        mScrollMultiplier = scrollMultiplier;
    }

    public void setOnClickEvent(OnClickEvent onClickEvent) {
        mOnClickEvent = onClickEvent;
    }

    public void setOnParallaxScroll(OnParallaxScroll parallaxScroll) {
        mParallaxScroll = parallaxScroll;
        mParallaxScroll.onParallaxScroll(0, 0, mHeader);
    }

    protected abstract void bindItemViewHolder(VH viewHolder, int position);

    protected abstract VH createItemViewHolder(ViewGroup parent, int viewType);

    protected abstract int getCount();

    protected abstract int getViewType(int position);

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ViewTypes.HEADER;
        }
        int itemPosition = position - 1;
        mItemViewTypes.put(itemPosition, getViewType(itemPosition));
        return position == 1 ? ViewTypes.FIRST_VIEW : (ViewTypes.NORMAL + itemPosition);
    }

    @Override
    public int getItemCount() {
        return getCount() + (mHeader == null ? 0 : 1);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        int itemPosition = position - 1;
        if (position != 0 && mHeader != null) {
            bindItemViewHolder((VH) viewHolder, itemPosition);
        } else if (position != 0) {
            bindItemViewHolder((VH) viewHolder, itemPosition);
        }

        if (mOnClickEvent != null) {
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOnClickEvent.onClick(v, position - (mHeader == null ? 0 : 1));
                }
            });
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        if (i == ViewTypes.HEADER && mHeader != null) {
            return new ViewHolder(mHeader);
        }

        if (i == ViewTypes.FIRST_VIEW && mHeader != null && mRecyclerView != null) {
            RecyclerView.ViewHolder holder = mRecyclerView.findViewHolderForPosition(0);
            if (holder != null) {
                translateHeader(-holder.itemView.getTop());
            }
        }

        int itemPosition;
        if (i == ViewTypes.FIRST_VIEW) {
            itemPosition = 0;
        } else {
            itemPosition = i - ViewTypes.NORMAL;
        }

        return createItemViewHolder(viewGroup, mItemViewTypes.get(itemPosition));
    }

    public void translateHeader(float headerTop) {
        float translationAmount = headerTop * mScrollMultiplier;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            mHeader.setTranslationY(translationAmount);
        } else {
            TranslateAnimation anim = new TranslateAnimation(0, 0, translationAmount, translationAmount);
            anim.setFillAfter(true);
            anim.setDuration(0);
            mHeader.startAnimation(anim);
        }
        mHeader.setClipY(Math.round(translationAmount));
        if (mParallaxScroll != null) {
            float left = Math.min(1, ((translationAmount) / (mHeader.getHeight() * mScrollMultiplier)));
            mParallaxScroll.onParallaxScroll(left, headerTop, mHeader);
        }
    }

    public void setParallaxHeader(View header, final RecyclerView view) {
        mRecyclerView = view;
        mHeader = new HeaderViewWrapper(header.getContext());
        mHeader.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        mHeader.addView(header, new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));
        view.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (mHeader != null) {
                    RecyclerView.ViewHolder holder = view.findViewHolderForPosition(0);
                    if (holder != null)
                        translateHeader(-holder.itemView.getTop());
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View itemView) {
            super(itemView);
        }
    }

    static class HeaderViewWrapper extends FrameLayout {
        private int mOffset;

        public HeaderViewWrapper(Context context) {
            super(context);
        }

        @Override
        protected void dispatchDraw(Canvas canvas) {
            canvas.clipRect(new Rect(getLeft(), getTop(), getRight(), getBottom() + mOffset));
            super.dispatchDraw(canvas);
        }

        public void setClipY(int offset) {
            mOffset = offset;
            invalidate();
        }
    }
}
