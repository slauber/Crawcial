function (doc) {
    if (!doc._attachments && doc.extended_entities) {
        emit(doc._id);
    }
}