package com.harsh.pdfstudio.model;

import android.graphics.Bitmap;
import android.util.LruCache;

import androidx.lifecycle.ViewModel;

import com.harsh.pdfstudio.operations.EditOperation;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.TreeSet;



/**
 * ViewModel responsible for managing the state and operations related to PDF editing.
 * It maintains a history of operations for undo/redo functionality and tracks
 * which pages have been modified.
 */
public class EditPdfViewModel extends ViewModel {

    int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    int cacheSize = maxMemory / 8;
//    LruCache<Integer, Bitmap> redoCache=new LruCache<Integer, Bitmap>(cacheSize) {
//        @Override
//        protected int sizeOf(Integer key, Bitmap bitmap) {
//            return bitmap.getByteCount() / 1024;
//        }
//    };
//
//    LruCache<Integer, Bitmap> undoCache=new LruCache<Integer, Bitmap>(cacheSize) {
//        @Override
//        protected int sizeOf(Integer key, Bitmap bitmap) {
//            return bitmap.getByteCount() / 1024;
//        }
//    };



    /**
     * A sorted set of page numbers that have associated edit operations.
     */
    private final Set<Integer> pdfPagesSet = new TreeSet<>();

    /**
     * A mapping of page numbers to the list of edit operations applied to that specific page.
     */
    private final HashMap<Integer, List<EditOperation>> pageOperations = new HashMap<>();

    /**
     * Stack to track operations that can be undone.
     */
    private final Stack<EditOperation> undoStack = new Stack<>();

    /**
     * Stack to track operations that can be redone after an undo.
     */
    private final Stack<EditOperation> redoStack = new Stack<>();

    /**
     * Applies an edit operation to a specific page.
     * The operation is added to the page's history, tracked in the modified pages set,
     * and pushed onto the undo stack. The redo stack is cleared when a new operation is applied.
     *
     * @param op The edit operation to apply.
     */
    public void applyOperation(EditOperation op) {
        Integer key = op.getPageNumber();

        // Ensure the list for the page exists and add the operation
        pageOperations
                .computeIfAbsent(key, k -> new ArrayList<>())
                .add(op);

        // Mark the page as modified

        pdfPagesSet.add(key);

        // Update undo/redo stacks
        undoStack.push(op);
        redoStack.clear();
    }

    /**
     * Checks if a specific page has any edit operations associated with it.
     *
     * @param page The page number to check.
     * @return true if the page has operations, false otherwise.
     */
    public boolean isPresent(int page) {
        return pdfPagesSet.contains(page);
    }

    /**
     * Returns the set of all page numbers that have been modified.
     *
     * @return A set of modified page indices.
     */
    public Set<Integer> getModifiedPages() {
        return pdfPagesSet;
    }

    /**
     * Retrieves the list of operations applied to a specific page.
     *
     * @param page The page number.
     * @return A list of operations for the given page, or null if none exist.
     */
    public List<EditOperation> getOperationsForPage(int page) {
        return pageOperations.get(page);
    }

    public Stack<EditOperation>  getStackOfModifiedPage(){

        return  undoStack;

    }

    public HashMap<Integer , List<EditOperation>> getPdfOperationsList(){
        return pageOperations;
    }

    public boolean isAnyJobDone(){

        return  pageOperations.size() > 0;
    }
}
