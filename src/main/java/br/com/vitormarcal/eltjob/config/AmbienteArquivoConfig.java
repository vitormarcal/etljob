package br.com.vitormarcal.eltjob.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "job")
@Data
@Component
public class AmbienteArquivoConfig {

    private BaseFileConfig customJob;


    @Data
    public static class BaseFileConfig {
        protected String cronExpression;
        protected String diretorio;
        protected String nomeBaseArquivo;
        protected String nome;
        protected String grupo;
    }
}
