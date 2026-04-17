package mp3_app;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DownloadService {


    private static final Pattern PROGRESS_PATTERN =
            Pattern.compile("(\\d{1,3}\\.\\d+)%");

    private Process currentProcess;

    public interface OutputListener {
        void onOutput(String text);
        void onProgress(double progress);
    }

    public void downloadMP3(String url, String folder, int maxDownloads, OutputListener listener) {

        new Thread(() -> {
            try {

                String basePath = new File(
                        DownloadService.class
                                .getProtectionDomain()
                                .getCodeSource()
                                .getLocation()
                                .toURI()
                ).getParent();

                String ytDlpPath = basePath + File.separator + "tools" + File.separator + "yt-dlp.exe";
                String ffmpegPath = basePath + File.separator + "tools";

                File ytFile = new File(ytDlpPath);
                if (!ytFile.exists()) {
                    listener.onOutput("ERROR: yt-dlp not found!");
                    return;
                }

                String outputTemplate = folder + File.separator + "%(title)s.%(ext)s";

                ProcessBuilder builder = new ProcessBuilder(
                        ytDlpPath,
                        "-x",
                        "--audio-format", "mp3",
                        "--audio-quality", "192K",
                        "--add-metadata",
                        "--embed-thumbnail",
                        "--ffmpeg-location", ffmpegPath,
                        "--max-downloads", String.valueOf(maxDownloads),
                        "-o", outputTemplate,
                        url
                );

                builder.redirectErrorStream(true);

                currentProcess = builder.start();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(currentProcess.getInputStream())
                );

                String line;

                while ((line = reader.readLine()) != null) {

                    listener.onOutput(line);


                    Matcher matcher = PROGRESS_PATTERN.matcher(line);

                    if (matcher.find()) {
                        try {
                            double percent = Double.parseDouble(matcher.group(1));
                            listener.onProgress(percent / 100.0);
                        } catch (Exception ignored) {
                        }
                    }
                }

                currentProcess.waitFor();

                listener.onProgress(1.0);
                listener.onOutput("Download complete!");

            } catch (Exception e) {
                listener.onOutput("Error: " + e.getMessage());
            }
        }).start();
    }


    public void stopDownload() {
        if (currentProcess != null && currentProcess.isAlive()) {
            currentProcess.destroy();
        }
    }
}