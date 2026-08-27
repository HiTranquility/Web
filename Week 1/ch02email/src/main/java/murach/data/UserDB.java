package murach.data;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import murach.business.User;

/**
 * The data access layer for the User business object.
 *
 * Chapter 2 doesn't cover databases yet, so the "data store" here is a
 * tab-delimited text file. Later chapters replace the body of these methods
 * with JDBC or JPA code without changing the servlet that calls them.
 */
public class UserDB {

    /** Override with -Dmurach.emaillist.file=... to move the data store. */
    private static final String FILE_PROPERTY = "murach.emaillist.file";

    public static void insert(User user) {
        Path file = getFile();
        try {
            Files.createDirectories(file.getParent());
            try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 PrintWriter out = new PrintWriter(writer)) {
                out.println(user.getEmail() + "\t"
                        + user.getFirstName() + "\t"
                        + user.getLastName());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Unable to write to " + file, e);
        }
    }

    public static Path getFile() {
        String configured = System.getProperty(FILE_PROPERTY);
        if (configured != null && !configured.isEmpty()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), "murach", "EmailList.txt");
    }
}
