import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Checks, if the file `gradle-wrapper.jar` is present, downloads it if necessary and ensures
 * that it has the checksum in `gradle-wrapper.properties`.
 */
public class Init {
  public static void main(String[] args) throws URISyntaxException, IOException, NoSuchAlgorithmException {
    final Path gradleWrapperDir = args.length == 1 ? Path.of(args[0]) : Path.of(Init.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
    final Properties gradleWrapperProps = loadProperties(gradleWrapperDir.resolve("gradle-wrapper.properties"));
    final URL distributionUrl = URI.create(gradleWrapperProps.getProperty("distributionUrl")).toURL();
    final String expectedWrapperSha256 = Objects.requireNonNull(gradleWrapperProps.getProperty("wrapperSha256Sum"));

    try (InputStream stream = new FileInputStream(gradleWrapperDir.resolve("gradle-wrapper.jar").toFile())) {
      assertChecksumMatches(expectedWrapperSha256, stream);
    } catch (IOException e) {
      System.out.println(e.getLocalizedMessage() + "\nGradle wrapper not present (or present with mismatching checksum). Downloading one now …");

      // 5. Write the `gradle-wrapper.jar` file
      Files.write(gradleWrapperDir.resolve("gradle-wrapper.jar"),
        // 4. Only proceed if the file matches the `wrapperSha256Sum` given in the `gradle-wrapper.properties`.
        assertChecksumMatches(expectedWrapperSha256,
          // 3. Unpack the actual `gradle-wrapper.jar` from that jar file
          unpackInputStream("gradle-wrapper.jar"::equals,
            // 2. Unpack the `gradle-wrapper-main-*.jar` from the Gradle distribution
            unpackInputStream(
              (s) -> s.matches("^gradle-[0-9a-z.-]+/lib/plugins/gradle-wrapper-main-[0-9a-z.-]+\\.jar$"),
              // 1. Download the Gradle distribution given in the `gradle-wrapper.properties`
              distributionUrl.openStream()
      ))));
    }
    System.out.println(" \uD83D\uDC4D You are good to go, you have the correct gradle-wrapper.jar");
  }

  private static Properties loadProperties(final Path filePath) throws IOException {
    final Properties p = new Properties();
    try (InputStream stream = new FileInputStream(filePath.toFile())) {
      p.load(stream);
      return p;
    }
  }

  /**
   * Unpacks the zip file given as `stream` parameter.
   * @return an {@link InputStream} for the first file in the zip archive that matches the given `filenameMatcher` {@link Predicate}.
   * @throws IOException if any error is encountered, or if there is no matching file
   */
  private static InputStream unpackInputStream(final Predicate<String> filenameMatcher, final InputStream stream) throws IOException {
    try (ZipInputStream zipStream = new ZipInputStream(stream)) {
      while (true) {
        System.out.print('░');
        final ZipEntry entry = zipStream.getNextEntry();
        if (entry == null) {
          throw new IOException("No matching filename in the zip archive.");
        } else if (filenameMatcher.test(entry.getName())) {
          System.out.println();
          return new ByteArrayInputStream(zipStream.readAllBytes());
        }
      }
    }
  }

  /**
   * Fully reads through the given `stream` and checks, if the data has the given SHA-256 checksum.
   * @return the byte data read from the {@link InputStream} given as parameter
   * @throws IOException if an error occurs while reading from the InputStream, or if the checksum does not match
   */
  private static byte[] assertChecksumMatches(final String expectedChecksum, final InputStream stream) throws NoSuchAlgorithmException, IOException {
    try (DigestInputStream dis = new DigestInputStream(stream, MessageDigest.getInstance("SHA-256"))) {
      final byte[] bytes = dis.readAllBytes();
      final String actualChecksum = HexFormat.of().formatHex(dis.getMessageDigest().digest());
      if (!expectedChecksum.equalsIgnoreCase(actualChecksum)) {
        throw new IOException("Checksum verification failed!\n  Expected: " + expectedChecksum + "\n    Actual: " + actualChecksum);
      }
      System.out.println(" ✅ Checksum is " + expectedChecksum + " as expected");
      return bytes;
    }
  }
}
