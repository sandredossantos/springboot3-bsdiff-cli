package br.com.stone.tms.bsdiff.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import br.com.stone.tms.bsdiff.cli.core.FileByFileV1DeltaGenerator;
import br.com.stone.tms.bsdiff.cli.shared.DefaultDeflateCompatibilityWindow;
import picocli.CommandLine;
import picocli.CommandLine.Option;
import picocli.CommandLine.Unmatched;

@SpringBootApplication
public class Application implements Runnable {

	@Option(names = { "--source" }, required = true, description = "source file path (source)")
	private String sourcePath;

	@Option(names = { "--target" }, required = true, description = "target file path (target)")
	private String targetPath;

	@Option(names = { "--output" }, required = true, description = "output file path (output)")
	private String outputPath;

	@Option(names = { "--timeout" }, description = "timeout in seconds (optional)")
	private Integer timeout;

	@Unmatched
	private List<String> unmatchedOptions;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		int exitCode = new CommandLine(new Application()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public void run() {

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		
		Callable<Void> task = () -> {
			generateDiff();
			return null;
		};

		Future<Void> future = executorService.submit(task);

		try {
			if (timeout != null) {
				future.get(timeout, TimeUnit.SECONDS);
			} else {
				future.get();
			}
		} catch (TimeoutException e) {
			System.err.println("Operation timed out after " + timeout + " seconds.");
			future.cancel(true);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			executorService.shutdown();
		}

	}

	private void generateDiff() throws FileNotFoundException, IOException, InterruptedException {

		File sourceFile = new File(sourcePath);
		File targetFile = new File(targetPath);

		if (!new DefaultDeflateCompatibilityWindow().isCompatible()) {
			System.err.println("zlib not compatible on this system");
			System.exit(-1);
		}

		Deflater compressor = new Deflater(9, true);

		try (FileOutputStream patchOut = new FileOutputStream(outputPath);
				DeflaterOutputStream compressedPatchOut = new DeflaterOutputStream(patchOut, compressor, 32768)) {

			new FileByFileV1DeltaGenerator().generateDelta(sourceFile, targetFile, compressedPatchOut);
			compressedPatchOut.finish();
			compressedPatchOut.flush();

		} finally {
			compressor.end();
		}
	}

}
