package com.example.myfoodreview0202;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class AddAlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //Bundleから未入力項目を取得
        Bundle args = getArguments();
        String msg = args.getString("Msg","");

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("注意");
        builder.setMessage(msg);
        builder.setPositiveButton("OK", new AddAlertDialogFragment.DialogButtonClickListener());
        //ダイアログオブジェクトを生成、リターン
        return builder.create();
    }

    //ダイアログボタンがタップされたときの処理
    public class DialogButtonClickListener implements DialogInterface.OnClickListener {
        public void onClick(DialogInterface builder, int which) {
            //更新ボタンがタップされたとき
            switch (which) {
                case DialogInterface.BUTTON_POSITIVE:
                    break;
            }
        }
    }
}