package net.iowntheinter.kvdn.storage.meta.types


enum EncodingType {
    KRYO,
    GZIP,
    BZIP,
    LZ4,
    B64

    Object decode(String value){
        switch (valueOf()){
            case KRYO:
                return
                break

        }
    }
}
