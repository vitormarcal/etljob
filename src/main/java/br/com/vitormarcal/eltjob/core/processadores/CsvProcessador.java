package br.com.vitormarcal.eltjob.core.processadores;

import br.com.vitormarcal.eltjob.core.FileObject;
import com.opencsv.CSVWriter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

@Slf4j
public class CsvProcessador extends AbstractProcessador {

    /**
     * Reconhece imediatamente caracteres como ´ ou ~ ao abrir o arquivo com o MS-Office, sem isto para o MS-Office,
     * o usuário deverá configurar o encode manualmente.
     */
    private static final char UTF_8_BOM = '\uFEFF';
    private static final String DIRETORIO_CSV = "/csv";
    private static final String EXTENSAO_CSV = ".csv";

    private String[] cabecalho;
    private CSVWriter csvWriter;
    private OutputStreamWriter fileWriter;

    public CsvProcessador(String fileName, String diretorioArquivo) {
        super(fileName + EXTENSAO_CSV, diretorioArquivo + DIRETORIO_CSV);
    }

    @Override
    protected boolean write(@NonNull FileObject fileObject) {
        Map<String, String> mapa;
        try {
            mapa = fileObject.mapeiaFields();
            inicializaCabecalho(mapa.keySet());
            int index = 0;
            String[] lineStr = new String[mapa.size()];
            for (Map.Entry<String, String> chave : mapa.entrySet()) {
                String coluna = chave.getValue();
                lineStr[index] = coluna != null && !coluna.isEmpty() ? coluna.trim() : "";
                index++;
            }
            csvWriter.writeNext(lineStr);
            return true;
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException("Erro ao fazer o parse dos dados para csv", e);
        }
    }

    private void inicializaCabecalho(Set<String> headers) {
        if (cabecalho == null && headers != null) {
            cabecalho = headers.toArray(new String[headers.size()]);
            csvWriter.writeNext(cabecalho);
        }
    }

    @Override
    protected boolean criarWriter() {
        if (fileWriter == null) {
            try {
                fileWriter = new FileWriter(file);
                fileWriter.write(UTF_8_BOM);
                csvWriter = new CSVWriter(fileWriter, ';', '"', '"', "\n");
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar writer do csv", e);
            }
        } else {
            return false;
        }
    }

    @Override
    public void close() {
        try {
            if (csvWriter != null) {
                csvWriter.close();
            } else if (fileWriter != null) {
                fileWriter.flush();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fechar processador csv", e);
        }
    }
}
