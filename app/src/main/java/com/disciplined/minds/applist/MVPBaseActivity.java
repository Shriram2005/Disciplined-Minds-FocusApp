package com.disciplined.minds.applist;

import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.disciplined.minds.R;


/**
 * Created by Square Infosoft.
 */

public abstract class MVPBaseActivity<P extends MVPBasePresenter> extends AppCompatActivity implements MVPBaseView<P> {

    boolean isDestroyed;
    private P presenter;
    public ProgressDialog dialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        presenter = getNewPresenter();
        getPresenter().bindView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isDestroyed = true;
        getPresenter().unBindView();
    }

    @Override
    public boolean isAlive() {
        return !isFinishing() && !isDestroyed;
    }

    public final P getPresenter() {
        return presenter;
    }

    public void showProgressDialog(Boolean isCancelable){
        if(dialog !=null && dialog.isShowing()){
            dialog.dismiss();
        }
        dialog = new ProgressDialog(this, R.style.MyAlertDialogStyle);
        dialog.setMessage(getString(R.string.msg_please_wait));
        dialog.setCancelable(isCancelable);
        dialog.show();
    }

    public void hideProgressDialog(){
        if (dialog != null) {
            dialog.dismiss();
        }
    }

}
