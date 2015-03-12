function(doc) {
    if(doc.passhash && doc.name) {
        emit(doc._id);
    }
}