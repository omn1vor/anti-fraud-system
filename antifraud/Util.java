package antifraud;

import java.util.Arrays;

public class Util {
    public static boolean isInvalidIp(String address) {
        if (!address.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}")) {
            return true;
        }
        return Arrays.stream(address.split("\\."))
                .mapToInt(Integer::parseInt)
                .anyMatch(i -> i <= 0 || i > 255);
    }

    public static boolean isInvalidCardNumber(String number) {
        if (!number.matches("\\d{16}")) {
            return true;
        }
        int[] arr = Arrays.stream(number.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();
        return getCheckSum(arr) != arr[arr.length - 1];
    }

    private static int getCheckSum(int[] digits) {
        int controlNumber = 0;
        for (int i = 0; i < digits.length - 1; i++) {
            int num = digits[i] * (i % 2 == 0 ? 2 : 1);
            if (num > 9) {
                num -= 9;
            }
            controlNumber +=num;
        }
        return controlNumber % 10 == 0 ? 0 : 10 - controlNumber % 10;
    }
}
