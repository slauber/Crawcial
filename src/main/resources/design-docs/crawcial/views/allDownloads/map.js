function(doc) {
    if(doc._attachments) {
        emit(doc._id,{Username:doc.user.name, Message:doc.text});
    }
}