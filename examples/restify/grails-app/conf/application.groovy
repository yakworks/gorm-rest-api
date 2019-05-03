app {
    resources {
        currentTenant = { return [id: 1, num: "testTenant"] }
        setup.location = "setup"

        rootLocation = { args ->
            File file = new File("./target/virgin-2")
            if (!file.exists()) {
                println "Creating rootLocation ${file.canonicalPath} for testing purposes."
                file.mkdirs()
            }
            return file.canonicalPath
        }

    }
}
