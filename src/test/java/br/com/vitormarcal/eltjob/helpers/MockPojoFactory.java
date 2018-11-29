package br.com.vitormarcal.eltjob.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MockPojoFactory {

    private static final Random random = new Random();

    private MockPojoFactory() {
        throw new UnsupportedOperationException("Only helper class");
    }

    public static List<MockPOJO> get(boolean emptyList) {
        return get(null, emptyList);
    }

    public static List<MockPOJO> get(Integer quantidade, boolean emptyList) {

        if (quantidade == null || quantidade.equals(0)){
            quantidade = random.nextInt(100);
        }

        List<MockPOJO> mockPOJOList = new ArrayList<>();


        while (quantidade != 0) {

            MockPOJO build = MockPOJO.builder()
                    .cores(emptyList ? null : Arrays.asList(getRamdomString(), getRamdomString(), getRamdomString()))
                    .sobrenome(getRamdomString())
                    .nome(getRamdomString())
                    .idade(random.nextInt(10))
                    .build();

            mockPOJOList.add(build);
            quantidade--;
        }

        return mockPOJOList;
    }


    private static String getRamdomString() {
        String possibleLetters = "123456789ABCDEFGHIJKLMNOPQRSTUVWYZ.";
        StringBuilder sb = new StringBuilder(10);
        for(int i = 0; i < 10; i++)
            sb.append(possibleLetters.charAt(random.nextInt(possibleLetters.length())));
        return sb.toString();
    }

}
