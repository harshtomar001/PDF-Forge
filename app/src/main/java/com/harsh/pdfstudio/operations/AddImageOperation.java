package com.harsh.pdfstudio.operations;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;

import com.harsh.pdfstudio.model.AddImageObject;
import com.harsh.pdfstudio.renderer.AddImageRenderer;

import java.util.List;

public class AddImageOperation implements PageOperation {


    private List<AddImageObject> addImageObjectList ;

    private Context context;

    public AddImageOperation(Context context ,List<AddImageObject> list){

        this.addImageObjectList=list;

        this.context = context;

    }




    @Override
    public void apply(PageRenderContext renderContext){

        Bitmap bitmap =  renderContext.getBitmap();


        AddImageRenderer renderer  = new AddImageRenderer(this.context,
                                                          renderContext.getCanvas() ,
                                                          this.addImageObjectList ,
                                                          bitmap
        );



        bitmap = renderer.renderImagesOnPages();



        renderContext.replaceBitmap(bitmap);




    }

}
