package net.iowntheinter.kvdn.storage.meta.types;

public enum ValueType {
    TEXT_HTML,
    TEXT_PLAIN,
    APPLICAION_JSON,
    BYTE_BUFFER,
    KVDN_POINTER;

    ValueType fromMIME(String mime) {
        return valueOf(mime.replace('/', '_').toUpperCase());
    }
    String toMIME(){
        return(null);
    }

}
