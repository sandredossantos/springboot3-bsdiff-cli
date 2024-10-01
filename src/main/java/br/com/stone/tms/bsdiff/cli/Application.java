package br.com.stone.tms.bsdiff.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
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

	@Option(names = { "--source" }, required = true, description = "Caminho do arquivo de origem (source)")
	private String sourcePath;

	@Option(names = { "--target" }, required = true, description = "Caminho do arquivo de destino (target)")
	private String targetPath;

	@Option(names = { "--output" }, required = true, description = "Caminho do arquivo de saída (output)")
	private String outputPath;

	@Unmatched
	private List<String> unmatchedOptions;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		int exitCode = new CommandLine(new Application()).execute(args);
		System.exit(exitCode);
	}

	@Override
	public void run() {
		try {
			generateDiff();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
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
