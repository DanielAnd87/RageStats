package com.example.danielandersson.ragestats.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.danielandersson.ragestats.Data.Comment;
import com.example.danielandersson.ragestats.R;
import com.example.danielandersson.ragestats.ui.fragment.BlockGraphItemFragment.OnListFragmentInteractionListener;

import java.util.ArrayList;

/**
 * specified {@link OnListFragmentInteractionListener}.
 */
public class CommentsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int HEADLINE = 1;
    public static final int LIST = 0;
    private ArrayList<Comment> mFilterdCommentList;
    private ArrayList<Comment> mUnfilterdCommentList;


    public CommentsAdapter(ArrayList<Comment> filterdCommentList) {
        mFilterdCommentList = filterdCommentList;
        mUnfilterdCommentList = filterdCommentList;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View viewlist = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comments_item, parent, false);
        View viewHeadline = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comments_headline_item, parent, false);


        if (viewType == LIST) {
            return new ViewHolderList(viewlist);
        } else {
            return new ViewHolderHeadline(viewHeadline);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return HEADLINE;
        } else {
            return LIST;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {


        if (holder.getItemViewType() == LIST) {
            final Comment comment = mFilterdCommentList.get(position - 1);
            final ViewHolderList viewHolderList = (ViewHolderList) holder;

            // FIXME: 2017-08-01 this doesnt work when inserting my own comments
            viewHolderList.mComment.setText(comment.getComment());
            viewHolderList.mDateTextView.setText(comment.getDate());
            viewHolderList.mTimeTextView.setText(comment.getTime());
            viewHolderList.mPublisherTextView.setText(comment.getMemberName());
        }

    }

    @Override
    public int getItemCount() {
        return (mFilterdCommentList.size() + HEADLINE);
    }

    public void addComment(Comment comment) {
        mFilterdCommentList.add(getItemCount() - 1, comment);
        notifyItemInserted(getItemCount());
    }

    public void filterByTag(String tag) {
        ArrayList<Comment> comments = new ArrayList<>();
        for (Comment comment : mUnfilterdCommentList) {
            if (comment.getTag().equals(tag)) {
                comments.add(comment);
            }
        }
        mFilterdCommentList = comments;
        notifyDataSetChanged();
        // TODO: 2017-08-01 add a way to add tags as well
    }

    public class ViewHolderList extends RecyclerView.ViewHolder {
        public final View mView;
        private final TextView mComment;
        private final TextView mDateTextView;
        private final TextView mTimeTextView;
        private final TextView mPublisherTextView;

        public ViewHolderList(View view) {
            super(view);
            mView = view;

            mComment = (TextView) view.findViewById(R.id.comment_textview);
            mDateTextView = (TextView) view.findViewById(R.id.date_textview);
            mTimeTextView = (TextView) view.findViewById(R.id.time_textview);
            mPublisherTextView = (TextView) view.findViewById(R.id.publisher_textview);
        }

    }

    public class ViewHolderHeadline extends RecyclerView.ViewHolder {

        public ViewHolderHeadline(View itemView) {
            super(itemView);
        }
    }

}
