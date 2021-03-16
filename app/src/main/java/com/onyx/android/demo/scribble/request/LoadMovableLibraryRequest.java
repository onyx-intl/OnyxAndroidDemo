package com.onyx.android.demo.scribble.request;

import com.onyx.android.sdk.data.folder.FolderStatus;
import com.onyx.android.sdk.data.utils.QueryBuilder;
import com.onyx.android.sdk.rx.RxRequest;
import com.onyx.android.sdk.scribble.data.NoteLibraryTableOfContentEntry;
import com.onyx.android.sdk.scribble.data.NoteModel;
import com.onyx.android.sdk.scribble.data.NoteModel_Table;
import com.onyx.android.sdk.scribble.provider.RemoteNoteProvider;
import com.onyx.android.sdk.utils.CollectionUtils;
import com.onyx.android.sdk.utils.StringUtils;
import com.raizlabs.android.dbflow.sql.language.OperatorGroup;

import java.util.List;

public class LoadMovableLibraryRequest extends RxRequest {

    private NoteLibraryTableOfContentEntry tableOfContentEntry;
    private RemoteNoteProvider remoteNoteProvider;

    public NoteLibraryTableOfContentEntry getTableOfContentEntry() {
        return tableOfContentEntry;
    }

    @Override
    public void execute() {
        tableOfContentEntry = new NoteLibraryTableOfContentEntry();
        buildTocEntry(null, tableOfContentEntry);
    }

    private void buildTocEntry(String parentId, NoteLibraryTableOfContentEntry toc) {
        OperatorGroup clause = OperatorGroup.clause()
                .and(QueryBuilder.getNullOrEqualCondition(NoteModel_Table.parentUniqueId, parentId))
                .and(NoteModel_Table.type.eq(NoteModel.TYPE_LIBRARY))
                .and(NoteModel_Table.status.eq(FolderStatus.ENABLE))
                .and(NoteModel_Table.uniqueId.isNotNull());
        List<NoteModel> noteModels = getNoteProvider().loadNoteList(clause);
        if (CollectionUtils.isNullOrEmpty(noteModels)) {
            return;
        }
        for (NoteModel libraryModel : noteModels) {
            String libraryModelUniqueId = libraryModel.getUniqueId();
            if (!isParentLibraryExist(libraryModel.getParentUniqueId())) {
                continue;
            }
            NoteLibraryTableOfContentEntry child = new NoteLibraryTableOfContentEntry();
            child.noteLibrary = libraryModel;
            toc.children.add(child);
            buildTocEntry(libraryModelUniqueId, child);
        }
    }

    private boolean isParentLibraryExist(String parentUniqueId) {
        if (StringUtils.isNullOrEmpty(parentUniqueId)) {
            return true;
        }
        NoteModel parent = getNoteProvider().loadNote(parentUniqueId);
        if (parent != null
                && StringUtils.isNotBlank(parent.getUniqueId())) {
            return isParentLibraryExist(parent.getParentUniqueId());
        }
        return false;
    }

    private RemoteNoteProvider getNoteProvider() {
        if (remoteNoteProvider == null) {
            remoteNoteProvider = new RemoteNoteProvider();
        }
        return remoteNoteProvider;
    }
}
