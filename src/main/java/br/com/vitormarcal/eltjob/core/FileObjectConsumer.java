package br.com.vitormarcal.eltjob.core;

import br.com.vitormarcal.eltjob.core.processadores.Processador;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class FileObjectConsumer implements Consumer<FileObject> {

    @NonNull
    private final Processador processador;

    @Override
    public void accept(@NonNull FileObject fileObject) {
        this.processador.writeLine(fileObject);
    }


    @Override
    public Consumer<FileObject> andThen(Consumer<? super FileObject> after) {
        return null;
    }
}
