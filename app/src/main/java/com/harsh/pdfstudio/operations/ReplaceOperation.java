package com.harsh.pdfstudio.operations;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import com.harsh.pdfstudio.renderer.ReplaceRenderer;
import java.io.IOException;

public class ReplaceOperation implements PageOperation{

    private Uri imageUri;
    private  Context context;

    public ReplaceOperation(Context context, Uri imageUri){
        this.imageUri=imageUri;
        this.context=context;
    }


    @Override
    public void apply(PageRenderContext renderContext){

        try {
           Bitmap replaceBitmap= ReplaceRenderer.replaceBitmap(context,imageUri);

           renderContext.replaceBitmap(replaceBitmap);

        }
        catch (IOException e) {

            return ;

        }


    }


}
