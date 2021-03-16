package com.onyx.android.demo.note;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.onyx.android.demo.R;
import com.onyx.android.demo.scribble.request.LoadMovableLibraryRequest;
import com.onyx.android.demo.view.DashLineItemDivider;
import com.onyx.android.demo.view.PageRecyclerView;
import com.onyx.android.demo.view.TocTreeRecyclerView;
import com.onyx.android.sdk.data.GPaginator;
import com.onyx.android.sdk.rx.RxCallback;
import com.onyx.android.sdk.rx.RxManager;
import com.onyx.android.sdk.scribble.data.NoteLibraryTableOfContentEntry;
import com.onyx.android.sdk.scribble.data.NoteModel;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.ResManager;

import java.util.ArrayList;
import java.util.List;

public class DialogCreateNoteFolder extends Dialog {
    private final static int MOVE_LIBRARY_LIST_ROW_COUNT = 7;
    private RxManager rxManager;
    private TocTreeRecyclerView noteFolderList;
    private TextView bottomPageSizeIndicator;
    private OnDialogCreateNoteListener listener;

    public interface OnDialogCreateNoteListener {
        void onCreateNote(NoteModel library);
    }

    public DialogCreateNoteFolder(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initContentView();
    }

    public DialogCreateNoteFolder setOnDialogCreateNoteListener(OnDialogCreateNoteListener onDialogCreateNoteListener) {
        this.listener = onDialogCreateNoteListener;
        return this;
    }

    private void initContentView() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_note_folder, null, false);
        setContentView(contentView);
        initView(contentView);
        loadNoteLibrary();
    }

    private void loadNoteLibrary() {
        getRxManager().enqueue(new LoadMovableLibraryRequest(), new RxCallback<LoadMovableLibraryRequest>() {
            @Override
            public void onNext(@NonNull LoadMovableLibraryRequest request) {
                initList(request.getTableOfContentEntry());
            }
        });
    }

    private void initView( View contentView) {
        noteFolderList = contentView.findViewById(R.id.note_folder_list);
        bottomPageSizeIndicator = contentView.findViewById(R.id.bottom_page_size_indicator);
        noteFolderList.setDefaultPageKeyBinding();
        noteFolderList.setOnPagingListener(new PageRecyclerView.OnPagingListener() {
            @Override
            public void onPageChange(int position, int itemCount, int pageSize) {
                updatePageInfo();
            }
        });
    }

    private void initList(NoteLibraryTableOfContentEntry tableOfContentEntry) {
        DashLineItemDivider decoration = new DashLineItemDivider(ResManager.getColor(R.color.dash_line_color));
        noteFolderList.addItemDecoration(decoration);
        noteFolderList.setItemDecorationHeight(decoration.getDividerHeight());

        NoteLibraryTableOfContentEntry root = new NoteLibraryTableOfContentEntry();
        root.noteLibrary = NoteModel.createLibrary(null, null, getContext().getString(R.string.root_directory));
        tableOfContentEntry.children.add(0, root);

        List<TocTreeRecyclerView.TocTreeNode> rootNodes = buildTreeNodesFromTocEntry(tableOfContentEntry);
        noteFolderList.bindTree(rootNodes, new TocTreeRecyclerView.Callback() {
            @Override
            public void onTreeNodeClicked(TocTreeRecyclerView.TocTreeNode node) {
                if (node.getTag() == null || !(node.getTag() instanceof NoteModel)) {
                    return;
                }
                if (listener != null) {
                    dismiss();
                    listener.onCreateNote((NoteModel) node.getTag());
                }
            }

            @Override
            public void onItemCountChanged(int position, int itemCount) {
                updatePageInfo();
            }
        }, MOVE_LIBRARY_LIST_ROW_COUNT);
        updatePageInfo();
    }

    private List<TocTreeRecyclerView.TocTreeNode> buildTreeNodesFromTocEntry(NoteLibraryTableOfContentEntry tocEntry) {
        List<TocTreeRecyclerView.TocTreeNode> nodes = new ArrayList<>();
        for (NoteLibraryTableOfContentEntry child : tocEntry.children) {
            nodes.add(buildTreeNode(null, child));
        }
        return nodes;
    }

    private TocTreeRecyclerView.TocTreeNode buildTreeNode(TocTreeRecyclerView.TocTreeNode parent, NoteLibraryTableOfContentEntry entry) {
        String desc = CollectionUtils.isNullOrEmpty(entry.children) ? "" : String.valueOf(CollectionUtils.getSize(entry.children));
        TocTreeRecyclerView.TocTreeNode node = new TocTreeRecyclerView.TocTreeNode(parent, entry.noteLibrary.getTitle(),
                desc, entry.noteLibrary);
        if (!CollectionUtils.isNullOrEmpty(entry.children)) {
            for (NoteLibraryTableOfContentEntry child : entry.children) {
                node.addChild(buildTreeNode(node, child));
            }
        }
        return node;
    }

    private void updatePageInfo() {
        GPaginator paginator = noteFolderList.getPaginator();
        if (paginator == null) {
            return;
        }
        bottomPageSizeIndicator.setText(paginator.getProgressText());
    }

    private RxManager getRxManager() {
        if (rxManager == null) {
            rxManager = RxManager.Builder.sharedSingleThreadManager();
        }
        return rxManager;
    }
}
