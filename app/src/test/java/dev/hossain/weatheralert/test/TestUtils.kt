package dev.hossain.weatheralert.test

/**
 * Common test utilities shared across test classes.
 */
object TestUtils {
    /**
     * Helper method to load JSON from test resources.
     *
     * @param fileName The name of the JSON file to load from the test resources directory
     * @return The JSON content as a String
     * @throws IllegalArgumentException if the file is not found
     */
    fun loadJsonFromResources(fileName: String): String {
        val classLoader = TestUtils::class.java.classLoader
        val inputStream =
            classLoader?.getResourceAsStream(fileName)
                ?: throw IllegalArgumentException("File not found: $fileName")
        return inputStream.bufferedReader().use { it.readText() }
    }
}
