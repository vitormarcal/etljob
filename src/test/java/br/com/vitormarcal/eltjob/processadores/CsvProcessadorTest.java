package br.com.vitormarcal.eltjob.processadores;

import br.com.vitormarcal.eltjob.core.processadores.CsvProcessador;
import br.com.vitormarcal.eltjob.core.processadores.Processador;
import br.com.vitormarcal.eltjob.helpers.MockPOJO;
import br.com.vitormarcal.eltjob.helpers.MockPojoFactory;
import com.opencsv.CSVReader;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.FileReader;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
public class CsvProcessadorTest {

    private static final String fileName = "teste";
    private String diretorioArquivo = "test/";
    private File diretorioBase;
    private File arquivo;

    @Before
    public void defineCaminhos() {
        diretorioArquivo = UUID.randomUUID() + "test/";
        diretorioBase = new File(diretorioArquivo);
        arquivo = new File(diretorioArquivo + "csv/" + fileName + ".csv");
    }


    @After
    public void deletaRecursosTemporarios() {
        if (diretorioBase.exists()) {
            Files.delete(diretorioBase);
        }
    }

    @Test
    public void write() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador csv = new CsvProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(e -> assertTrue(csv.writeLine(e)));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test
    public void criarWriter() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador csv = new CsvProcessador(fileName, diretorioArquivo)) {
            assertTrue(csv.inicializaWriter());
            mockPOJOList.forEach(e -> assertFalse(csv.inicializaWriter()));
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test(expected = RuntimeException.class)
    public void close() throws Exception {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5_000, true);

        Processador processador = null;

        try (Processador csv = new CsvProcessador(fileName, diretorioArquivo)) {
            processador = csv;
            assertTrue(csv.inicializaWriter());
            mockPOJOList.forEach(e -> assertTrue(csv.writeLine(e)));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
        processador.close();//stream já fechado, deve retornar JobProcessException
    }


    @Test
    public void givenCsvAndString_whenContentIdentical_thenCorrect() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5, true);

        processa(mockPOJOList);

        List<String[]> linhas = getLinhasCsv();

        assertNotNull(linhas);
        assertEquals(mockPOJOList.size() + 1, linhas.size()); //qtd registros + 1 que é a linha header do csv

        String[] rows = linhas.get(0)[0].replaceAll("\"", "").split(";");

        assertEquals("sobrenome", rows[1]);
        assertEquals("idade", rows[2]);
        assertEquals("cores", rows[3]);
    }

    @Test
    public void givenCsvAndFileObjects_whenContentIdentical_thenCorrenct() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5, true);

        processa(mockPOJOList);

        List<String[]> linhas = getLinhasCsv();

        assertNotNull(linhas);
        for (int i = 1, a = 0; i < linhas.size(); i++, a++) {
            String[] rows = linhas.get(i)[0].replaceAll("\"", "").split(";");

            assertEquals(mockPOJOList.get(a).getNome(), rows[0]);
            assertEquals(mockPOJOList.get(a).getSobrenome(), rows[1]);
            assertEquals(mockPOJOList.get(a).getIdade(), Integer.valueOf(rows[2]));
            assertEquals(3, rows.length);//sem collection
        }
    }


    @Test
    public void givenCsvAndFileObjectWithFieldCollection_thenCollunmCollectionInCsvEqualsToStringCollectionObject() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5, false);
        processa(mockPOJOList);

        List<String[]> linhas = getLinhasCsv();

        assertNotNull(linhas);
        for (int i = 1, a = 0; i < linhas.size(); i++, a++) {
            String[] rows = linhas.get(i)[0].replaceAll("\"", "").split(";");

            assertEquals(mockPOJOList.get(a).getCores().toString(), "[" + rows[3] + "]");
        }

    }

    public void processa(List<MockPOJO> mockPOJOList) {
        try (Processador csv = new CsvProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(csv::writeLine);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private List<String[]> getLinhasCsv() {
        List<String[]> linhas = null;
        try (CSVReader reader = new CSVReader(new FileReader(arquivo))) {
            assertTrue(arquivo.exists());
            linhas = reader.readAll();

        } catch (Exception e) {
            fail(e.getMessage());
        }
        return linhas;
    }
}
