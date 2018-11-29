package br.com.vitormarcal.eltjob.core;

import br.com.vitormarcal.eltjob.core.processadores.*;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class FabricaDeConsumers implements AutoCloseable {

    private List<Processador> processadors;

    private List<Processador> pegaTodosProcessadores(@NonNull String fileName, @NonNull String diretorioArquivo) {
        Processador csv = getProcessadorCSV(fileName, diretorioArquivo);
        Processador xml = getProcessadorXML(fileName, diretorioArquivo);
        Processador xlsx = getProcessadorXLSX(fileName, diretorioArquivo);
        Processador json = getProcessadorJSON(fileName, diretorioArquivo);
        processadors = Arrays.asList(csv, xml, xlsx, json);
        return processadors;
    }

    private List<Processador> pegaTodosProcessadoresTranscricao(@NonNull String fileName, @NonNull String diretorioArquivo) {
        Processador csv = getProcessadorCSV(fileName, diretorioArquivo);
        Processador xml = getProcessadorXML(fileName, diretorioArquivo);
        Processador json = getProcessadorJSON(fileName, diretorioArquivo);
        processadors = Arrays.asList(csv, xml, json);

        return processadors;
    }

    private Processador getProcessadorCSV(@NonNull String fileName, @NonNull String diretorioArquivo) {
        return new CsvProcessador(fileName, diretorioArquivo);
    }

    private Processador getProcessadorXML(@NonNull String fileName, @NonNull String diretorioArquivo) {
        return new XmlProcessador(fileName, diretorioArquivo);
    }

    private Processador getProcessadorXLSX(@NonNull String fileName, @NonNull String diretorioArquivo) {
        return new XlsxProcessador(fileName, diretorioArquivo);
    }

    private Processador getProcessadorJSON(@NonNull String fileName, @NonNull String diretorioArquivo) {
        return new JsonProcessador(fileName, diretorioArquivo);
    }

    public List<FileObjectConsumer> pegaTodosTiposConsumers(@NonNull String nomeArquivo, @NonNull Long ano, @NonNull String diretorioArquivo) {
        List<FileObjectConsumer> consumers = new ArrayList<>();
        String fileName = formataNomeArquivo(nomeArquivo, ano);
        pegaTodosProcessadores(fileName, diretorioArquivo).forEach(p -> {
            FileObjectConsumer e = new FileObjectConsumer(p);
            consumers.add(e);

        });
        return consumers;
    }

    public List<FileObjectConsumer> pegaTodosTiposConsumersTranscricao(@NonNull String nomeArquivo, @NonNull String diretorioArquivo) {
        List<FileObjectConsumer> consumers = new ArrayList<>();
        pegaTodosProcessadoresTranscricao(nomeArquivo, diretorioArquivo).forEach(p -> {
            FileObjectConsumer e = new FileObjectConsumer(p);
            consumers.add(e);

        });
        return consumers;
    }

    public List<FileObjectConsumer> pegaTodosTiposConsumers(@NonNull String nomeArquivo, @NonNull String diretorioArquivo) {
        List<FileObjectConsumer> consumers = new ArrayList<>();
        pegaTodosProcessadores(nomeArquivo, diretorioArquivo).forEach(p -> {
            FileObjectConsumer e = new FileObjectConsumer(p);
            consumers.add(e);
        });
        return consumers;
    }

    private String formataNomeArquivo(@NonNull String nomeArquivo, @NonNull Long ano) {
        return String.format("%s-%s", nomeArquivo, ano);
    }

    @Override
    public void close() throws Exception {
        for (Processador processador : processadors) processador.close();
    }
}
