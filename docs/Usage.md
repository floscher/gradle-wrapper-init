> [gradle-wrapper-init](../README.md) › [Documentation](./README.md) › **Usage**

# Usage

Run the following command in the command line in the project directory of the Gradle root project:
```shell
java gradle/wrapper/Init.java
```

This will check, if your `gradle-wrapper.jar` is already present in the correct version.
If not, it will try to download the correct version (will only succeed if the checksum of the file matches).

---

The command can be run repeatedly, but doing so is only needed, when the project upgrades to a different Gradle version.

If you want to study what `gradle-wrapper-init` does, you can find a commented version of the distributed file here: [`src/main/java/Init.java`](../src/main/java/Init.java)
