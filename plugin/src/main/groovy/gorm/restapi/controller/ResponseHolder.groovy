class ResponseHolder {
    Object data
    def headers = [:]
    def message

    void addHeader( String name, Object value ) {
        if (!headers[name]) {
            headers[name] = []
        }
        headers[name].add value?.toString()
    }
}