package net.iowntheinter.kvdn.unipop

/**
 * Created by g on 2/23/17.
 */
class kvdnStruct {
    private final String index
    private final String type
    private final String id
    private final Map<String, Object> fields

    kvdnStruct(String index, String type, String id, Map<String, Object> fields) {
        this.index = index
        this.type = type
        this.id = id
        this.fields = fields
    }

    String getIndex() {
        return index
    }

    String getType() {
        return type
    }

    String getId() {
        return id
    }

    Map<String, Object> getFields() {
        return fields
    }
}
