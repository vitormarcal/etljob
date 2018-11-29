package br.com.vitormarcal.eltjob.core.processadores;

import br.com.vitormarcal.eltjob.core.FileObject;

public interface Processador extends AutoCloseable {


    boolean writeLine(FileObject fileObject);

    boolean inicializaWriter();

}
