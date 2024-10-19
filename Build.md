<!--
  This file is placed in the root directory instead of `./docs/` , so IntelliJ users can run them directly from the Markdown viewer.
  See https://youtrack.jetbrains.com/issue/IJPL-92401
-->
> [gradle-wrapper-init](./README.md) › [Documentation](./docs/README.md) › **Build**

# Build

Building this project is not strictly necessary, the source file [`Init.java`](src/main/java/Init.java)
could also be used as-is.

The build process just removes some stuff from the file that's not necessarily needed (comments, blank lines, …) to make it more lightweight.
Also, a notice showing the license of the file as well as a link to the usage documentation is added to the beginning of the file.

## Build with `java` command line

This step is available as IntelliJ run configuration `Build` (it might be marked with a red ❌, but should work without problem).

> **Requirements**
> * Java 17 or newer
> * git (we use [the version used in Ubuntu 24.04](https://packages.ubuntu.com/noble/git), other versions should work as well as long as they [support SHA-256 hashes](https://git-scm.com/docs/hash-function-transition))

Just run this command in the command line.

```shell
java src/build/java/Build.java
```

The resulting files can be found in the [`build/`](./build/) directory.

## Build with Docker

This step is available as IntelliJ run configuration `Build with Docker`.

> **Requirements**
> * Docker (obviously), with Compose

To build with Docker you just need to run:
```shell
docker compose up
```

The resulting files can be found in the [`build/`](./build/) directory.

> **Note:** Keep in mind that Docker might create the files in the build directory as root user.
> So if you later build without docker, you might get `AccessDeniedException`s, because the built files
> can not be written. In that case remove the `build/` directory manually.
