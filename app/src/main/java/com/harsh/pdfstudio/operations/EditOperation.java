package com.harsh.pdfstudio.operations;

public class EditOperation {
    private int pageNumber;

    public enum OperationType {
        WATERMARK,
        AddText,
        ROTATE,
        DELETE_PAGE
    }

    private OperationType type;

    public EditOperation(int pageNumber,OperationType operationsType){
        this.pageNumber=pageNumber;
        this.type=operationsType;
    }



    public int getPageNumber(){
        return pageNumber;
    }

    public OperationType getOperationsType(){return type;};


}
