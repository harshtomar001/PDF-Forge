package com.harsh.pdfstudio.model;

import android.graphics.pdf.PdfRenderer;

import androidx.lifecycle.ViewModel;

import com.harsh.pdfstudio.adapter.EditPDF_adap;

public class Store_pdf_VM_edt_pdf_inp_frag extends ViewModel {


    public PdfRenderer pdfRenderer;
    public EditPDF_adap adapter;
    public FileItem currentPdf;
    public int pdfSize;
    public boolean hasPdfLoaded = false;



}
