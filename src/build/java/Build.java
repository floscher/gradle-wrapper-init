import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Build {
  public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException, NoSuchAlgorithmException {

    final String gitHash;
    final Process process = Runtime.getRuntime().exec(new String[]{ "git", "rev-parse", "HEAD" });
    try (
      InputStream stream = process.getInputStream();
      InputStream err = process.getErrorStream()
    ) {
      if (0 != process.waitFor()) {
        throw new IOException("git process exited with code " + process.exitValue() + ": " +new String(err.readAllBytes()));
      }
      gitHash = new String(stream.readAllBytes()).trim();
      if (!gitHash.matches("^[0-9a-f]{64}$")) {
        throw new IOException("Unexpected git hash format, expected 64 hexadecimal characters (was length " + gitHash.length() + "): " + gitHash);
      }
    }

    final Path projectDir = Path.of(Build.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent().resolve("../../../").toRealPath();

    final String contentWithoutComments = Files
      .readString(projectDir.resolve("src/main/java/Init.java"))
      .replaceAll("\\r\\n", "\n") // replace any Windows style line endings
      .replaceAll("/\\*(?:.|\\n)*?\\*/", "") // remove block comments
      .replaceAll("\\n(\\s*(//.*)?)\\n", "\n") // remove line comments and blank lines
      .replaceAll("\\s+\\n", "\n") // remove any trailing spaces
      ;

    final Map<Boolean, List<String>> importPartitions = contentWithoutComments.lines().collect(Collectors.partitioningBy(it -> it.matches("import .+\\..+;")));


    final String result =
      Stream.of(
        "// This file is licensed under the Unlicense: https://unlicense.org",
        "// For usage documentation for `gradle-wrapper-init`, see https://gitlab.com/floscher/gradle-wrapper-init/-/blob/" + gitHash.substring(0, 16) + "/docs/Usage.md",
        "// JAVA 17+"
      ).collect(Collectors.joining("\n", "", "\n")) +
      importPartitions.get(true).stream()
        .map(it -> it.substring(0, it.lastIndexOf('.')) + ".*;")
        .distinct()
        .sorted()
        .collect(Collectors.joining("\n", "", "\n")) +
      String.join("\n", importPartitions.get(false)) +
      '\n';

    final String sha256sum = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(result.getBytes(StandardCharsets.UTF_8)));

    final File buildDir = projectDir.resolve("build").toFile();
    if (!buildDir.exists() && !buildDir.mkdirs()) {
      throw new IOException("Could not create build directory " + buildDir + "!");
    }

    Files.writeString(projectDir.resolve("build/Init.java"), result);
    Files.writeString(projectDir.resolve("build/checksums.properties"), "gradleWrapperInitSha256Sum=" + sha256sum + '\n');
    System.out.println("Build finished successfully!");
  }
}
