function (doc) {
    if (!doc._attachments && doc.extended_entities) {
        emit(doc._id,{Username:doc.user.name, Message:doc.text});
    }
}