package com.harsh.pdfstudio.utils;

import android.content.Intent;

public class Image_picker {

    public static Intent openImageFiles(){
        Intent intent=new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);
        intent.setType("image/*");
        return intent;
    }


}
