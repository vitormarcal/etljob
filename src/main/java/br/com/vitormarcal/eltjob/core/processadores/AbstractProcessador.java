package br.com.vitormarcal.eltjob.core.processadores;

import br.com.vitormarcal.eltjob.core.FileObject;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
abstract class AbstractProcessador implements Processador {

    private final String fileName;
    private final String diretorioArquivo;
    private File dir;
    File file;

    AbstractProcessador(String fileName, String diretorioArquivo) {
        this.fileName = String.format("%s/%s", diretorioArquivo, fileName);
        this.diretorioArquivo = diretorioArquivo;
    }

    @Override
    public final boolean writeLine(@NonNull FileObject fileObject) {
        inicializaWriter();
        return write(fileObject);
    }

    protected abstract boolean write(FileObject fileObject);

    @Override
    public final boolean inicializaWriter() {
        if (this.file == null) {
            criaArquivoSeNecessario();
            return criarWriter();
        }
        return false;
    }

    protected abstract boolean criarWriter();

    private void criaArquivoSeNecessario() {
        try {
            if (file == null) {
                criaDiretoriosSeNecessario();
                file = new File(fileName);
                if (file.exists()) {
                    Files.delete(Paths.get(fileName));
                    if (file.createNewFile()) {
                        log.debug("Criado arquivo {}", fileName);
                    } else {
                        log.error("Não foi possível criar o arquivo {}", fileName);
                    }
                }

            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void criaDiretoriosSeNecessario() {
        if (dir == null) {
            dir = new File(diretorioArquivo);
            if (!dir.exists()) dir.mkdirs();
        }
    }
}
