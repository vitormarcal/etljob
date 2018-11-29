package br.com.vitormarcal.eltjob.processadores;

import br.com.vitormarcal.eltjob.core.processadores.Processador;
import br.com.vitormarcal.eltjob.core.processadores.XmlProcessador;
import br.com.vitormarcal.eltjob.helpers.MockPOJO;
import br.com.vitormarcal.eltjob.helpers.MockPojoFactory;
import org.assertj.core.util.Files;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import org.xmlunit.builder.Input;
import org.xmlunit.xpath.JAXPXPathEngine;
import org.xmlunit.xpath.XPathEngine;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.xmlunit.matchers.HasXPathMatcher.hasXPath;

@RunWith(SpringRunner.class)
public class XmlProcessadorTest {

    private static final XPathEngine xpath = new JAXPXPathEngine();
    private static final String fileName = "teste";
    private String diretorioArquivo = "test/";
    private File diretorioBase;
    private File arquivo;

    @Before
    public void defineCaminhos() {
        diretorioArquivo = UUID.randomUUID() + "test/";
        diretorioBase = new File(diretorioArquivo);
        arquivo = new File(diretorioArquivo + "xml/" + fileName + ".xml");
    }

    @After
    public void deletaRecursosTemporarios() {
        if (diretorioBase.exists()) {
            Files.delete(diretorioBase);
        }
    }

    @Test
    public void givenXml_whenHasPath_thenCorrect() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5, true);
        try (Processador processador = new XmlProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(processador::writeLine);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        assertTrue(arquivo.exists());

        Input.Builder inputBuilder = Input.fromFile(arquivo);

        assertThat(inputBuilder, hasXPath("//xml"));
        assertThat(inputBuilder, hasXPath("//dados"));
        assertThat(inputBuilder, hasXPath("//mockpojo"));
        assertThat(inputBuilder, hasXPath("//nome"));
        assertThat(inputBuilder, hasXPath("//sobrenome"));
        assertThat(inputBuilder, hasXPath("//idade"));

        assertEquals(mockPOJOList.get(0).getNome(), xpath.evaluate("/xml/dados/mockpojo/nome", inputBuilder.build()));
        assertEquals(mockPOJOList.get(0).getIdade(), Integer.valueOf(xpath.evaluate("/xml/dados/mockpojo/idade", inputBuilder.build())));
        assertEquals(mockPOJOList.get(0).getSobrenome(), xpath.evaluate("/xml/dados/mockpojo/sobrenome", inputBuilder.build()));
        assertEquals("", xpath.evaluate("/xml/dados/mockpojo/cores", inputBuilder.build()));

    }


    @Test
    public void givenXmlWithFieldCollections_whenHasPath_thenCorrect() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5, false);
        try (Processador processador = new XmlProcessador(fileName, diretorioArquivo)) {
            processador.writeLine(mockPOJOList.get(0));
            processador.writeLine(mockPOJOList.get(1));
        } catch (Exception e) {
            fail(e.getMessage());
        }

        Input.Builder inputBuilder = Input.fromFile(arquivo);

        StringBuilder expected = new StringBuilder(",");
        mockPOJOList.get(0).getCores().forEach(e -> expected.append(e).append(","));

        String content = xpath.evaluate("/xml/dados/mockpojo/cores", inputBuilder.build()).replaceAll("\n", ",").replaceAll("\t", "");
        assertEquals(expected.toString(), content);


    }


    @Test
    public void write() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador xml = new XmlProcessador(fileName, diretorioArquivo)) {
            mockPOJOList.forEach(e -> assertTrue(xml.writeLine(e)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
    }

    @Test
    public void criarWriter() {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(10_000, true);
        try (Processador xml = new XmlProcessador(fileName, diretorioArquivo)) {
            assertTrue(xml.inicializaWriter());
            mockPOJOList.forEach(e -> assertFalse(xml.inicializaWriter()));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
        assertTrue(arquivo.exists());
    }

    @Test(expected = RuntimeException.class)
    public void close() throws Exception {
        List<MockPOJO> mockPOJOList = MockPojoFactory.get(5_000, true);

        Processador processador = null;

        try (Processador xml = new XmlProcessador(fileName, diretorioArquivo)) {
            processador = xml;
            assertTrue(xml.inicializaWriter());
            mockPOJOList.forEach(e -> assertTrue(xml.writeLine(e)));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        assertTrue(arquivo.exists());
        processador.close();//stream já fechado, deve retornar JobProcessException
    }
}
