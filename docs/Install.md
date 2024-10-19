> [gradle-wrapper-init](../README.md) › [Documentation](./README.md) › **Install**

# Install

This is how you can add `gradle-wrapper-init` to a Gradle project, or upgrade it for a newer Gradle version.

## Add wrapper sha256 sum

At https://services.gradle.org/distributions/ you can find the SHA-256 checksum for your desired Gradle version in a file called `gradle-*-wrapper.jar.sha256`.

Add that checksum to your `gradle/wrapper/gradle-wrapper.properties` file in a new line like this:
```properties
wrapperSha256Sum=‹checksum›
```

## Add `Init.java`

Copy the `Init.java` file from the latest `gradle-wrapper-init` release into the directory `$projectDir/gradle/wrapper/` inside your project.

Now you should be good to go, see [Usage.md](./Usage.md) for instructions on how to use it in your project.
