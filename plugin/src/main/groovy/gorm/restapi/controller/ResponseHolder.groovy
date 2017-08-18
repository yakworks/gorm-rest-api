class ResponseHolder {
    Object data
    Map headers = [:]
    String message

    void addHeader( String name, Object value ) {
        if (!headers[name]) {
            headers[name] = []
        }
        headers[name].add value?.toString()
    }
}