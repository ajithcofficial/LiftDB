package nosqlpkg;

public class Converter {
	public static int integerToBytes(int in, int from, byte out[]) {
		try {
			out[from++] = (byte) ((in & 0xff000000) >>> 24);
			out[from++] = (byte) ((in & 0x00ff0000) >> 16);
			out[from++] = (byte) ((in & 0x0000ff00) >> 8);
			out[from] = (byte) (in & 0x000000ff);
			

		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return -1;
		}
		return 0;
	}
	public static int bytesToInteger(byte in[], int from) {
		int a,i = 0, j = 0;

		try {
			a = in[from++];
			if(a < 0) {
				a *= -1;
				a = 128 + (128 -a);
			}
			i = a << 24;
			j |= i;
			
			a = in[from++];
			if(a < 0) {
				a *= -1;
				a = 128 + (128 -a);
			}
			i = a << 16;
			j |= i;
			
			a = in[from++];
			if(a < 0) {
				a *= -1;
				a = 128 + (128 -a);
			}
			i = a << 8;
			j |= i;
			
			a = in[from];
			if(a < 0) {
				a *= -1;
				a = 128 + (128 -a);
			}
			i = a;
			j |= i;

		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return -1;
		}
		return j;
	}
	public static String bytesToString(byte in[], int from, int len) {
		char str[] = new char[len];

		try {
			for (int i = 0; i < len; i++) {
				str[i] = (char)in[from + i];
				if (str[i] == 0) {
					break;
				}
			}
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return null;
		}
		return new String(str);

	}

	public static int stringToBytes(String str, int maxLen, int from, byte out[]) {
		int i;

		try {
			if (str.length() >= maxLen) {
				return -1;
			}

			for (i = 0; i < str.length(); i++) {
				out[from + i] = (byte) str.charAt(i);
			}
			out[from + i] = 0;
		}
		catch(ArrayIndexOutOfBoundsException e) {
			System.out.println(e);
			return -1;
		}

		return 0;
	}
}
