package com.example.myfoodreview0202;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class AddConfirmDialogFragment extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("登録確認");
        builder.setMessage("登録してもよろしいですか？");
        builder.setPositiveButton("登録",new DialogButtonClickListener());
        builder.setNegativeButton("キャンセル",null);
        //ダイアログオブジェクトを生成、リターン
        return builder.create();

    }
    //ダイアログボタンがタップされたときの処理
    public class DialogButtonClickListener implements DialogInterface.OnClickListener{
        public void onClick(DialogInterface builder,int which){
            //登録ボタンがタップされたとき
            switch(which){
                case DialogInterface.BUTTON_POSITIVE:
                    AddActivity activity = (AddActivity) getActivity();
                    activity.shopListRegister();
                    break;
                case DialogInterface.BUTTON_NEGATIVE:
                    break;
            }
        }
    }
}
