package android.support.v7.widget;

import androidx.recyclerview.widget.RecyclerView;

public abstract class ViewHolderDelegate {

    private ViewHolderDelegate() {
        throw new UnsupportedOperationException("no instances");
    }

    public static void setPosition(RecyclerView.ViewHolder viewHolder, int position) {
        viewHolder.mPosition = position;
    }
}
