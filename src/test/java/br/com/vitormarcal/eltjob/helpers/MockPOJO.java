package br.com.vitormarcal.eltjob.helpers;

import br.com.vitormarcal.eltjob.core.AbstractFileObject;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
@Builder
public class MockPOJO extends AbstractFileObject {

    private String nome;
    private String sobrenome;
    private Integer idade;
    private List<String> cores;

}
