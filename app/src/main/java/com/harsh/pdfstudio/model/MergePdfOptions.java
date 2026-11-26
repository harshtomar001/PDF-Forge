package com.harsh.pdfstudio.model;

import com.harsh.pdfstudio.ui.MergePdfActivity.PageSizeMode;
import com.harsh.pdfstudio.ui.MergePdfActivity.PageMargin;

public class MergePdfOptions {

    private PageSizeMode pageSizeMode;
    private PageMargin margin;

    public MergePdfOptions(PageSizeMode value, PageMargin pageMarginValue) {

        pageSizeMode = value;
        margin = pageMarginValue;
    }

    public PageSizeMode getPageSizeMode() {

        return pageSizeMode;

    }

    public void setPageSizeMode(PageSizeMode pageSizeMode) {

        this.pageSizeMode = pageSizeMode;

    }

    public PageMargin getMargin() {

        return margin;

    }

    public void setMargin(PageMargin margin) {

        this.margin = margin;

    }
}
