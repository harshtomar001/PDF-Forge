package com.harsh.pdfstudio.operations;

import java.io.IOException;

public interface PageOperation {

    void apply(PageRenderContext context) throws IOException;

}
