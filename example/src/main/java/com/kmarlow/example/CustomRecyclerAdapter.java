package com.kmarlow.example;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kmarlow.parallaxrecyclerviewadapter.ParallaxHeaderRecyclerAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CustomRecyclerAdapter extends ParallaxHeaderRecyclerAdapter<CustomRecyclerAdapter.CustomViewHolder> {

    private static final List<String> EXAMPLE_ARRAY;

    static {
        List<String> exampleArray = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            exampleArray.add("This");
            exampleArray.add("Is");
            exampleArray.add("A");
            exampleArray.add("Test");
            exampleArray.add("List");
        }
        EXAMPLE_ARRAY = Collections.unmodifiableList(exampleArray);
    }

    protected CustomRecyclerAdapter(Context context) {
        super(context);
    }

    @Override
    protected void bindItemViewHolder(CustomViewHolder viewHolder, int position) {
        viewHolder.mTextView.setText(EXAMPLE_ARRAY.get(position));
    }

    @Override
    protected CustomViewHolder createItemViewHolder(ViewGroup viewGroup, int i) {
        final View view = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, viewGroup,
                        false);
        return new CustomViewHolder(view);
    }

    @Override
    protected int getCount() {
        return EXAMPLE_ARRAY.size();
    }

    @Override
    protected int getViewType(int position) {
        return 0;
    }

    static class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public CustomViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }
}
