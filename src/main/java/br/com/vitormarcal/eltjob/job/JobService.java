package br.com.vitormarcal.eltjob.job;

import br.com.vitormarcal.eltjob.config.AmbienteArquivoConfig;
import org.quartz.Job;

/**
 * Atenção! Toda classe que implementar esta interface deverá ter um simples construtor vazio, caso deseja injetar
 * propriedades no objeto, faça via field injection e não constructor injection. De acordo com as docs
 * do quartz o objeto do Job deve ter um construtor sem argumentos. Para mais informações, consulte a documentação.
 * Como a injeção de dependência via field é justa neste caso, favor anotar a classe com a seguinte annotation:
 * "@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")" pois assim evitaremos falso positivos nas ferramentas de lint
 *
 * @see br.com.vitormarcal.eltjob.job.JobTrigger
 * @see br.com.vitormarcal.eltjob.config.AutowiringSpringBeanJobFactory
 */
public interface JobService extends Job {

    default String getJobName() {
        return getBaseFileConfig().getNome();
    }

    default String getGroupName() {
        String grupo = getBaseFileConfig().getGrupo();
        String nome = getBaseFileConfig().getNome();
        return grupo == null || grupo.isEmpty() ? nome : grupo;
    }

    default String getCronExpression() {
        return getBaseFileConfig().getCronExpression();
    }

    AmbienteArquivoConfig.BaseFileConfig getBaseFileConfig();

}
