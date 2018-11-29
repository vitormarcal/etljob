package br.com.vitormarcal.eltjob.processadores;

import br.com.vitormarcal.eltjob.core.processadores.JsonProcessador;
import br.com.vitormarcal.eltjob.core.processadores.Processador;
import br.com.vitormarcal.eltjob.helpers.MockPOJO;
import br.com.vitormarcal.eltjob.helpers.MockPojoFactory;
import com.google.gson.Gson;
import org.assertj.core.util.Files;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.comparator.ArraySizeComparator;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class JsonProcessadorTest {

    private final static String fileName = "teste";
    private static final Random random = new Random();
    private String diretorioArquivo = "test/";
    private File diretorioBase;
    private File arquivo;

    @Before
    public void defineCaminhos() {
        diretorioArquivo = UUID.randomUUID() + "test/";
        diretorioBase =  new File(diretorioArquivo);
        arquivo = new File(diretorioArquivo + "json/"+ fileName + ".json");
    }

    @After
    public void deletaRecursosTemporarios() {
        if (diretorioBase.exists()) {
            Files.delete(diretorioBase);
        }
    }

    @Test
    public void givenJsonAndFileObject_whenIdenticalContent_thenCorrect() throws JSONException {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(1, false);
        processa(mockPOJOList);

        Object objectJson = getObjectJson();

        assertNotNull(objectJson);

        String jsonFinal = objectJson.toString();

        String expected = String.format("{dados:[{nome:%s, sobrenome:%s, idade:%s, cores:%s}]}",
                mockPOJOList.get(0).getNome(), mockPOJOList.get(0).getSobrenome(), mockPOJOList.get(0).getIdade(), mockPOJOList.get(0).getCores().toString());

        JSONAssert.assertEquals(expected, jsonFinal, JSONCompareMode.STRICT);
    }

    @Test
    public void givenJsonAndFileObjectList_whenSizeIdentical_thenCorrect() throws JSONException {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(false);
        processa(mockPOJOList);

        Object objectJson = getObjectJson();

        assertNotNull(objectJson);

        String jsonFinal = objectJson.toString();

        String dados = String.format("{dados:[%s]}", mockPOJOList.size());

        JSONAssert.assertEquals(dados, jsonFinal, new ArraySizeComparator(JSONCompareMode.STRICT));


    }

    @Test
    public void givenJsonAndFileObjectList_whenSizeNotIdentical_thenCorrect() throws JSONException {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(false);
        processa(mockPOJOList);

        Object objectJson = getObjectJson();

        assertNotNull(objectJson);

        String jsonFinal = objectJson.toString();

        int fakeSize = 0;


        do {
            fakeSize = random.nextInt(500);
        } while (fakeSize == mockPOJOList.size());

        String dados = String.format("{dados:[%s]}", fakeSize);

        JSONAssert.assertNotEquals(dados, jsonFinal, new ArraySizeComparator(JSONCompareMode.STRICT));

    }

    private Object getObjectJson() {
        Object objectJson = null;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(arquivo))) {
            Gson gson = new Gson();
            objectJson = gson.fromJson(bufferedReader, Object.class);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        return objectJson;
    }

    public void processa(List<MockPOJO> mockPOJOList) {
        try (Processador json = new JsonProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(json::writeLine);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void write() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador json = new JsonProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(e -> assertTrue(json.writeLine(e)));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
    }

    @Test
    public void criarWriter() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador json = new JsonProcessador(fileName, diretorioArquivo)) {
            assertTrue(json.inicializaWriter());
            mockPOJOList.forEach(e -> assertFalse(json.inicializaWriter()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test(expected = RuntimeException.class)
    public void close() throws Exception {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5_000, true);

        Processador processador = null;

        try (Processador json = new JsonProcessador(fileName, diretorioArquivo)) {
            processador = json;
            assertTrue(json.inicializaWriter());
            mockPOJOList.forEach(e -> assertTrue(json.writeLine(e)));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
        processador.close();//stream já fechado, deve retornar JobProcessException
    }

}
