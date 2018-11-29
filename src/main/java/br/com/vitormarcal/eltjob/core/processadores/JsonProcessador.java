package br.com.vitormarcal.eltjob.core.processadores;

import br.com.vitormarcal.eltjob.core.FileObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import lombok.NonNull;

import java.io.FileWriter;
import java.io.IOException;

public class JsonProcessador extends AbstractProcessador {

    private static final String DIRETORIO_JSON = "/json";
    private static final String EXTENSAO_JSON = ".json";
    private Gson gson;

    private FileWriter writer;
    private JsonWriter jsonWriter;

    public JsonProcessador(String fileName, String diretorioArquivo) {
        super(fileName + EXTENSAO_JSON, diretorioArquivo + DIRETORIO_JSON);
    }

    @Override
    protected boolean write(@NonNull FileObject fileObject) {
        gson.toJson(fileObject, fileObject.getClass(), jsonWriter);
        return true;
    }

    @Override
    protected boolean criarWriter() {
        if (this.writer == null) {
            try {
                this.writer = new FileWriter(this.file);
                inicializaGson();
                iniciaArrayDados();
                return true;
            } catch (IOException e) {
                throw new RuntimeException("Erro ao criar writer do json", e);
            }
        } else {
            return false;
        }

    }

    private void inicializaGson() {
        gson = new GsonBuilder()
                .disableHtmlEscaping()
                .create();
    }

    private void iniciaArrayDados() throws IOException {
        jsonWriter = new JsonWriter(writer);
        jsonWriter.setIndent("  ");
        jsonWriter.beginObject();
        jsonWriter.name("dados");
        jsonWriter.beginArray();
    }

    @Override
    public void close() {
        try {
            if (writer != null) {
                if (jsonWriter != null) {
                    jsonWriter.endArray();
                    jsonWriter.endObject();
                }
                writer.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("Erro ao fechar processador json", e);
        }

    }
}
