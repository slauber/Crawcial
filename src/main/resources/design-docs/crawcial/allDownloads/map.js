function (doc) {
    if (doc._attachments && doc.media) {
        emit(doc._id);
    }
}