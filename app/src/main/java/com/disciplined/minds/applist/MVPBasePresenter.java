package com.disciplined.minds.applist;

/**
 * Created by Square Infosoft.
 */

public abstract class MVPBasePresenter<V extends MVPBaseView> {
    private V view;

    /**
     * binds view indicating view is active and accepts requests to update ui
     *
     * @param baseView
     */
    public final void bindView(V baseView) {
        this.view = baseView;
    }

    /**
     * unbinds view indicating view no longer accepts requests to update ui
     */
    public final void unBindView() {
        this.view = null;
    }

    /**
     * Tells whether view is alive and accepting requests to update ui
     *
     * @return
     */
    public final boolean isViewAlive() {
        return view != null && view.isAlive();
    }

    public final V getView() {
        return view;
    }

}