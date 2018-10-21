package au.edu.unimelb.student.group55.my_ins.BlueTooth.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.FileNotFoundException;

import au.edu.unimelb.student.group55.my_ins.BlueTooth.service.SendSocketService;
import au.edu.unimelb.student.group55.my_ins.R;


/**
 * Bounced tools
 */

public class DialogUtil {
    private static ProgressDialog progressDialog;
    private static AlertDialog.Builder builder;

    //progressbar
    public static void ShowProgress(Context context, String message){
        if (progressDialog ==null){
            progressDialog = new ProgressDialog(context);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(true);
        }
        progressDialog.show();

    }

    public static void CancelProgress(){
        if (progressDialog !=null){
            if (progressDialog.isShowing()){
                progressDialog.cancel();
            }
        }
    }

    //Custom popover
    public static void ShowAlertDialog(Context context , String Title, String Message, DialogInterface.OnClickListener onClickListener){
        if (builder==null){
            builder = new AlertDialog.Builder(context);
            // builder.setIcon(R.drawable.ic_launcher);
            builder.setTitle(Title);
            builder.setMessage(Message);

            builder.setPositiveButton("Ensure", onClickListener);
            builder.setNegativeButton("Cancell",null);
        }

        builder.show();
    }

    //Custom popup for locking
    public static class DefineDialog extends Dialog {
        private int progreesmax;
        private int getProgrees;
        private Uri path;
        private ProgressBar pro;
        private Context context;
        public DefineDialog(@NonNull Context context, int progreesmax, int getProgrees, Uri path) {
            super(context);
            this.progreesmax = progreesmax;
            this.getProgrees = getProgrees;
            this.path = path;
            this.context=context;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.bt2_dialog_define);
            setCanceledOnTouchOutside(true);
            ImageView img =findViewById(R.id.dialog_img);
            pro =findViewById(R.id.dialog_progress);
            ContentResolver cr = context.getContentResolver();
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(cr.openInputStream(path));
                img.setImageBitmap(bitmap);
            } catch (FileNotFoundException e) {
                Log.e("Exception", e.getMessage(),e);
            }

            pro.setMax(progreesmax);
            new SendSocketService() .setProgressListener(new SendSocketService.setProgessIml() {
                @Override
                public void setProgress(int size) {
                    pro.setMax(size);
                }
            });

            pro.setProgress(getProgrees);

        }


    }

}
