import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.UUID;

import static spark.Spark.*;

public class Main {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void main(String[] args) {

        int port = 8080;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println(String.format("Invalid integer value: %s, using default port...", args[0]));
            }
        }

        System.out.println(String.format("Starting Spark on port %d", port));

        port(port);

        post("/convert/", (req, resp) -> {
            System.out.println("Received new request, generating files...");

            final File file = new File(UUID.randomUUID().toString() + ".vsdx");
            final File converted = new File(StringUtils.substringBefore(file.getPath(), ".") + ".xml");

            System.out.println(String.format("File created as: %s, writing contents", file.getPath()));
            try (final BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {

                out.write(req.bodyAsBytes());

                System.out.println("Converting file...");

                vsdxBatchConvert.execute(file);

                System.out.println("Generating response body...");

                final StringBuilder builder = new StringBuilder();
                for (final String line : Files.readAllLines(converted.toPath())) {
                    builder.append(line);
                }

                resp.body(builder.toString());

                return resp.body();
            } catch (final IOException ex) {
                ex.printStackTrace();
            } finally {

                System.out.println("Deleting files...");

                file.delete();
                converted.delete();

            }

            System.out.println("An unexpected error occurred, giving 400 error (bad request)");

            resp.status(400);

            return null;
        });

    }

}
