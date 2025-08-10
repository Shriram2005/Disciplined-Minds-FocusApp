package com.disciplined.minds.applist;

/**
 * Created on 10/09/16.
 */
public interface MVPBaseView<P> {

    /**
     * returns whether view is accepting requests to update ui
     *
     * @return
     */
    boolean isAlive();

    P getNewPresenter();

}