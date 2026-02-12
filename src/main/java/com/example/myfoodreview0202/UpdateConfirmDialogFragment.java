package com.example.myfoodreview0202;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;


public class UpdateConfirmDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("確認");
        builder.setMessage("更新してもよろしいですか？");
        builder.setPositiveButton("更新", new UpdateConfirmDialogFragment.DialogButtonClickListener());
        builder.setNegativeButton("キャンセル", null);
        //ダイアログオブジェクトを生成、リターン
        return builder.create();
    }

    //ダイアログボタンがタップされたときの処理
    public class DialogButtonClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface builder, int which) {
            //更新ボタンがタップされたとき
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    UpdateActivity activity = (UpdateActivity) getActivity();
                    activity.shopUpdate();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
